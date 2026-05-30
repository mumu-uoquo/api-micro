package com.uoquo.platform.user.model.pojo;

/**
 * Table: bko_user_role
 */
public class UserRole {
    /**
     * Column: id
     * Type: VARCHAR(32)
     * Remark: ID
     */
    private String id;

    /**
     * Column: user_id
     * Type: VARCHAR(32)
     * Remark: 用户ID
     */
    private String userId;

    /**
     * Column: role_id
     * Type: VARCHAR(32)
     * Remark: 角色ID
     */
    private String roleId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId == null ? null : userId.trim();
    }

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId == null ? null : roleId.trim();
    }
}