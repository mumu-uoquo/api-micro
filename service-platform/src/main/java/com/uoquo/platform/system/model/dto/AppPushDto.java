package com.uoquo.platform.system.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Date;

/**
 * 出参：接入授权推送信息
 */
@Schema(description = "接入授权推送")
public class AppPushDto {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED)
    private String id;

    @Schema(description = "接入信息ID")
    private String appId;

    @Schema(description = "数据来源（030）")
    private String dataSource;

    @Schema(description = "数据范围（031）")
    private String dataScope;

    @Schema(description = "数据类型集合（009）")
    private String dataBizType;

    @Schema(description = "推送方式（032）")
    private String pushMode;

    @Schema(description = "推送实现（标准、定制）")
    private String pushImpl;

    @Schema(description = "推送目标")
    private String targetHost;

    @Schema(description = "目标地址")
    private String targetAddress;

    @Schema(description = "三方授权")
    private String targetAppkey;

    @Schema(description = "三方秘钥")
    private String targetSecret;

    @Schema(description = "压缩方式")
    private String compressType;

    @Schema(description = "推送速率（条/秒）")
    private Integer pushRateLimit;

    @Schema(description = "失败处理")
    private String failHandling;

    @Schema(description = "可用状态（001）")
    private String status;

    @Schema(description = "状态时间")
    private Date statusTime;

    @Schema(description = "状态备注")
    private String statusMemo;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "更新时间")
    private Date updateTime;

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

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}
