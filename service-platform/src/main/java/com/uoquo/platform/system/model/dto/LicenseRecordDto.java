package com.uoquo.platform.system.model.dto;

import java.util.Date;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 出参：License导入记录
 */
@Schema(description = "License导入记录")
public class LicenseRecordDto {

    @Schema(description = "记录ID")
    private String id;

    @Schema(description = "授权快照（JSON）")
    private String licenseInfo;

    @Schema(description = "系统版本")
    private String systemVersion;

    @Schema(description = "序列号")
    private String serialNo;

    @Schema(description = "激活码")
    private String activationCode;

    @Schema(description = "授权开始日期")
    private Date validFrom;

    @Schema(description = "授权结束日期，NULL表示永久")
    private Date validExpire;

    @Schema(description = "创建人")
    private String createUser;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "是否为当前生效授权")
    private Boolean isCurrent;

    @Schema(description = "导入结果：true=成功 false=失败")
    private Boolean importResult;

    @Schema(description = "失败原因")
    private String failReason;

    @Schema(description = "备注")
    private String description;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLicenseInfo() {
        return licenseInfo;
    }

    public void setLicenseInfo(String licenseInfo) {
        this.licenseInfo = licenseInfo;
    }

    public String getSystemVersion() {
        return systemVersion;
    }

    public void setSystemVersion(String systemVersion) {
        this.systemVersion = systemVersion;
    }

    public String getSerialNo() {
        return serialNo;
    }

    public void setSerialNo(String serialNo) {
        this.serialNo = serialNo;
    }

    public String getActivationCode() {
        return activationCode;
    }

    public void setActivationCode(String activationCode) {
        this.activationCode = activationCode;
    }

    public Date getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(Date validFrom) {
        this.validFrom = validFrom;
    }

    public Date getValidExpire() {
        return validExpire;
    }

    public void setValidExpire(Date validExpire) {
        this.validExpire = validExpire;
    }

    public String getCreateUser() {
        return createUser;
    }

    public void setCreateUser(String createUser) {
        this.createUser = createUser;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Boolean getIsCurrent() {
        return isCurrent;
    }

    public void setIsCurrent(Boolean isCurrent) {
        this.isCurrent = isCurrent;
    }

    public Boolean getImportResult() {
        return importResult;
    }

    public void setImportResult(Boolean importResult) {
        this.importResult = importResult;
    }

    public String getFailReason() {
        return failReason;
    }

    public void setFailReason(String failReason) {
        this.failReason = failReason;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
