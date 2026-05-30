package com.uoquo.platform.user.model.param;

import com.uoquo.web.param.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;


/**
 * 入参：用户列表查询
 */
@Schema(description = "用户列表查询")
public class GroupListParam extends PageRequest {

    @Schema(description = "父机构id")
    private String instituteParentId;

    @Schema(description = "机构id")
    private String instituteId;

    @Schema(description = "所属部门")
    private String deptId;

    @Schema(description = "组姓名")
    private String groupName;

    public String getInstituteId() {
        return instituteId;
    }

    public void setInstituteId(String instituteId) {
        this.instituteId = instituteId;
    }

    public String getInstituteParentId() {
        return instituteParentId;
    }

    public void setInstituteParentId(String instituteParentId) {
        this.instituteParentId = instituteParentId;
    }

    public String getDeptId() {
        return deptId;
    }

    public void setDeptId(String deptId) {
        this.deptId = deptId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

}
