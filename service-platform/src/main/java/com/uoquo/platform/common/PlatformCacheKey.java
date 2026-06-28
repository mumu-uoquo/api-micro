package com.uoquo.platform.common;

import com.uoquo.web.BaseCacheKey;

/**
 * 缓存key的常量定义
 * @author xuhz
 */
public class PlatformCacheKey extends BaseCacheKey {

    /**
     * SSE订阅码
     */
    public final static String SSE_CODE_PREFIX = "UOQUO:SSE:CODE:";

    /**
     * 用户验证码标识
     */
    public static final String USER_CAPTCHA_FLAG       = "UOQUO:USER:CAPTCHA:FLAG:";

    /**
     * 用户验证码前缀
     */
    public static final String USER_CAPTCHA_CODE       = "UOQUO:USER:CAPTCHA:CODE:";

    /**
     * 文件上传临时码前缀
     */
    public static final String DFS_UPLOAD_PREFIX         = "UOQUO:DFS:UPLOAD:";

    /**
     * 文件下载临时码前缀
     */
    public static final String DFS_DOWNLOAD_PREFIX       = "UOQUO:DFS:DOWNLOAD:";

    /**
     * 文件MD5码缓存前缀
     */
    public static final String DFS_FILE_MD5_PREFIX       = "UOQUO:DFS:FILE_MD5:";

    /**
     * 2FA临时Token前缀（登录后未验证TOTP前）
     */
    public static final String TOTP_TEMP_TOKEN          = "UOQUO:TOTP:TEMP_TOKEN:";

    /**
     * 2FA绑定临时密钥前缀（生成二维码后，绑定前）
     */
    public static final String TOTP_BIND_SECRET         = "UOQUO:TOTP:BIND_SECRET:";

    /**
     * 2FA验证错误次数前缀（防暴力破解）
     */
    public static final String TOTP_VERIFY_ERROR        = "UOQUO:TOTP:VERIFY_ERROR:";

    /**
     * 手机验证码获取次数限制前缀
     */
    public static final String PHONE_CAPTCHA_ERROR     = "UOQUO:PHONE:CAPTCHA:ERROR:";

    /**
     * 手机验证码发送频率限制前缀
     */
    public static final String PHONE_CAPTCHA_LIMIT      = "UOQUO:PHONE:CAPTCHA:LIMIT:";

    /**
     * 邮箱验证码获取次数限制前缀
     */
    public static final String EMAIL_CAPTCHA_ERROR     = "UOQUO:EMAIL:CAPTCHA:ERROR:";

    /**
     * 邮箱验证码发送频率限制前缀
     */
    public static final String EMAIL_CAPTCHA_LIMIT      = "UOQUO:EMAIL:CAPTCHA:LIMIT:";

    /**
     * 第三方凭证绑定临时Token前缀（凭证未绑定时生成，TTL=300s）
     */
    public static final String BIND_TEMP_TOKEN = "UOQUO:BIND_TEMP:";

    /**
     * 第三方扫码登录 state 缓存前缀（记录 scene/appid/agentId/status/code，TTL=600s）
     */
    public static final String CREDENTIAL_STATE = "UOQUO:CREDENTIAL:STATE:";

    /**
     * 运维登录连续失败次数前缀（按账号计数）
     */
    public static final String OPS_LOGIN_FAIL = "UOQUO:OPS:LOGIN:FAIL:";

    /**
     * 运维登录锁定标识前缀（连续失败5次后锁定24小时）
     */
    public static final String OPS_LOGIN_LOCK = "UOQUO:OPS:LOGIN:LOCK:";

}
