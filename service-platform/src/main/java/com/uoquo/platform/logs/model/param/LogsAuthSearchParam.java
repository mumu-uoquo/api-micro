package com.uoquo.platform.logs.model.param;

import com.uoquo.web.param.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Date;

/**
 * 入参：登录日志查询
 * @author uoquo
 */
@Schema(description = "入参：登录日志查询")
public class LogsAuthSearchParam extends PageRequest {

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "用户名称（精确匹配）")
    private String userName;

    @Schema(description = "会话ID")
    private String token;

    @Schema(description = "所属企业ID")
    private String instituteId;

    @Schema(description = "登录IP")
    private String loginIp;

    @Schema(description = "登录地点")
    private String loginAddress;

    @Schema(description = "设备序号")
    private String deviceSn;

    @Schema(description = "应用平台ID")
    private String appModuleId;

    @Schema(description = "操作端版本")
    private String appVersion;

    @Schema(description = "操作端KEY")
    private String appKey;

    @Schema(description = "登录状态（080）")
    private String loginStatus;

    @Schema(description = "登录时间起始")
    private Date loginTimeStart;

    @Schema(description = "登录时间终止")
    private Date loginTimeEnd;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId == null ? null : userId.trim();
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token == null ? null : token.trim();
    }

    public String getInstituteId() {
        return instituteId;
    }

    public void setInstituteId(String instituteId) {
        this.instituteId = instituteId == null ? null : instituteId.trim();
    }

    public String getLoginIp() {
        return loginIp;
    }

    public void setLoginIp(String loginIp) {
        this.loginIp = loginIp == null ? null : loginIp.trim();
    }

    public String getLoginAddress() {
        return loginAddress;
    }

    public void setLoginAddress(String loginAddress) {
        this.loginAddress = loginAddress == null ? null : loginAddress.trim();
    }

    public String getDeviceSn() {
        return deviceSn;
    }

    public void setDeviceSn(String deviceSn) {
        this.deviceSn = deviceSn == null ? null : deviceSn.trim();
    }

    public String getAppModuleId() {
        return appModuleId;
    }

    public void setAppModuleId(String appModuleId) {
        this.appModuleId = appModuleId == null ? null : appModuleId.trim();
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion == null ? null : appVersion.trim();
    }

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey == null ? null : appKey.trim();
    }

    public String getLoginStatus() {
        return loginStatus;
    }

    public void setLoginStatus(String loginStatus) {
        this.loginStatus = loginStatus == null ? null : loginStatus.trim();
    }

    public Date getLoginTimeStart() {
        return loginTimeStart;
    }

    public void setLoginTimeStart(Date loginTimeStart) {
        this.loginTimeStart = loginTimeStart;
    }

    public Date getLoginTimeEnd() {
        return loginTimeEnd;
    }

    public void setLoginTimeEnd(Date loginTimeEnd) {
        this.loginTimeEnd = loginTimeEnd;
    }
}