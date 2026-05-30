package com.uoquo.platform.message.model.param;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;

/**
 * 入参：消息模板状态更新
 */
@Schema(description = "消息模板状态更新")
public class MsgTemplateStatusParam {
    @Schema(description = "模板ID")
    @NotBlank
    private String id;

    @Schema(description = "可用状态（001）")
    @NotBlank
    private String status;

    @Schema(description = "状态备注")
    private String statusMemo;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status == null ? null : status.trim();
    }

    public String getStatusMemo() {
        return statusMemo;
    }

    public void setStatusMemo(String statusMemo) {
        this.statusMemo = statusMemo == null ? null : statusMemo.trim();
    }
}