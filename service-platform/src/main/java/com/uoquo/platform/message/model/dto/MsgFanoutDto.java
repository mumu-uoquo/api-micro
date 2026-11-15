package com.uoquo.platform.message.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 出参：消息群发任务的领取结果
 * @author xuhz
 */
@Schema(description = "消息群发任务的领取结果")
public class MsgFanoutDto {

    @Schema(description = "是否领取成功（false 表示已完成、正在被其他节点执行、消息已撤回/删除或接管次数已用完，调用方不应继续群发）")
    private boolean claimed;

    @Schema(description = "领取序号（更新进度时必须带上）")
    private int retry;

    @Schema(description = "已处理的最后一个接收人ID（为空表示从头开始）")
    private String cursor;

    @Schema(description = "已处理的接收人数")
    private int total;

    public boolean isClaimed() {
        return claimed;
    }

    public void setClaimed(boolean claimed) {
        this.claimed = claimed;
    }

    public int getRetry() {
        return retry;
    }

    public void setRetry(int retry) {
        this.retry = retry;
    }

    public String getCursor() {
        return cursor;
    }

    public void setCursor(String cursor) {
        this.cursor = cursor;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }
}
