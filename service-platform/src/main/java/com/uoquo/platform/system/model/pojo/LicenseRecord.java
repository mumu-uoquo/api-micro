package com.uoquo.platform.system.model.pojo;

import java.util.Date;

import com.uoquo.mybatis.sensitive.SensitiveData;
import com.uoquo.mybatis.sensitive.SensitiveField;

/**
 * Table: sys_license_record
 * MyBatis 拦截器检测 @SensitiveData 类中的 @SensitiveField 字段自动加解密
 */
@SensitiveData
public class LicenseRecord {

    /**
     * Column: id
     * Type: VARCHAR(32)
     * Remark: ID
     */
    private String id;

    /**
     * Column: license_info
     * Type: JSON
     * Remark: 授权快照
     */
    private String licenseInfo;

    /**
     * Column: system_version
     * Type: VARCHAR(32)
     * Remark: 系统版本
     */
    private String systemVersion;

    /**
     * Column: serial_no
     * Type: VARCHAR(64)
     * Remark: 序列号
     */
    private String serialNo;

    /**
     * Column: activation_code
     * Type: VARCHAR(128)
     * Remark: 激活码（AES加密存储）
     */
    @SensitiveField
    private String activationCode;

    /**
     * Column: valid_from
     * Type: DATE
     * Remark: 授权开始日期
     */
    private Date validFrom;

    /**
     * Column: valid_expire
     * Type: DATE
     * Remark: 授权结束日期
     */
    private Date validExpire;

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
     * Column: is_current
     * Type: BOOLEAN
     * Remark: 是否是生效授权
     */
    private Boolean isCurrent;

    /**
     * Column: import_result
     * Type: BOOLEAN
     * Remark: 导入结果
     */
    private Boolean importResult;

    /**
     * Column: fail_reason
     * Type: VARCHAR(512)
     * Remark: 失败原因
     */
    private String failReason;

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
        this.systemVersion = systemVersion == null ? null : systemVersion.trim();
    }

    public String getSerialNo() {
        return serialNo;
    }

    public void setSerialNo(String serialNo) {
        this.serialNo = serialNo == null ? null : serialNo.trim();
    }

    public String getActivationCode() {
        return activationCode;
    }

    public void setActivationCode(String activationCode) {
        this.activationCode = activationCode == null ? null : activationCode.trim();
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
        this.createUser = createUser == null ? null : createUser.trim();
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
        this.failReason = failReason == null ? null : failReason.trim();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description == null ? null : description.trim();
    }
}
