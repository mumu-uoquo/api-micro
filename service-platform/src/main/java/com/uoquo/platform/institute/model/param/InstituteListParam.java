package com.uoquo.platform.institute.model.param;

import com.uoquo.web.param.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;


/**
 * 入参：企业查询
 */
@Schema(description = "企业查询")
public class InstituteListParam extends PageRequest {

    @Schema(description = "父级ID")
    private String parentId;

    @Schema(description = "企业名称")
    private String instituteName;

    @Schema(description = "企业类型（020）")
    private String instituteType;

    @Schema(description = "授权分组（004）")
    private String roleGroup;

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getInstituteName() {
        return instituteName;
    }

    public void setInstituteName(String instituteName) {
        this.instituteName = instituteName;
    }

    public String getInstituteType() {
        return instituteType;
    }

    public void setInstituteType(String instituteType) {
        this.instituteType = instituteType;
    }

    public String getRoleGroup() {
        return roleGroup;
    }

    public void setRoleGroup(String roleGroup) {
        this.roleGroup = roleGroup;
    }
}
