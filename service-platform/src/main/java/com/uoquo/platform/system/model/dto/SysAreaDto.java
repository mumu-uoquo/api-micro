package com.uoquo.platform.system.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;


import java.util.List;

/**
 * 出参：地区信息
 */
@Schema(description = "地区信息")
public class SysAreaDto {

    @Schema(description = "行政编码")
    private String districtCode;

    @Schema(description = "长途区号")
    private String phoneCode;

    @Schema(description = "全称")
    private String fullName;

    @Schema(description = "短称")
    private String shortName;

    @Schema(description = "简称")
    private String abbrName;

    @Schema(description = "下级区域")
    List<SysAreaDto> children;

    public String getDistrictCode() {
        return districtCode;
    }

    public void setDistrictCode(String districtCode) {
        this.districtCode = districtCode;
    }

    public String getPhoneCode() {
        return phoneCode;
    }

    public void setPhoneCode(String phoneCode) {
        this.phoneCode = phoneCode;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getAbbrName() {
        return abbrName;
    }

    public void setAbbrName(String abbrName) {
        this.abbrName = abbrName;
    }

    public List<SysAreaDto> getChildren() {
        return children;
    }

    public void setChildren(List<SysAreaDto> children) {
        this.children = children;
    }
}