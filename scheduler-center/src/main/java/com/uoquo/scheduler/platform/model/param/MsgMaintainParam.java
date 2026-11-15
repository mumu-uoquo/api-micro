package com.uoquo.scheduler.platform.model.param;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 入参：消息的定时维护任务（到点发布、过期清理、查询中断的群发）。<br>
 * 字段为空时由 service-platform 使用默认值。
 * @author xuhz
 */
@Schema(description = "消息定时维护任务")
public class MsgMaintainParam {

    @Schema(description = "单次处理的最大条数")
    private Integer limit;

    @Schema(description = "过期清理：过期后保留的天数")
    private Integer retainDays;

    @Schema(description = "过期清理：单次调用的最长执行时间（秒）")
    private Integer maxSeconds;

    @Schema(description = "查询中断的群发：中断判定时间（秒）")
    private Integer staleSeconds;

    @Schema(description = "查询中断的群发：最大接管次数")
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
