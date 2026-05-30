package com.uoquo.platform.common;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 配置编码（用户 > 机构 > 系统）
 */
public class SettingsCode {
    /**
     * 是否启用 MFA 认证
     */
    public static final String MFA_AUTH_ENABLED = "security.mfa.enabled";

    /** 会话超时时间控制 */
    public static final String SESSION_TIMEOUT = "security.session.timeout";

    /** RSA 公钥 */
    public static final String RSA_PUBLIC_KEY  = "security.rsa.publicKey";
    /** RSA 私钥 */
    public static final String RSA_PRIVATE_KEY = "security.rsa.privateKey";

    /** AES 密钥 */
    public static final String AES_KEY         = "security.aes.key";
    /** TAES时间片长度（秒） */
    public static final String AES_TIME_STEP   = "security.aes.time-step";

    /** 网关通信秘钥 */
    public static final String GLOBAL_GATEWAY_KEY = "security.gateway.key";

    /**
     * 需要加密存储的配置编码集合
     * config_value 在存储时自动加密、查询时自动解密
     */
    public static final Set<String> ENCRYPTED_KEYS;
    static {
        Set<String> keys = new HashSet<>();
        keys.add(RSA_PRIVATE_KEY);           // security.rsa.privateKey
        keys.add(AES_KEY);                   // security.aes.key
        keys.add(GLOBAL_GATEWAY_KEY);        // security.gateway.key
        ENCRYPTED_KEYS = Collections.unmodifiableSet(keys);
    }

}
