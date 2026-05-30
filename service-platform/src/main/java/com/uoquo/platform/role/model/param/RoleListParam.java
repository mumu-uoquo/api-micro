package com.uoquo.platform.role.model.param;

import com.uoquo.web.param.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;


/**
 * 入参：角色查询
 */
@Schema(description = "角色查询")
public class RoleListParam extends PageRequest {

    @Schema(description = "所属机构")
    private String instituteId;

    @Schema(description = "角色名称")
    private String roleName;

    @Schema(description = "作用范围")
    private String roleType;

    @Schema(description = "角色分组")
    private String roleGroup;

    @Schema(description = "角色等级")
    private Integer roleGrade;

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
}
