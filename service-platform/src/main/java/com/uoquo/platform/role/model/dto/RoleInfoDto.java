package com.uoquo.platform.role.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 出参：角色信息
 */
@Schema(description = "角色信息")
public class RoleInfoDto {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED)
    private String id;

    @Schema(description = "所属机构")
    private String instituteId;

    @Schema(description = "所属机构名称")
    private String instituteName;

    @Schema(description = "角色名字", requiredMode = Schema.RequiredMode.REQUIRED)
    private String roleName;

    @Schema(description = "作用范围（003）")
    private String roleType;

    @Schema(description = "授权分组（004）")
    private String roleGroup;

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

    public String getInstituteId() {
        return instituteId;
    }

    public void setInstituteId(String instituteId) {
        this.instituteId = instituteId;
    }

    public String getInstituteName() {
        return instituteName;
    }

    public void setInstituteName(String instituteName) {
        this.instituteName = instituteName;
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

    public String getRoleGroup() {
        return roleGroup;
    }

    public void setRoleGroup(String roleGroup) {
        this.roleGroup = roleGroup;
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
