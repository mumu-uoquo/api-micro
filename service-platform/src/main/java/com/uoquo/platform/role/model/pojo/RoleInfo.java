package com.uoquo.platform.role.model.pojo;

import java.util.Date;

/**
 * Table: bko_role
 */
public class RoleInfo {
    /**
     * Column: id
     * Type: VARCHAR(32)
     * Remark: 主键
     */
    private String id;

    /**
     * Column: institute_id
     * Type: VARCHAR(32)
     * Remark: 所属机构
     */
    private String instituteId;

    /**
     * Column: role_name
     * Type: VARCHAR(50)
     * Remark: 角色名称
     */
    private String roleName;

    /**
     * Column: role_type
     * Type: CHAR(6)
     * Remark: 作用范围（003）
     */
    private String roleType;

    /**
     * Column: role_group
     * Type: VARCHAR(32)
     * Remark: 授权分组（004）
     */
    private String roleGroup;

    /**
     * Column: role_grade
     * Type: INT
     * Default value: 30
     * Remark: 角色等级（越小越高）
     */
    private Integer roleGrade;

    /**
     * Column: description
     * Type: VARCHAR(100)
     * Remark: 角色描述
     */
    private String description;

    /**
     * Column: create_user
     * Type: VARCHAR(32)
     * Remark: 创建人
     */
    private String createUser;

    /**
     * Column: create_time
     * Type: DATETIME
     * Remark: 创建时间
     */
    private Date createTime;

    /**
     * Column: update_user
     * Type: VARCHAR(32)
     * Remark: 更新人
     */
    private String updateUser;

    /**
     * Column: update_time
     * Type: DATETIME
     * Remark: 更新时间
     */
    private Date updateTime;

    /**
     * Column: delete_state
     * Type: BIGINT
     * Default value: 0
     * Remark: 删除标识
     */
    private Long deleteState;

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

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName == null ? null : roleName.trim();
    }

    public String getRoleType() {
        return roleType;
    }

    public void setRoleType(String roleType) {
        this.roleType = roleType == null ? null : roleType.trim();
    }

    public String getRoleGroup() {
        return roleGroup;
    }

    public void setRoleGroup(String roleGroup) {
        this.roleGroup = roleGroup == null ? null : roleGroup.trim();
    }

    public Integer getRoleGrade() {
        return roleGrade;
    }

    public void setRoleGrade(Integer roleGrade) {
        this.roleGrade = roleGrade;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description == null ? null : description.trim();
    }

    public String getCreateUser() {
        return createUser;
    }

    public void setCreateUser(String createUser) {
        this.createUser = createUser == null ? null : createUser.trim();
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getUpdateUser() {
        return updateUser;
    }

    public void setUpdateUser(String updateUser) {
        this.updateUser = updateUser == null ? null : updateUser.trim();
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Long getDeleteState() {
        return deleteState;
    }

    public void setDeleteState(Long deleteState) {
        this.deleteState = deleteState;
    }
}