package com.uoquo.platform.common.exception;

import com.uoquo.web.BaseReturnCode;
import com.uoquo.web.ReturnLevel;

/**
 * 账户相关响应码（21XXX）
 * <ul>
 *     <li>210XX：用户相关</li>
 *     <li>211XX：角色相关</li>
 *     <li>212XX：模块资源</li>
 * </ul>
 */
public class AccountReturnCode extends BaseReturnCode {
    AccountReturnCode(String code, String text) {
        this(code, text, ReturnLevel.ERROR);
    }
    AccountReturnCode(String code, String text, ReturnLevel level) {
        super(code, text, level);
    }

    /** =============================== 210XX 用户相关 =============================== **/
    public static BaseReturnCode PHONE_EXIST       = new AccountReturnCode("21001", "手机号已存在");
    public static BaseReturnCode PHONE_NOT_EXIST   = new AccountReturnCode("21002", "手机号不存在");
    public static BaseReturnCode ACCOUNT_EXIST     = new AccountReturnCode("21003", "账号名称重复");
    public static BaseReturnCode ACCOUNT_NOT_EXIST = new AccountReturnCode("21004", "用户不存在");

    public static BaseReturnCode REFERRAL_CODE_ERROR    = new AccountReturnCode("21010", "生成推介码出错");
    public static BaseReturnCode ACCOUNT_PASSWORD_ERROR = new AccountReturnCode("21011", "账号密码错误");
    public static BaseReturnCode OLD_PASSWORD_ERROR     = new AccountReturnCode("21012", "旧密码错误");
    public static BaseReturnCode PASSWORD_ERROR         = new AccountReturnCode("21013", "密码错误");
    public static BaseReturnCode CAPTCHA_ERROR          = new AccountReturnCode("21014", "验证码错误");
    public static BaseReturnCode ABNORMAL_LOGOUT        = new AccountReturnCode("21015", "异常退出");

    public static BaseReturnCode ACCOUNT_DISABLE      = new AccountReturnCode("21020", "账户被禁用");
    public static BaseReturnCode ACCOUNT_DELETE       = new AccountReturnCode("21021", "账户已删除");
    public static BaseReturnCode ACCOUNT_LOCK         = new AccountReturnCode("21022", "账户被锁定");
    public static BaseReturnCode ACCOUNT_UNBOUND_2FA  = new AccountReturnCode("21023", "账户未绑定双因子认证");

    public static BaseReturnCode TOTP_VALIDATION_ERROR = new AccountReturnCode("21030", "动态码不正确");
    public static BaseReturnCode TOTP_ATTEMPT_EXCEED   = new AccountReturnCode("21031", "动态码错误次数过多，请重新获取二维码");
    public static BaseReturnCode TOTP_QRCODE_EXPIRED   = new AccountReturnCode("21032", "绑定已过期，请重新获取二维码");

    public static BaseReturnCode CAPTCHA_SEND_TOO_FREQUENT   = new AccountReturnCode("21040", "验证码发送过于频繁，请稍后再试");
    public static BaseReturnCode CAPTCHA_SECRET_EXPIRED      = new AccountReturnCode("21041", "验证码已过期，请重新获取");
    public static BaseReturnCode CAPTCHA_CODE_ERROR          = new AccountReturnCode("21042", "验证码错误");
    public static BaseReturnCode CAPTCHA_CODE_ATTEMPT_EXCEED = new AccountReturnCode("21043", "验证码错误次数过多，请重新获取");

    public static BaseReturnCode REGISTER_DISABLED = new AccountReturnCode("21050", "系统未开启注册");

    public static BaseReturnCode CREDENTIAL_STATE_INVALID  = new AccountReturnCode("21060", "授权状态已失效，请重新发起");
    public static BaseReturnCode CREDENTIAL_EXCHANGE_FAILED = new AccountReturnCode("21061", "第三方授权换取用户标识失败");

    public static BaseReturnCode OPS_LOGIN_LOCKED       = new AccountReturnCode("21070", "运维登录已锁定，请24小时后再试");
    public static BaseReturnCode OPS_AUTH_FAILED        = new AccountReturnCode("21071", "运维认证失败");
    public static BaseReturnCode EMERGENCY_LOGIN_LOCKED = new AccountReturnCode("21072", "紧急登录已锁定，请24小时后再试");

    /** =============================== 211XX 角色相关 =============================== **/
    public static BaseReturnCode ROLE_NAME_EXIST = new AccountReturnCode("22101", "角色名称重复");

    /** =============================== 222XX 模块资源 =============================== **/
    public static BaseReturnCode MODULE_CODE_EXIST = new AccountReturnCode("22201", "模块编码重复");



}
