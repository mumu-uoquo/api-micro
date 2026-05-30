package com.uoquo.platform.institute.model.param;

import io.swagger.v3.oas.annotations.media.Schema;


import jakarta.validation.constraints.NotBlank;

/**
 * 入参：企业信息
 */
@Schema(description = "企业信息")
public class InstituteInfoParam {

    @Schema(description = "id")
    private String id;

    @Schema(description = "父级ID")
    private String parentId;

    @Schema(description = "企业名称")
    @NotBlank
    private String instituteName;

    @Schema(description = "企业简称")
    private String shortName;

    @Schema(description = "企业编码")
    private String instituteCode;

    @Schema(description = "行政编码")
    private String districtCode;

    @Schema(description = "企业地址")
    private String address;

    @Schema(description = "企业类型（020）")
    private String instituteType;

    @Schema(description = "授权分组（004）")
    private String roleGroup;

    @Schema(description = "备注")
    private String description;

    @Schema(description = "经度")
    private String locationLng;

    @Schema(description = "纬度")
    private String locationLat;

    @Schema(description = "三方ID")
    private String thirdId;

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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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

    public String getInstituteCode() {
        return instituteCode;
    }

    public void setInstituteCode(String instituteCode) {
        this.instituteCode = instituteCode;
    }

    public String getDistrictCode() {
        return districtCode;
    }

    public void setDistrictCode(String districtCode) {
        this.districtCode = districtCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocationLng() {
        return locationLng;
    }

    public void setLocationLng(String locationLng) {
        this.locationLng = locationLng;
    }

    public String getLocationLat() {
        return locationLat;
    }

    public void setLocationLat(String locationLat) {
        this.locationLat = locationLat;
    }

    public String getThirdId() {
        return thirdId;
    }

    public void setThirdId(String thirdId) {
        this.thirdId = thirdId;
    }
}
