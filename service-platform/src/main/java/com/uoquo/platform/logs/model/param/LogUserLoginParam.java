package com.uoquo.platform.logs.model.param;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import java.util.Date;
import java.util.Map;

/**
 * 入参：登录日志<br>
 * 备注：该入参为内部使用，因此ID采用对应的消息ID，方便追踪查找
 * @author uoquo
 */
@Schema(description = "入参：登录日志")
public class LogUserLoginParam {

    @Schema(description = "事件ID（消息ID）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "id 不能为空")
    private String id;

    @Schema(description = "会话ID")
    private String token;

    @Schema(description = "请求追踪ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "traceId不能为空")
    private String traceId;

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "用户名", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "用户名不能为空")
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

    @Schema(description = "登录状态（同响应码）")
    private String loginStatus;

    @Schema(description = "登录时间")
    private Date loginTime;

    @Schema(description = "登录参数")
    private Map<String, ?> loginParam;

    @Schema(description = "登录模式")
    private String loginMode;

    @Schema(description = "备注")
    private String description;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getAppModuleName() {
        return appModuleName;
    }

    public void setAppModuleName(String appModuleName) {
        this.appModuleName = appModuleName == null ? null : appModuleName.trim();
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

    public Date getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(Date loginTime) {
        this.loginTime = loginTime;
    }

    public Map<String, ?> getLoginParam() {
        return loginParam;
    }

    public void setLoginParam(Map<String, ?> loginParam) {
        this.loginParam = loginParam;
    }

    public String getLoginMode() {
        return loginMode;
    }

    public void setLoginMode(String loginMode) {
        this.loginMode = loginMode == null ? null : loginMode.trim();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description == null ? null : description.trim();
    }
}