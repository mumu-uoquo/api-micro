package com.uoquo.scheduler.platform.model.param;

import java.util.ArrayList;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 入参：消息推送日志（批量，单次最多1000条，仅用于首次推送）
 * @author xuhz
 */
@Schema(description = "消息推送日志（批量）")
public class MsgPushLogBatchParam {

    @Schema(description = "推送日志", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<MsgPushLogParam> logs = new ArrayList<>();

    public List<MsgPushLogParam> getLogs() {
        return logs;
    }

    public void setLogs(List<MsgPushLogParam> logs) {
        this.logs = logs;
    }
}
