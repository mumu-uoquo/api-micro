package com.uoquo.platform.system.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 出参：AppInfo的授权资源
 */
@Schema(description = "AppInfo的授权资源")
public class AppPermissionDto {

    @Schema(description = "主键")
    private String id;

    @Schema(description = "接入ID")
    private String appId;

    @Schema(description = "资源ID")
    private String resourceId;

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

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId == null ? null : resourceId.trim();
    }
}