package com.uoquo.platform.auth.controller;

import java.io.BufferedReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.uoquo.annotation.web.IgnoreAuth;
import com.uoquo.platform.common.SettingsCode;
import com.uoquo.platform.common.utils.WechatMsgCrypt;
import com.uoquo.platform.system.model.dto.SettingDto;
import com.uoquo.platform.system.service.SysSettingService;
import com.uoquo.utils.StringUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 微信消息推送接收处理。
 *
 * <p>参考：
 * <ul>
 *     <li>https://developers.weixin.qq.com/doc/service/guide/dev/push/</li>
 *     <li>https://developers.weixin.qq.com/doc/service/guide/dev/push/encryption.html</li>
 * </ul>
 *
 * <p>说明：本接口直接返回原始字符串（echostr / "success"），不包裹统一响应体，
 * 以满足微信服务器对接口返回格式的要求。
 */
@IgnoreAuth(all = true)
@Tag(name = "wechat", description = "微信消息推送")
@RestController
@RequestMapping("/v1/wechat")
public class WechatController {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /** 提取 XML 标签值（兼容 CDATA） */
    private static final Pattern TAG_PATTERN = Pattern.compile(
            "<(\\w+)>(?:<!\\[CDATA\\[(.*?)\\]\\]>|(.*?))</\\1>", Pattern.DOTALL);

    @Autowired
    private SysSettingService sysSettingService;

    /**
     * 服务器地址有效性验证（配置服务器 URL 时微信发起 GET 请求）。
     * 校验通过后原样返回 echostr。
     */
    @Operation(summary = "微信服务器URL验证", hidden = true)
    @GetMapping("/push")
    public String verify(@RequestParam("signature") String signature,
                         @RequestParam("timestamp") String timestamp,
                         @RequestParam("nonce") String nonce,
                         @RequestParam("echostr") String echostr) {
        String token = sysSettingService.getValueByCode(SettingsCode.WECHAT_MSG_TOKEN);
        if (WechatMsgCrypt.checkSignature(token, timestamp, nonce, signature)) {
            return echostr;
        }
        logger.warn("微信URL验证签名不通过：signature={}, timestamp={}, nonce={}", signature, timestamp, nonce);
        return "";
    }

    /**
     * 接收微信推送的消息/事件。
     * 支持安全模式（encrypt_type=aes）与明文模式，解析后交由业务处理，统一回复 "success"。
     */
    @Operation(summary = "接收微信推送消息", hidden = true)
    @PostMapping("/push")
    public String receive(HttpServletRequest request,
                          @RequestParam(value = "signature", required = false) String signature,
                          @RequestParam(value = "msg_signature", required = false) String msgSignature,
                          @RequestParam("timestamp") String timestamp,
                          @RequestParam("nonce") String nonce,
                          @RequestParam(value = "encrypt_type", required = false) String encryptType) {
        String body = this.readBody(request);
        if (StringUtil.isNull(body)) {
            return "success";
        }

        String token = sysSettingService.getValueByCode(SettingsCode.WECHAT_MSG_TOKEN);
        String plainXml;
        boolean encrypted = "aes".equalsIgnoreCase(encryptType) || body.contains("<Encrypt>");
        if (encrypted) {
            String encrypt = this.extractTag(body, "Encrypt");
            if (StringUtil.isNull(encrypt)) {
                logger.warn("微信安全模式消息缺少 Encrypt 节点");
                return "success";
            }
            // 安全模式：校验 msg_signature
            if (!WechatMsgCrypt.checkMsgSignature(token, timestamp, nonce, encrypt, msgSignature)) {
                logger.warn("微信消息体签名不通过：msg_signature={}", msgSignature);
                return "success";
            }
            String aesKey = sysSettingService.getValueByCode(SettingsCode.WECHAT_MSG_AESKEY);
            plainXml = WechatMsgCrypt.decrypt(aesKey, encrypt);
        } else {
            // 明文模式：校验普通 signature（如配置了）
            if (StringUtil.notNull(signature)
                    && !WechatMsgCrypt.checkSignature(token, timestamp, nonce, signature)) {
                logger.warn("微信明文消息签名不通过：signature={}", signature);
                return "success";
            }
            plainXml = body;
        }

        Map<String, String> message = this.parseXml(plainXml);
        this.handleMessage(message);
        // 不做被动回复，统一返回 success（微信要求 5 秒内响应）
        return "success";
    }

    /**
     * 处理解析后的微信消息/事件。
     * 目前仅记录日志，后续可按 MsgType / Event 分发到具体业务。
     */
    private void handleMessage(Map<String, String> message) {
        if (message.isEmpty()) {
            return;
        }
        String msgType = message.get("MsgType");
        String event = message.get("Event");
        if (logger.isInfoEnabled()) {
            logger.info("收到微信推送：msgType={}, event={}, from={}, to={}",
                    msgType, event, message.get("FromUserName"), message.get("ToUserName"));
        }
        // TODO 按 MsgType（text/image/event 等）与 Event（subscribe/unsubscribe/CLICK 等）分发处理
    }

    /**
     * 解析微信消息 XML 为键值对（兼容 CDATA）。
     */
    private Map<String, String> parseXml(String xml) {
        Map<String, String> map = new HashMap<>();
        if (StringUtil.isNull(xml)) {
            return map;
        }
        Matcher matcher = TAG_PATTERN.matcher(xml);
        while (matcher.find()) {
            String value = matcher.group(2) != null ? matcher.group(2) : matcher.group(3);
            map.put(matcher.group(1), value == null ? "" : value.trim());
        }
        return map;
    }

    /**
     * 提取单个 XML 标签的值（兼容 CDATA）。
     */
    private String extractTag(String xml, String tag) {
        Matcher matcher = Pattern.compile(
                "<" + tag + ">(?:<!\\[CDATA\\[(.*?)\\]\\]>|(.*?))</" + tag + ">", Pattern.DOTALL).matcher(xml);
        if (matcher.find()) {
            return matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
        }
        return null;
    }

    /**
     * 读取请求体（微信以 text/xml 提交）。
     */
    private String readBody(HttpServletRequest request) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (Exception e) {
            logger.error("读取微信消息体失败", e);
            return null;
        }
        return sb.toString();
    }
}
