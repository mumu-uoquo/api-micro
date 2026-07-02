package com.uoquo.platform.common;

import java.util.Set;

/**
 * 配置编码（代码中需要固化的）
 * 优先级：用户 > 机构 > 系统
 */
public class SettingsCode {
    /** 序列号 */
    public static final String SERIAL_NUMBER    = "sys.serial-number";
    /* ************************ 安全相关 ************************** */
    /** 是否启用 MFA 认证 */
    public static final String MFA_AUTH_ENABLED = "security.mfa.enabled";
    /** 会话超时时间控制 */
    public static final String SESSION_TIMEOUT  = "security.session.timeout";
    /** RSA 公钥 */
    public static final String RSA_PUBLIC_KEY   = "security.rsa.public-key";
    /** RSA 私钥 */
    public static final String RSA_PRIVATE_KEY  = "security.rsa.private-key";
    /** AES 密钥 */
    public static final String AES_KEY          = "security.aes.key";
    /** TAES时间片长度（秒） */
    public static final String AES_TOTP_STEP    = "security.aes.totp";

    /** 网关通信秘钥 */
    public static final String GLOBAL_GATEWAY_KEY = "security.gateway.key";

    /* ************************ 登录相关 ************************** */
    /** 是否开启注册 */
    public static final String REGISTER_ENABLE     = "sys.register.enabled";
    /** 是否开启水印 */
    public static final String WATERMARK_ENABLE    = "sys.watermark.enabled";
    /** 是否开启账号密码登录 */
    public static final String LOGIN_ACCOUNT_ENABLE = "login.account.enabled";
    /** 是否开启短信码登录 */
    public static final String LOGIN_SMS_ENABLE    = "login.sms.enabled";
    /** 是否开启微信登录 */
    public static final String LOGIN_WECHAT_ENABLE = "login.wechat.enabled";
    /** 是否开启企微在线登录 */
    public static final String LOGIN_WECOM_ENABLE  = "login.wecom.enabled";
    /** 是否开启紧急认证登录（仅MFA认证） */
    public static final String LOGIN_EMERG_ENABLE  = "login.emerg.enabled";

    /* ************************ 第三方登录应用配置 ************************** */
    /** 微信 appid */
    public static final String WECHAT_APPID        = "login.wechat.appid";
    /** 微信 secret */
    public static final String WECHAT_SECRET       = "login.wechat.secret";
    /** 微信回调地址 */
    public static final String WECHAT_REDIRECT_URI = "login.wechat.redirect-uri";
    /** 渲染方式 */
    public static final String WECHAT_RENDER_TYPE  = "login.wechat.render-type";
    /** 企业微信 corpid（appid） */
    public static final String WECOM_CORPID        = "login.wecom.corpid";
    /** 企业微信 agentid */
    public static final String WECOM_AGENTID       = "login.wecom.agentid";
    /** 企业微信 secret */
    public static final String WECOM_SECRET        = "login.wecom.secret";
    /** 企业微信回调地址 */
    public static final String WECOM_REDIRECT_URI  = "login.wecom.redirect-uri";
    /** 渲染方式 */
    public static final String WECOM_RENDER_TYPE   = "login.wecom.render-type";

    /* ************************ 运维企微配置 ************************** */
    /** 运维企微 corpid */
    public static final String OPS_WECOM_CORPID        = "ops.wecom.corpid";
    /** 运维企微 agentid */
    public static final String OPS_WECOM_AGENTID       = "ops.wecom.agentid";
    /** 运维企微 secret */
    public static final String OPS_WECOM_SECRET        = "ops.wecom.secret";
    /** 运维企微回调地址 */
    public static final String OPS_WECOM_REDIRECT_URI  = "ops.wecom.redirect-uri";

    /* ************************ 微信消息推送配置 ************************** */
    /** 微信消息推送 Token（服务器配置中的 Token，用于签名校验） */
    public static final String WECHAT_MSG_TOKEN  = "sys.wechat.push.token";
    /** 微信消息推送 EncodingAESKey（安全模式消息体加解密） */
    public static final String WECHAT_MSG_AESKEY = "sys.wechat.push.aeskey";


    /**
     * 需要公开的配置编码集合（不登录即可获取）
     */
    public static final Set<String> PUBLIC_KEYS = Set.of(
            RSA_PUBLIC_KEY,      // security.rsa.publicKey
            REGISTER_ENABLE,     // sys.register.enabled
            WATERMARK_ENABLE,    // sys.watermark.enabled
            LOGIN_SMS_ENABLE,    // login.sms.enabled
            LOGIN_WECHAT_ENABLE, // login.wechat.enabled
            LOGIN_WECOM_ENABLE,  // login.wecom.enabled
            LOGIN_EMERG_ENABLE,  // login.emerg.enabled
            LOGIN_ACCOUNT_ENABLE // login.account.enabled
    );

    /**
     * 内部使用的配置编码集合（不可返回给前端）
     */
    public static final Set<String> PRIVATE_KEYS = Set.of(
            SERIAL_NUMBER,      // sys.serial-number
            RSA_PRIVATE_KEY,    // security.rsa.privateKey
            AES_KEY,            // security.aes.key
            GLOBAL_GATEWAY_KEY, // security.gateway.key
            WECHAT_MSG_AESKEY,  // sys.wechat.push.aeskey
            WECHAT_MSG_TOKEN,   // sys.wechat.push.token
            OPS_WECOM_CORPID,      // ops.wecom.corpid
            OPS_WECOM_AGENTID,     // ops.wecom.agentid
            OPS_WECOM_SECRET,      // ops.wecom.secret
            OPS_WECOM_REDIRECT_URI // ops.wecom.redirect-uri
    );

    /**
     * 获取编码的公开范围
     */
    public static String getPublicType(String code) {
        if (code == null) {
            return DictionaryCodeEnum.ROLE_TYPE_INNER.getCode();
        }
        if (PUBLIC_KEYS.contains(code)) {
            return DictionaryCodeEnum.ROLE_TYPE_NORMAL.getCode();
        } else if (PRIVATE_KEYS.contains(code)) {
            return DictionaryCodeEnum.ROLE_TYPE_PRIVATE.getCode();
        } else {
            return DictionaryCodeEnum.ROLE_TYPE_INNER.getCode();
        }
    }
}
