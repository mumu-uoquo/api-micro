package com.uoquo.platform.logs.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Date;
import java.util.Map;

/**
 * 出参：用户登录记录
 */
@Schema(description = "用户登录记录")
public class LogUserLoginDto {

    @Schema(description = "记录id", requiredMode = Schema.RequiredMode.REQUIRED)
    private String id;

    @Schema(description = "会话ID")
    private String token;

    @Schema(description = "请求追踪ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String traceId;

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "用户名")
    private String userName;

    @Schema(description = "所属企业ID")
    private String instituteId;

    @Schema(description = "登录IP")
    private String loginIp;

    @Schema(description = "登录地点")
    private String loginAddress;

    @Schema(description = "登录设备")
    private String deviceSn;

    @Schema(description = "设备系统")
    private String deviceOs;

    @Schema(description = "UserAgent信息")
    private String deviceUa;

    @Schema(description = "应用平台ID")
    private String appModuleId;

    @Schema(description = "应用平台名称")
    private String appModuleName;

    @Schema(description = "操作端版本")
    private String appVersion;

    @Schema(description = "操作端KEY")
    private String appKey;

    @Schema(description = "操作端名称")
    private String appName;

    @Schema(description = "登录状态（同响应码）")
    private String loginStatus;

    @Schema(description = "登录参数")
    private Map<String, Object> loginParam;

    @Schema(description = "登录模式")
    private String loginMode;

    @Schema(description = "登录时间")
    private Date loginTime;

    @Schema(description = "退出状态（同响应码）")
    private String logoutStatus;

    @Schema(description = "退出信息")
    private String logoutDesc;

    @Schema(description = "退出时间")
    private Date logoutTime;

    @Schema(description = "备注")
    private String description;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token == null ? null : token.trim();
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId == null ? null : traceId.trim();
    }

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
        this.userName = userName == null ? null : userName.trim();
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

    public String getDeviceOs() {
        return deviceOs;
    }

    public void setDeviceOs(String deviceOs) {
        this.deviceOs = deviceOs == null ? null : deviceOs.trim();
    }

    public String getDeviceUa() {
        return deviceUa;
    }

    public void setDeviceUa(String deviceUa) {
        this.deviceUa = deviceUa == null ? null : deviceUa.trim();
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

    public String getAppModuleName() {
        return appModuleName;
    }

    public void setAppModuleName(String appModuleName) {
        this.appModuleName = appModuleName == null ? null : appModuleName.trim();
    }

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey == null ? null : appKey.trim();
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getLoginStatus() {
        return loginStatus;
    }

    public void setLoginStatus(String loginStatus) {
        this.loginStatus = loginStatus == null ? null : loginStatus.trim();
    }

    public Map<String, Object> getLoginParam() {
        return loginParam;
    }

    public void setLoginParam(Map<String, Object> loginParam) {
        this.loginParam = loginParam;
    }

    public String getLoginMode() {
        return loginMode;
    }

    public void setLoginMode(String loginMode) {
        this.loginMode = loginMode == null ? null : loginMode.trim();
    }

    public Date getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(Date loginTime) {
        this.loginTime = loginTime;
    }

    public String getLogoutStatus() {
        return logoutStatus;
    }

    public void setLogoutStatus(String logoutStatus) {
        this.logoutStatus = logoutStatus == null ? null : logoutStatus.trim();
    }

    public String getLogoutDesc() {
        return logoutDesc;
    }

    public void setLogoutDesc(String logoutDesc) {
        this.logoutDesc = logoutDesc == null ? null : logoutDesc.trim();
    }

    public Date getLogoutTime() {
        return logoutTime;
    }

    public void setLogoutTime(Date logoutTime) {
        this.logoutTime = logoutTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description == null ? null : description.trim();
    }
}