package com.uoquo.platform.message.model.param;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 入参：消息的定时维护任务（到点发布、过期清理、查询中断的群发），仅供 scheduler-center 内部调用。<br>
 * 所有字段都有默认值，未传或传入非法值（小于等于0）时使用默认值。
 * @author xuhz
 */
@Schema(description = "消息定时维护任务")
public class MsgMaintainParam {

    @Schema(description = "单次处理的最大条数（到点发布默认50，过期清理默认20，查询中断的群发默认10）")
    private Integer limit;

    @Schema(description = "过期清理：过期后保留的天数，超过该天数的消息才会被清理（默认30）")
    private Integer retainDays;

    @Schema(description = "过期清理：单次调用的最长执行时间（秒，默认20）")
    private Integer maxSeconds;

    @Schema(description = "查询中断的群发：中断判定时间（秒，默认300）")
    private Integer staleSeconds;

    @Schema(description = "查询中断的群发：最大接管次数（默认5）")
    private Integer maxRetry;

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public Integer getRetainDays() {
        return retainDays;
    }

    public void setRetainDays(Integer retainDays) {
        this.retainDays = retainDays;
    }

    public Integer getMaxSeconds() {
        return maxSeconds;
    }

    public void setMaxSeconds(Integer maxSeconds) {
        this.maxSeconds = maxSeconds;
    }

    public Integer getStaleSeconds() {
        return staleSeconds;
    }

    public void setStaleSeconds(Integer staleSeconds) {
        this.staleSeconds = staleSeconds;
    }

    public Integer getMaxRetry() {
        return maxRetry;
    }

    public void setMaxRetry(Integer maxRetry) {
        this.maxRetry = maxRetry;
    }
}
