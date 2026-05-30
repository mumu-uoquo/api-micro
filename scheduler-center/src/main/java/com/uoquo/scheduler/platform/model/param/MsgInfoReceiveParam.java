package com.uoquo.scheduler.platform.model.param;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;

/**
 * 入参：消息发布
 * @author xuhz
 */
@Schema(description = "消息发布给具体的人")
public class MsgInfoReceiveParam {

    @Schema(description = "消息ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String messageId;

    @Schema(description = "接收人ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String receiverId;

    @Schema(description = "接收人名称")
    private String receiverName;

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }
}