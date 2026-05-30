package com.uoquo.platform.system.model.param;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Date;

/**
 * 入参：节假日查询参数
 */
@Schema(description = "节假日查询参数")
public class SysHolidaySearchParam {

    @Schema(description = "开始日期")
    private Date startDate;

    @Schema(description = "结束日期")
    private Date endDate;

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
}
