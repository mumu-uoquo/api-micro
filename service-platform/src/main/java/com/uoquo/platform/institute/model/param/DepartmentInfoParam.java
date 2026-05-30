package com.uoquo.platform.institute.model.param;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;

/**
 * 入参：部门信息
 */
@Schema(description = "部门信息")
public class DepartmentInfoParam {

    @Schema(description = "id")
    private String id;

    @Schema(description = "父级ID")
    private String parentId;

    @Schema(description = "所属企业")
    @NotBlank
    private String instituteId;

    @Schema(description = "所属区域")
    private String areaId;

    @Schema(description = "部门名称")
    @NotBlank
    private String deptName;

    @Schema(description = "部门编码")
    private String deptCode;

    @Schema(description = "详细地址")
    private String address;

    @Schema(description = "备注")
    private String description;

    @Schema(description = "默认标识")
    private Boolean defaulted;

    @Schema(description = "三方ID（数据同步）")
    private String thirdId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId == null ? null : parentId.trim();
    }

    public String getInstituteId() {
        return instituteId;
    }

    public void setInstituteId(String instituteId) {
        this.instituteId = instituteId == null ? null : instituteId.trim();
    }

    public String getAreaId() {
        return areaId;
    }

    public void setAreaId(String areaId) {
        this.areaId = areaId == null ? null : areaId.trim();
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName == null ? null : deptName.trim();
    }

    public String getDeptCode() {
        return deptCode;
    }

    public void setDeptCode(String deptCode) {
        this.deptCode = deptCode == null ? null : deptCode.trim();
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address == null ? null : address.trim();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description == null ? null : description.trim();
    }

    public Boolean getDefaulted() {
        return defaulted;
    }

    public void setDefaulted(Boolean defaulted) {
        this.defaulted = defaulted;
    }

    public String getThirdId() {
        return thirdId;
    }

    public void setThirdId(String thirdId) {
        this.thirdId = thirdId == null ? null : thirdId.trim();
    }

}