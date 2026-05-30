package com.uoquo.platform.message.controller;

import com.uoquo.utils.json.JsonUtil;
import com.uoquo.web.ReturnData;
import com.uoquo.annotation.web.IgnoreAuth;
import com.uoquo.web.param.IdParam;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

/**
 * 下推消息的执行器
 * <ul>
 *     <li>该应用只负责三方系统的实现，消息的组装以及记录推送日志等由调用方自行负责</li>
 *     <li>由Feign内部调用，不提供外部接口</li>
 *     <li>站内WEB消息交由 MessageEventListener 处理</li>
 * </ul>
 * @author xuhz
 */
@Hidden
@Tag(name = "message", description = "消息发送")
@IgnoreAuth(inner = true)
@RestController
@RequestMapping("/v1/message/send")
public class MessgeSendController {
    final Logger logger = LoggerFactory.getLogger(getClass());

    @Operation(summary = "下推消息：微信", hidden = true,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "收件ID", required = true)
    )
    @PostMapping("/weixin")
    public ReturnData<String> send2Weixin(HttpServletRequest request, @RequestBody @Valid IdParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("send2Weixin param: {}", JsonUtil.serialize(param));
        }
        // TODO 下推消息：微信
        return new ReturnData<>();
    }

    @Operation(summary = "下推消息：APP", hidden = true,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "收件ID", required = true)
    )
    @PostMapping("/app")
    public ReturnData<String> send2App(HttpServletRequest request, @RequestBody @Valid IdParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("send2App param: {}", JsonUtil.serialize(param));
        }
        // TODO 下推消息：APP
        return new ReturnData<>();
    }

    @Operation(summary = "下推消息：短信", hidden = true,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "收件ID", required = true)
    )
    @PostMapping("/sms")
    public ReturnData<String> send2Sms(HttpServletRequest request, @RequestBody @Valid IdParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("send2Sms param: {}", JsonUtil.serialize(param));
        }
        // TODO 下推消息：短信
        return new ReturnData<>();
    }
}
