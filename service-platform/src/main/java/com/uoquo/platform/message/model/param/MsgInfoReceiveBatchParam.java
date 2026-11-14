package com.uoquo.platform.message.model.param;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

/**
 * 入参：消息发布给一批人（批量添加接收记录）
 * @author xuhz
 */
@Schema(description = "消息发布给一批人")
public class MsgInfoReceiveBatchParam {

    /** 单次最多处理的接收人数量 */
    public static final int MAX_RECEIVERS = 1000;

    @Schema(description = "消息ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String messageId;

    @Schema(description = "接收人（单次最多1000个）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty
    @Size(max = MAX_RECEIVERS)
    @Valid
    private List<Receiver> receivers;

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public List<Receiver> getReceivers() {
        return receivers;
    }

    public void setReceivers(List<Receiver> receivers) {
        this.receivers = receivers;
    }

    /**
     * 接收人
     */
    @Schema(description = "接收人")
    public static class Receiver {

        @Schema(description = "接收人ID", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        private String receiverId;

        @Schema(description = "接收人名称")
        private String receiverName;

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
}
