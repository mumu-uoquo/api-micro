package com.uoquo.platform.system.model.dto;

import java.util.Date;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 出参：当前授权详情<br/>
 * 数据来源：数据库最新（当前生效）的授权记录 sys_license_record。
 */
@Schema(description = "授权详情")
public class LicenseInfoDto {

    @Schema(description = "序列号")
    private String serialNo;

    @Schema(description = "拥有人")
    private String owner;

    @Schema(description = "授权时间")
    private Date authorizedTime;

    @Schema(description = "过期时间，NULL 表示永久有效")
    private Date expireTime;

    @Schema(description = "授权开始日期")
    private Date validFrom;

    @Schema(description = "授权结束日期，NULL 表示永久有效")
    private Date validExpire;

    @Schema(description = "系统版本")
    private String systemVersion;

    public String getSerialNo() {
        return serialNo;
    }

    public void setSerialNo(String serialNo) {
        this.serialNo = serialNo;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public Date getAuthorizedTime() {
        return authorizedTime;
    }

    public void setAuthorizedTime(Date authorizedTime) {
        this.authorizedTime = authorizedTime;
    }

    public Date getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(Date expireTime) {
        this.expireTime = expireTime;
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

    public String getSystemVersion() {
        return systemVersion;
    }

    public void setSystemVersion(String systemVersion) {
        this.systemVersion = systemVersion;
    }
}
