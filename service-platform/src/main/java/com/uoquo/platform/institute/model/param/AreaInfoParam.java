package com.uoquo.platform.institute.model.param;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;

/**
 * 入参：分区信息
 */
@Schema(description = "分区信息")
public class AreaInfoParam {
    @Schema(description = "id")
    private String id;

    @Schema(description = "所属企业")
    @NotBlank
    private String instituteId;

    @Schema(description = "区域名称")
    @NotBlank
    private String areaName;

    @Schema(description = "区域编码")
    private String areaCode;

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

    public String getInstituteId() {
        return instituteId;
    }

    public void setInstituteId(String instituteId) {
        this.instituteId = instituteId == null ? null : instituteId.trim();
    }

    public String getAreaName() {
        return areaName;
    }

    public void setAreaName(String areaName) {
        this.areaName = areaName == null ? null : areaName.trim();
    }

    public String getAreaCode() {
        return areaCode;
    }

    public void setAreaCode(String areaCode) {
        this.areaCode = areaCode == null ? null : areaCode.trim();
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