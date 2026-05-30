package com.uoquo.platform.system.model.pojo;

import java.util.Date;

/**
 * Table: sys_holiday
 */
public class SysHoliday {
    /**
     * Column: id
     * Type: VARCHAR(32)
     * Remark: ID
     */
    private String id;

    /**
     * Column: date_value
     * Type: DATE
     * Remark: 具体日期
     */
    private Date dateValue;

    /**
     * Column: date_type
     * Type: CHAR(6)
     * Remark: 日期类型（007）
     */
    private String dateType;

    /**
     * Column: description
     * Type: VARCHAR(200)
     * Remark: 备注
     */
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