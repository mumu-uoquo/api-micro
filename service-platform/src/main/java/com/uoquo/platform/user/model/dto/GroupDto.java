package com.uoquo.platform.user.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;


/**
 * 出参：分组信息
 */
@Schema(description = "分组信息")
public class GroupDto {

    @Schema(description = "分组id", requiredMode = Schema.RequiredMode.REQUIRED)
    private String id;

    @Schema(description = "用户所属机构id")
    private String instituteId;

    @Schema(description = "用户所属机构名称")
    private String instituteName;

    @Schema(description = "所属部门")
    private String deptId;

    @Schema(description = "所属部门名称")
    private String deptName;

    @Schema(description = "分组名字", requiredMode = Schema.RequiredMode.REQUIRED)
    private String groupName;

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

    public String getDeptId() {
        return deptId;
    }

    public void setDeptId(String deptId) {
        this.deptId = deptId;
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
}
