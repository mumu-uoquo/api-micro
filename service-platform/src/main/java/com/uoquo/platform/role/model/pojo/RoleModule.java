package com.uoquo.platform.role.model.pojo;

/**
 * Table: bko_role_module
 */
public class RoleModule {
    /**
     * Column: id
     * Type: VARCHAR(32)
     * Remark: ID
     */
    private String id;

    /**
     * Column: role_id
     * Type: VARCHAR(32)
     * Remark: 角色ID
     */
    private String roleId;

    /**
     * Column: module_id
     * Type: VARCHAR(32)
     * Remark: 模块ID
     */
    private String moduleId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId == null ? null : roleId.trim();
    }

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId == null ? null : moduleId.trim();
    }
}