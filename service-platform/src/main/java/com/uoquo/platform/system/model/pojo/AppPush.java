package com.uoquo.platform.system.model.pojo;

import java.util.Date;

/**
 * Table: bko_app_push
 */
public class AppPush {
    /**
     * Column: id
     * Type: VARCHAR(32)
     * Remark: 主键
     */
    private String id;

    /**
     * Column: app_id
     * Type: VARCHAR(50)
     * Remark: 接入信息ID
     */
    private String appId;

    /**
     * Column: data_source
     * Type: CHAR(6)
     * Default: 030099
     * Remark: 数据来源（030）
     */
    private String dataSource;

    /**
     * Column: data_scope
     * Type: CHAR(6)
     * Default: 031001
     * Remark: 数据范围（031）
     */
    private String dataScope;

    /**
     * Column: data_biz_type
     * Type: VARCHAR(128)
     * Remark: 数据类型集合（009）
     */
    private String dataBizType;

    /**
     * Column: push_mode
     * Type: CHAR(6)
     * Default: 032001
     * Remark: 推送方式（032）
     */
    private String pushMode;

    /**
     * Column: push_impl
     * Type: VARCHAR(10)
     * Default: standard
     * Remark: 推送实现（标准、定制）
     */
    private String pushImpl;

    /**
     * Column: target_host
     * Type: VARCHAR(64)
     * Remark: 推送目标
     */
    private String targetHost;

    /**
     * Column: target_address
     * Type: VARCHAR(100)
     * Remark: 目标地址
     */
    private String targetAddress;

    /**
     * Column: target_appkey
     * Type: VARCHAR(64)
     * Remark: 三方授权
     */
    private String targetAppkey;

    /**
     * Column: target_secret
     * Type: VARCHAR(64)
     * Remark: 三方秘钥
     */
    private String targetSecret;

    /**
     * Column: compress_type
     * Type: VARCHAR(10)
     * Default: none
     * Remark: 压缩方式
     */
    private String compressType;

    /**
     * Column: push_rate_limit
     * Type: INT
     * Default: 0
     * Remark: 推送速率（条/秒）
     */
    private Integer pushRateLimit;

    /**
     * Column: fail_handling
     * Type: CHAR(6)
     * Remark: 失败处理
     */
    private String failHandling;

    /**
     * Column: status
     * Type: CHAR(6)
     * Default: 001001
     * Remark: 可用状态（001）
     */
    private String status;

    /**
     * Column: status_time
     * Type: DATETIME
     * Remark: 状态时间
     */
    private Date statusTime;

    /**
     * Column: status_memo
     * Type: VARCHAR(100)
     * Remark: 状态备注
     */
    private String statusMemo;

    /**
     * Column: create_user
     * Type: VARCHAR(32)
     * Remark: 创建人
     */
    private String createUser;

    /**
     * Column: create_time
     * Type: DATETIME
     * Remark: 创建时间
     */
    private Date createTime;

    /**
     * Column: update_user
     * Type: VARCHAR(32)
     * Remark: 更新人
     */
    private String updateUser;

    /**
     * Column: update_time
     * Type: DATETIME
     * Remark: 更新时间
     */
    private Date updateTime;

    /**
     * Column: delete_state
     * Type: BIGINT
     * Default: 0
     * Remark: 删除标识
     */
    private Long deleteState;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId == null ? null : appId.trim();
    }

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource == null ? null : dataSource.trim();
    }

    public String getDataScope() {
        return dataScope;
    }

    public void setDataScope(String dataScope) {
        this.dataScope = dataScope == null ? null : dataScope.trim();
    }

    public String getDataBizType() {
        return dataBizType;
    }

    public void setDataBizType(String dataBizType) {
        this.dataBizType = dataBizType == null ? null : dataBizType.trim();
    }

    public String getPushMode() {
        return pushMode;
    }

    public void setPushMode(String pushMode) {
        this.pushMode = pushMode == null ? null : pushMode.trim();
    }

    public String getPushImpl() {
        return pushImpl;
    }

    public void setPushImpl(String pushImpl) {
        this.pushImpl = pushImpl == null ? null : pushImpl.trim();
    }

    public String getTargetHost() {
        return targetHost;
    }

    public void setTargetHost(String targetHost) {
        this.targetHost = targetHost == null ? null : targetHost.trim();
    }

    public String getTargetAddress() {
        return targetAddress;
    }

    public void setTargetAddress(String targetAddress) {
        this.targetAddress = targetAddress == null ? null : targetAddress.trim();
    }

    public String getTargetAppkey() {
        return targetAppkey;
    }

    public void setTargetAppkey(String targetAppkey) {
        this.targetAppkey = targetAppkey == null ? null : targetAppkey.trim();
    }

    public String getTargetSecret() {
        return targetSecret;
    }

    public void setTargetSecret(String targetSecret) {
        this.targetSecret = targetSecret == null ? null : targetSecret.trim();
    }

    public String getCompressType() {
        return compressType;
    }

    public void setCompressType(String compressType) {
        this.compressType = compressType == null ? null : compressType.trim();
    }

    public Integer getPushRateLimit() {
        return pushRateLimit;
    }

    public void setPushRateLimit(Integer pushRateLimit) {
        this.pushRateLimit = pushRateLimit;
    }

    public String getFailHandling() {
        return failHandling;
    }

    public void setFailHandling(String failHandling) {
        this.failHandling = failHandling == null ? null : failHandling.trim();
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status == null ? null : status.trim();
    }

    public Date getStatusTime() {
        return statusTime;
    }

    public void setStatusTime(Date statusTime) {
        this.statusTime = statusTime;
    }

    public String getStatusMemo() {
        return statusMemo;
    }

    public void setStatusMemo(String statusMemo) {
        this.statusMemo = statusMemo == null ? null : statusMemo.trim();
    }

    public String getCreateUser() {
        return createUser;
    }

    public void setCreateUser(String createUser) {
        this.createUser = createUser == null ? null : createUser.trim();
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getUpdateUser() {
        return updateUser;
    }

    public void setUpdateUser(String updateUser) {
        this.updateUser = updateUser == null ? null : updateUser.trim();
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Long getDeleteState() {
        return deleteState;
    }

    public void setDeleteState(Long deleteState) {
        this.deleteState = deleteState;
    }
}
