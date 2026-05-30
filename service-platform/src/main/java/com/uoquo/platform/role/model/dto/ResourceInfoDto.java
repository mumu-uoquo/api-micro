package com.uoquo.platform.role.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;


/**
 * 出参：资源信息
 */
@Schema(description = "资源信息")
public class ResourceInfoDto {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED)
    private String id;

    @Schema(description = "关联表ID")
    private String relateId;

    @Schema(description = "资源名称")
    private String resourceName;

    @Schema(description = "资源URL")
    private String resourceUrl;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    public String getRelateId() {
        return relateId;
    }

    public void setRelateId(String relateId) {
        this.relateId = relateId;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName == null ? null : resourceName.trim();
    }

    public String getResourceUrl() {
        return resourceUrl;
    }

    public void setResourceUrl(String resourceUrl) {
        this.resourceUrl = resourceUrl == null ? null : resourceUrl.trim();
    }

}