package com.uoquo.platform.system.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Date;

/**
 * 出参：节假日信息
 */
@Schema(description = "节假日信息")
public class SysHolidayDto {

    @Schema(description = "ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String id;

    @Schema(description = "具体日期", requiredMode = Schema.RequiredMode.REQUIRED)
    private Date dateValue;

    @Schema(description = "日期类型（007）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String dateType;

    @Schema(description = "备注")
    private String description;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    public Date getDateValue() {
        return dateValue;
    }

    public void setDateValue(Date dateValue) {
        this.dateValue = dateValue;
    }

    public String getDateType() {
        return dateType;
    }

    public void setDateType(String dateType) {
        this.dateType = dateType == null ? null : dateType.trim();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description == null ? null : description.trim();
    }
}
