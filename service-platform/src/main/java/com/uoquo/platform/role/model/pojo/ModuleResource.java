package com.uoquo.platform.role.model.pojo;

/**
 * Table: bko_module_resource
 */
public class ModuleResource {
    /**
     * Column: id
     * Type: VARCHAR(32)
     * Remark: ID
     */
    private String id;

    /**
     * Column: module_id
     * Type: VARCHAR(32)
     * Remark: 模块ID
     */
    private String moduleId;

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

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId == null ? null : moduleId.trim();
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId == null ? null : resourceId.trim();
    }
}