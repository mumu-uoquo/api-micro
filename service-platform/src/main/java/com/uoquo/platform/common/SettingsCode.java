package com.uoquo.platform.common;

import java.util.Set;

/**
 * 配置编码（代码中需要固化的）
 * 优先级：用户 > 机构 > 系统
 */
public class SettingsCode {
    /**
     * 是否启用 MFA 认证
     */
    public static final String MFA_AUTH_ENABLED = "security.mfa.enabled";

    /** 会话超时时间控制 */
    public static final String SESSION_TIMEOUT = "security.session.timeout";

    /** RSA 公钥 */
    public static final String RSA_PUBLIC_KEY  = "security.rsa.public-key";
    /** RSA 私钥 */
    public static final String RSA_PRIVATE_KEY = "security.rsa.private-key";

    /** AES 密钥 */
    public static final String AES_KEY         = "security.aes.key";
    /** TAES时间片长度（秒） */
    public static final String AES_TOTP_STEP   = "security.aes.totp";

    /** 网关通信秘钥 */
    public static final String GLOBAL_GATEWAY_KEY = "security.gateway.key";

    /**
     * 需要公开的配置编码集合（不登录即可获取）
     */
    public static final Set<String> PUBLIC_KEYS = Set.of(
        RSA_PUBLIC_KEY    // security.rsa.publicKey
    );

    /**
     * 内部使用的配置编码集合（不可返回给前端）
     */
    public static final Set<String> PRIVATE_KEYS = Set.of(
        RSA_PRIVATE_KEY,   // security.rsa.privateKey
        AES_KEY,            // security.aes.key
        GLOBAL_GATEWAY_KEY // security.gateway.key
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
