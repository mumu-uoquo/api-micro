package com.uoquo.platform.message.model.param;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

/**
 * 入参：消息推送日志（批量）
 * @author xuhz
 */
@Schema(description = "消息推送日志（批量）")
public class MsgPushLogBatchParam {

    @Schema(description = "推送日志（单次最多1000条，仅用于首次推送的日志，不支持重试日志）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty
    @Size(max = MsgInfoReceiveBatchParam.MAX_RECEIVERS)
    @Valid
    private List<MsgPushLogParam> logs;

    public List<MsgPushLogParam> getLogs() {
        return logs;
    }

    public void setLogs(List<MsgPushLogParam> logs) {
        this.logs = logs;
    }
}
