package com.uoquo.platform.auth.service.impl;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import com.uoquo.utils.http.HttpParams;
import com.uoquo.utils.http.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.uoquo.platform.auth.service.WechatService;
import com.uoquo.platform.common.SettingsCode;
import com.uoquo.platform.common.exception.AccountReturnCode;
import com.uoquo.platform.system.service.SysSettingService;
import com.uoquo.web.exception.ParamErrorException;
import com.uoquo.web.exception.UoquoException;
import com.uoquo.utils.StringUtil;
import com.uoquo.utils.json.JsonUtil;

/**
 * 微信/企微相关服务实现
 */
@Service
public class WechatServiceImpl implements WechatService {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final SysSettingService sysSettingService;

    /**
     * 构造函数注入依赖
     */
    public WechatServiceImpl(SysSettingService sysSettingService) {
        this.sysSettingService = sysSettingService;
    }

    @Override
    public String exchangeWechatOpenId(String code) {
        logger.debug("开始交换微信OpenID，授权码：{}", code);

        // 参数验证
        if (StringUtil.isNull(code)) {
            logger.error("微信授权码不能为空");
            throw new ParamErrorException("微信授权码不能为空");
        }
        
        try {
            String appid  = this.getSysConfig(SettingsCode.WECHAT_APPID);
            String secret = this.getSysConfig(SettingsCode.WECHAT_SECRET);
            String openid = this.getWechatOpenId(appid, secret, code);
            return openid;
        } catch (UoquoException e) {
            throw e; // 重新抛出已知的业务异常
        } catch (Exception e) {
            logger.error("微信OpenID交换过程中发生未预期异常", e);
            throw new UoquoException(AccountReturnCode.CREDENTIAL_EXCHANGE_FAILED, e.getMessage());
        }
    }

    @Override
    public String exchangeWecomUserId(String code) {
        logger.debug("开始交换企业微信UserID，授权码：{}", code);

        // 参数验证
        if (StringUtil.isNull(code)) {
            logger.error("企业微信授权码不能为空");
            throw new ParamErrorException("企业微信授权码不能为空");
        }
        
        try {
            String corpid = this.getSysConfig(SettingsCode.WECOM_CORPID);
            String secret = this.getSysConfig(SettingsCode.WECOM_SECRET);
            // 第一步：获取access_token
            String accessToken = this.getWecomAccessToken(corpid, secret);
            // 第二步：通过code获取userid
            String userid = this.getWecomUserId(accessToken, code);
            return userid;
        } catch (UoquoException e) {
            throw e; // 重新抛出已知的业务异常
        } catch (Exception e) {
            logger.error("企业微信UserID交换过程中发生未预期异常", e);
            throw new UoquoException(AccountReturnCode.CREDENTIAL_EXCHANGE_FAILED, e.getMessage());
        }
    }

    @Override
    public String exchangeOpsWecomMobile(String code) {
        logger.debug("开始交换运维企业微信手机号，授权码：{}", code);
        
        // 参数验证
        if (StringUtil.isNull(code)) {
            logger.error("运维企业微信授权码不能为空");
            throw new ParamErrorException("运维企业微信授权码不能为空");
        }
        
        try {
            String corpid = this.getSysConfig(SettingsCode.OPS_WECOM_CORPID);
            String secret = this.getSysConfig(SettingsCode.OPS_WECOM_SECRET);
            // 第一步：获取access_token
            String accessToken = this.getWecomAccessToken(corpid, secret);
            // 第二步：通过code获取userid
            String userTicket = this.getWecomUserTicket(accessToken, code);
            // 第三步：通过user_ticket获取手机号
            String mobile = this.getWecomUserMobile(accessToken, userTicket);
            return mobile;
        } catch (UoquoException e) {
            throw e; // 重新抛出已知的业务异常
        } catch (Exception e) {
            logger.error("运维企业微信手机号交换过程中发生未预期异常", e);
            throw new UoquoException(AccountReturnCode.CREDENTIAL_EXCHANGE_FAILED, e.getMessage());
        }
    }

    @Override
    public String opsMfaHtml(boolean success, String message) {
        logger.debug("生成运维MFA HTML页面，成功状态：{}，消息内容：{}", success, message);

        String html = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
                    <title>运维授权</title>
                    <style>
                        * { margin: 0; padding: 0; box-sizing: border-box; }
                        body {
                            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Arial, sans-serif;
                            text-align: center;
                            background: #f5f5f5;
                            min-height: 100vh;
                            display: flex;
                            flex-direction: column;
                            align-items: center;
                            justify-content: center;
                            padding: 20px;
                        }
                        h1 {
                            font-size: 1.5rem;
                            font-weight: 600;
                            color: #333;
                            margin-bottom: 24px;
                        }
                        h1.error {
                            color: #cc0000;
                            margin-bottom: 20px;
                        }
                        .message {
                            font-size: 1rem;
                            width: 90%;
                            color: #cc0000;
                            max-width: 340px;
                            line-height: 1.5;
                            padding: 28px 20px;
                            border-radius: 12px;
                            border: 2px solid #cc0000;
                            background-color: #ffe6e6;
                        }
                        .message.code {
                            font-size: 3rem;
                            font-weight: bold;
                            color: #0066cc;
                            border: 2px solid #0066cc;
                            background-color: #f0f8ff;
                            letter-spacing: 8px;
                            word-break: break-all;
                            display: inline-block;
                        }
                        .instructions {
                            margin-top: 28px;
                            color: #888;
                            font-size: 0.95rem;
                            line-height: 1.6;
                        }
                    </style>
                </head>
                <body>
                """;
        if (success) {
            html += "  <h1>动态口令</h1>\n" +
                    "  <div class=\"message code\">" + message + "</div>\n" +
                    "  <div class=\"instructions\">\n" +
                    "      <p>请在运维登录页面输入该动态口令</p>\n" +
                    "      <p>该动态口令在5分钟内有效</p>\n" +
                    "  </div>\n";
        } else {
            html += "  <h1 class=\"error\">错误提示</h1>\n" +
                    "  <div class=\"message\">" + message + "</div>\n" +
                    "  <div class=\"instructions\">\n" +
                    "      <p>请返回运维登录页面</p>\n" +
                    "      <p>刷新二维码后重新尝试</p>\n" +
                    "  </div>\n";
        }
        html += "</body></html>";
        return html;
    }

    /**
     * 微信：换取OpenID
     * https://developers.weixin.qq.com/doc/oplatform/Website_App/WeChat_Login/Authorized_Interface_Calling_UnionID.html
     */
    private String getWechatOpenId(String appid, String secret, String code) throws Exception {
        logger.debug("调用微信API获取OpenID");
        String url = "https://api.weixin.qq.com/sns/oauth2/access_token";
        HttpParams params = new HttpParams();
        params.add("appid",  appid);
        params.add("secret", secret);
        params.add("code",   code);
        params.add("grant_type", "authorization_code");
        String respBody = HttpUtil.get(url, params);
        String openid = this.getResponseValue("openid", respBody);
        logger.debug("微信OpenID交换成功，OpenID：{}", openid);
        return openid;
    }

    /**
     * 企微：获取access_token
     * https://developer.work.weixin.qq.com/document/path/91039
     */
    private String getWecomAccessToken(String corpid, String secret) throws Exception {
        logger.debug("调用企业微信API获取access_token");
        String tokenUrl = "https://qyapi.weixin.qq.com/cgi-bin/gettoken";
        HttpParams tokenParams = new HttpParams();
        tokenParams.add("corpid", corpid);
        tokenParams.add("corpsecret", secret);
        String tokenRespBody = HttpUtil.get(tokenUrl, tokenParams);
        String accessToken = this.getResponseValue("access_token", tokenRespBody);
        logger.debug("获取企业微信access_token成功：{}", accessToken);
        return accessToken;
    }

    /**
     * 企微：换取userid
     * https://developer.work.weixin.qq.com/document/path/98176
     */
    private String getWecomUserId(String accessToken, String code) throws Exception {
        logger.debug("调用企业微信API获取UserID");
        String userUrl = "https://qyapi.weixin.qq.com/cgi-bin/auth/getuserinfo";
        HttpParams userParams = new HttpParams();
        userParams.add("access_token", accessToken);
        userParams.add("code", code);
        String userRespBody = HttpUtil.get(userUrl, userParams);
        String userid = this.getResponseValue("userid", userRespBody);
        logger.info("企业微信UserID交换成功，UserID：{}", userid);
        return userid;
    }

    /**
     * 企微：换取user_ticket
     * https://developer.work.weixin.qq.com/document/path/91023
     */
    private String getWecomUserTicket(String accessToken, String code) throws Exception {
        logger.debug("调用企业微信API获取user_ticket");
        String userUrl = "https://qyapi.weixin.qq.com/cgi-bin/auth/getuserinfo";
        HttpParams userParams = new HttpParams();
        userParams.add("access_token", accessToken);
        userParams.add("code", code);
        String userRespBody = HttpUtil.get(userUrl, userParams);
        String userTicket = this.getResponseValue("user_ticket", userRespBody);
        logger.info("企业微信user_ticket交换成功，user_ticket：{}", userTicket);
        return userTicket;
    }

    /**
     * 企微：获取用户mobile
     * https://developer.work.weixin.qq.com/document/path/91023
     */
    private String getWecomUserMobile(String accessToken, String userTicket) throws Exception {
        // https://developer.work.weixin.qq.com/document/path/95833
        logger.debug("调用企业微信API获取手机号");
        String detailUrl = "https://qyapi.weixin.qq.com/cgi-bin/auth/getuserdetail?access_token=" + urlEncode(accessToken);
        HttpParams detailParams = new HttpParams();
        detailParams.add("user_ticket", userTicket);
        String detailRespBody = HttpUtil.json(detailUrl, detailParams);
        String mobile = this.getResponseValue("mobile", detailRespBody);
        logger.info("企业微信手机号交换成功，手机号：{}", mobile);
        return mobile;
    }

    private String getSysConfig(String code) {
        String setting = sysSettingService.getValueByCode(code);
        if (StringUtil.isNull(setting)) {
            throw new ParamErrorException("缺少配置项：" + code);
        }
        return setting;
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String getResponseValue(String key, String respBody) {
        Map<String, Object> resp = JsonUtil.deserialize(respBody);
        Object val = resp.get(key);
        if (val == null) {
            Object errcode = resp.get("errcode");
            Object errmsg  = resp.get("errmsg");
            logger.error("微信返回的错误码：{}，错误信息：{}，原始响应：{}", errcode, errmsg, respBody);
            throw new UoquoException(AccountReturnCode.CREDENTIAL_EXCHANGE_FAILED, (String)errmsg);
        }
        return (String)val;
    }
}