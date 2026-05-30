package com.uoquo.platform.institute.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * 出参：机构树状信息
 */
@Schema(description = "机构树状信息")
public class InstituteTreeDto {

    @Schema(description = "id", requiredMode = Schema.RequiredMode.REQUIRED)
    private String id;

    @Schema(description = "父级ID")
    private String parentId;

    @Schema(description = "企业名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String instituteName;

    @Schema(description = "企业简称")
    private String shortName;

    @Schema(description = "企业类型（020）")
    private String instituteType;

    @Schema(description = "子机构信息")
    private List<InstituteTreeDto> children;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getInstituteType() {
        return instituteType;
    }

    public void setInstituteType(String instituteType) {
        this.instituteType = instituteType;
    }

    public List<InstituteTreeDto> getChildren() {
        return children;
    }

    public void setChildren(List<InstituteTreeDto> children) {
        this.children = children;
    }
}
