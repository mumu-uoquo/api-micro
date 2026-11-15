package com.uoquo.platform.message.model.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * 入参：消息群发任务（领取、更新进度），仅供 scheduler-center 内部调用
 * @author xuhz
 */
@Schema(description = "消息群发任务")
public class MsgFanoutParam {

    /** 默认的中断判定时间（秒）：进度超过该时间没有更新，视为群发已中断 */
    public static final int DEFAULT_STALE_SECONDS = 300;

    /** 中断判定时间的下限（秒）：小于该值时按该值处理，避免执行较慢的群发被误判为中断而被重复接管 */
    public static final int MIN_STALE_SECONDS = 120;

    /** 默认的最大接管次数 */
    public static final int DEFAULT_MAX_RETRY = 5;

    @Schema(description = "消息ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String messageId;

    @Schema(description = "中断判定时间（秒）：进度超过该时间没有更新，视为群发已中断（领取时使用，默认300）")
    private Integer staleSeconds;

    @Schema(description = "最大接管次数（领取时使用，默认5）")
    private Integer maxRetry;

    @Schema(description = "领取序号（领取成功时返回，更新进度时必须带上）")
    private Integer retry;

    @Schema(description = "已处理的最后一个接收人ID（更新进度时使用）")
    private String cursor;

    @Schema(description = "已处理的接收人数（更新进度时使用）")
    private Integer total;

    @Schema(description = "是否已全部处理完（更新进度时使用）")
    private Boolean finished;

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
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

    public Integer getRetry() {
        return retry;
    }

    public void setRetry(Integer retry) {
        this.retry = retry;
    }

    public String getCursor() {
        return cursor;
    }

    public void setCursor(String cursor) {
        this.cursor = cursor;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public Boolean getFinished() {
        return finished;
    }

    public void setFinished(Boolean finished) {
        this.finished = finished;
    }
}
