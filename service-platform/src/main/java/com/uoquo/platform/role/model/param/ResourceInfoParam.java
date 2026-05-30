package com.uoquo.platform.role.model.param;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;


/**
 * 入参：添加/修改资源
 */
@Schema(description = "添加/修改资源")
public class ResourceInfoParam {

    @Schema(description = "主键")
    private String id;

    @Schema(description = "关联模块/资源ID")
    private String relateId;

    @NotBlank
    @Schema(description = "资源名称")
    private String resourceName;

    @NotBlank
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