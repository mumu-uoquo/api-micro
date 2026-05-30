package com.uoquo.platform.role.model.param;

import io.swagger.v3.oas.annotations.media.Schema;


import jakarta.validation.constraints.NotBlank;

/**
 * 入参：新增/修改角色
 */
@Schema(description = "新增/修改角色")
public class RoleInfoParam {

    @Schema(description = "主键")
    private String id;

    @Schema(description = "复制来源")
    private String fromRoleId;

    @Schema(description = "机构id")
    private String instituteId;

    @NotBlank
    @Schema(description = "角色名称")
    private String roleName;

    @Schema(description = "作用范围（003）")
    private String roleType;

    @Schema(description = "角色等级（越小越高）")
    private Integer roleGrade;

    @Schema(description = "角色描述")
    private String description;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFromRoleId() {
        return fromRoleId;
    }

    public void setFromRoleId(String fromRoleId) {
        this.fromRoleId = fromRoleId;
    }

    public String getInstituteId() {
        return instituteId;
    }

    public void setInstituteId(String instituteId) {
        this.instituteId = instituteId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getRoleType() {
        return roleType;
    }

    public void setRoleType(String roleType) {
        this.roleType = roleType;
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
        this.description = description;
    }
}
