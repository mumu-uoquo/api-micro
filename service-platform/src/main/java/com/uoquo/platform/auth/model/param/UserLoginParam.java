package com.uoquo.platform.auth.model.param;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 入参：用户登录
 */
@Schema(description = "用户登录")
public class UserLoginParam {

    @Schema(description = "登录账号")
    private String account;

    @Schema(description = "登录密码")
    private String password;

    @Schema(description = "双因子动态码")
    private String totpCode;

    @Schema(description = "验证码")
    private String captcha;

    @Schema(description = "是否记住")
    private Boolean rememberMe;

    @Schema(description = "刷新token")
    private String refreshToken;

    @Schema(description = "设备标识码")
    private String deviceId;

    @Schema(description = "当前角色")
    private String currentRoleId;

    @Schema(description = "UA（主要用于移动端登录）")
    private String userAgent;

    @Schema(description = "发起方版本")
    private String appVersion;

    @Schema(description = "TOTP验证临时Token")
    private String tempToken;

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getTotpCode() {
        return totpCode;
    }

    public void setTotpCode(String totpCode) {
        this.totpCode = totpCode;
    }

    public String getCaptcha() {
        return captcha;
    }

    public void setCaptcha(String captcha) {
        this.captcha = captcha;
    }

    public Boolean getRememberMe() {
        return rememberMe;
    }

    public void setRememberMe(Boolean rememberMe) {
        this.rememberMe = rememberMe;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getCurrentRoleId() {
        return currentRoleId;
    }

    public void setCurrentRoleId(String currentRoleId) {
        this.currentRoleId = currentRoleId;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getTempToken() {
        return tempToken;
    }

    public void setTempToken(String tempToken) {
        this.tempToken = tempToken;
    }
}
