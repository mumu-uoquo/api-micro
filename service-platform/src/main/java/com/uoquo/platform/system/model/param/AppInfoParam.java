package com.uoquo.platform.system.model.param;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;

/**
 * 入参：接入授权信息（新增、删除）
 */
@Schema(description = "接入授权")
public class AppInfoParam {
    @Schema(description = "主键")
    private String id;

    @Schema(description = "所属机构")
    private String instituteId;

    @Schema(description = "名称")
    @NotBlank(message = "名称不能为空")
    private String appName;

    @Schema(description = "appkey")
    private String appkey;

    @Schema(description = "secret")
    private String secret;

    @Schema(description = "可信站点")
    private String trustSite;

    @Schema(description = "可信IP")
    private String trustIps;

    @Schema(description = "模板类型")
    private String templateType;

    @Schema(description = "授权根模块")
    private String moduleId;

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

    public String getTrustIps() {
        return trustIps;
    }

    public void setTrustIps(String trustIps) {
        this.trustIps = trustIps == null ? null : trustIps.trim();
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

}