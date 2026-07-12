package com.uoquo.platform.system.model.dto;

import com.uoquo.annotation.json.Sensitive;
import com.uoquo.annotation.json.SensitiveType;

import io.swagger.v3.oas.annotations.media.Schema;


import java.util.Date;

/**
 * 出参：接入授权信息
 */
@Schema(description = "接入授权")
public class AppInfoDto {
    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED)
    private String id;

    @Schema(description = "所属机构ID")
    private String instituteId;
    @Schema(description = "所属机构名称")
    private String instituteName;

    @Schema(description = "名称")
    private String appName;

    @Schema(description = "appkey")
    @Sensitive(type = SensitiveType.CRYPT_AES)
    private String appkey;

    @Schema(description = "secret")
    @Sensitive(type = SensitiveType.CRYPT_AES)
    private String secret;

    @Schema(description = "可信站点")
    private String trustSite;

    @Schema(description = "模板类型（006）")
    private String templateType;

    @Schema(description = "授权根模块（平台）")
    private String moduleId;

    @Schema(description = "授权根模块（平台）名称")
    private String moduleName;

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

    public String getInstituteId() {
        return instituteId;
    }

    public void setInstituteId(String instituteId) {
        this.instituteId = instituteId == null ? null : instituteId.trim();
    }

    public String getInstituteName() {
        return instituteName;
    }

    public void setInstituteName(String instituteName) {
        this.instituteName = instituteName;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName == null ? null : appName.trim();
    }

    public String getAppkey() {
        return appkey;
    }

    public void setAppkey(String appkey) {
        this.appkey = appkey == null ? null : appkey.trim();
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret == null ? null : secret.trim();
    }

    public String getTrustSite() {
        return trustSite;
    }

    public void setTrustSite(String trustSite) {
        this.trustSite = trustSite == null ? null : trustSite.trim();
    }

    public String getTemplateType() {
        return templateType;
    }

    public void setTemplateType(String templateType) {
        this.templateType = templateType;
    }

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId == null ? null : moduleId.trim();
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
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