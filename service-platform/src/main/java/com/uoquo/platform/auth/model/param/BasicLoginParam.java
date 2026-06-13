package com.uoquo.platform.auth.model.param;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 登录入参基类：所有登录方式的公共字段
 */
@Schema(description = "登录基础参数")
public class BasicLoginParam {

    @Schema(description = "是否记住")
    private Boolean rememberMe;

    @Schema(description = "UA（主要用于移动端登录）")
    private String userAgent;

    @Schema(description = "发起方版本")
    private String appVersion;

    public Boolean getRememberMe() {
        return rememberMe;
    }

    public void setRememberMe(Boolean rememberMe) {
        this.rememberMe = rememberMe;
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
}
