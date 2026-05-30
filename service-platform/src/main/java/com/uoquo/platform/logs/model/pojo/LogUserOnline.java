package com.uoquo.platform.logs.model.pojo;

import java.util.Date;

/**
 * Table: log_user_online
 */
public class LogUserOnline {
    /**
     * Column: id
     * Type: VARCHAR(32)
     * Remark: 日志ID（ULID）
     */
    private String id;

    /**
     * Column: token
     * Type: VARCHAR(64)
     * Remark: 会话ID（access_token）
     */
    private String token;

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
     * Column: login_time
     * Type: DATETIME
     * Remark: 最近登录/token刷新时间
     */
    private Date loginTime;

    /**
     * Column: create_time
     * Type: DATETIME
     * Remark: 首次登录时间（不随upsert更新）
     */
    private Date createTime;

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

    public Date getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(Date loginTime) {
        this.loginTime = loginTime;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description == null ? null : description.trim();
    }
}
