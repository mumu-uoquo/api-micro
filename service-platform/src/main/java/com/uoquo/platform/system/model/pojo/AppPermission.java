package com.uoquo.platform.system.model.pojo;

/**
 * Table: bko_app_permission
 */
public class AppPermission {
    /**
     * Column: id
     * Type: VARCHAR(32)
     * Remark: ID
     */
    private String id;

    /**
     * Column: app_id
     * Type: VARCHAR(32)
     * Remark: 接入ID
     */
    private String appId;

    /**
     * Column: resource_id
     * Type: VARCHAR(32)
     * Remark: 资源ID
     */
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