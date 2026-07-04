package com.uoquo.platform.logs.model.pojo;

import java.util.Date;
import java.util.Map;

/**
 * Table: log_user_login
 */
public class LogUserLogin {
    /**
     * Column: id
     * Type: VARCHAR(32)
     * Remark: 日志ID
     */
    private String id;

    /**
     * Column: token
     * Type: VARCHAR(32)
     * Remark: 会话ID
     */
    private String token;

    /**
     * Column: trace_id
     * Type: VARCHAR(32)
     * Remark: 请求追踪ID
     */
    private String traceId;

    /**
     * Column: user_id
     * Type: VARCHAR(32)
     * Remark: 用户ID
     */
    private String userId;

    /**
     * Column: user_name
     * Type: VARCHAR(50)
     * Remark: 用户名
     */
    private String userName;

    /**
     * Column: institute_id
     * Type: VARCHAR(32)
     * Remark: 所属企业ID
     */
    private String instituteId;

    /**
     * Column: login_ip
     * Type: VARCHAR(50)
     * Remark: 登录IP
     */
    private String loginIp;

    /**
     * Column: login_address
     * Type: VARCHAR(100)
     * Remark: 登录地点
     */
    private String loginAddress;

    /**
     * Column: device_sn
     * Type: VARCHAR(100)
     * Remark: 设备序号
     */
    private String deviceSn;

    /**
     * Column: device_os
     * Type: VARCHAR(100)
     * Remark: 设备系统
     */
    private String deviceOs;

    /**
     * Column: device_ua
     * Type: VARCHAR(200)
     * Remark: UserAgent信息
     */
    private String deviceUa;

    /**
     * Column: app_module_id
     * Type: VARCHAR(32)
     * Remark: 应用平台ID
     */
    private String appModuleId;

    /**
     * Column: app_module_name
     * Type: VARCHAR(50)
     * Remark: 应用平台名称
     */
    private String appModuleName;

    /**
     * Column: app_version
     * Type: VARCHAR(20)
     * Remark: 操作端版本
     */
    private String appVersion;

    /**
     * Column: app_key
     * Type: VARCHAR(32)
     * Remark: 操作端KEY
     */
    private String appKey;

    /**
     * Column: app_name
     * Type: VARCHAR(50)
     * Remark: 操作端名称
     */
    private String appName;

    /**
     * Column: login_status
     * Type: CHAR(6)
     * Remark: 登录状态（同响应码）
     */
    private String loginStatus;

    /**
     * Column: login_time
     * Type: DATETIME
     * Remark: 登录时间
     */
    private Date loginTime;

    /**
     * Column: login_param
     * Type: JSON(0)
     * Remark: 登录参数
     */
    private Map<String, Object> loginParam;

    /**
     * Column: login_mode
     * Type: VARCHAR(50)
     * Remark: 登录模式
     */
    private String loginMode;

    /**
     * Column: logout_status
     * Type: CHAR(6)
     * Remark: 退出状态（同响应码）
     */
    private String logoutStatus;

    /**
     * Column: logout_time
     * Type: DATETIME
     * Remark: 退出时间
     */
    private Date logoutTime;

    /**
     * Column: logout_desc
     * Type: VARCHAR(200)
     * Remark: 退出信息
     */
    private String logoutDesc;

    /**
     * Column: description
     * Type: VARCHAR(200)
     * Remark: 备注
     */
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

    public Date getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(Date loginTime) {
        this.loginTime = loginTime;
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

    public String getLogoutStatus() {
        return logoutStatus;
    }

    public void setLogoutStatus(String logoutStatus) {
        this.logoutStatus = logoutStatus == null ? null : logoutStatus.trim();
    }

    public Date getLogoutTime() {
        return logoutTime;
    }

    public void setLogoutTime(Date logoutTime) {
        this.logoutTime = logoutTime;
    }

    public String getLogoutDesc() {
        return logoutDesc;
    }

    public void setLogoutDesc(String logoutDesc) {
        this.logoutDesc = logoutDesc == null ? null : logoutDesc.trim();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description == null ? null : description.trim();
    }
}