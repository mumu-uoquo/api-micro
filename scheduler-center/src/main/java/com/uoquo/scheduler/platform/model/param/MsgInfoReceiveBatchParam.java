package com.uoquo.scheduler.platform.model.param;

import java.util.ArrayList;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 入参：消息发布给一批人（批量添加接收记录，单次最多1000个）
 * @author xuhz
 */
@Schema(description = "消息发布给一批人")
public class MsgInfoReceiveBatchParam {

    @Schema(description = "消息ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String messageId;

    @Schema(description = "接收人（单次最多1000个）", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Receiver> receivers = new ArrayList<>();

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

    public void addReceiver(String receiverId, String receiverName) {
        Receiver receiver = new Receiver();
        receiver.setReceiverId(receiverId);
        receiver.setReceiverName(receiverName);
        this.receivers.add(receiver);
    }

    /**
     * 接收人
     */
    @Schema(description = "接收人")
    public static class Receiver {

        @Schema(description = "接收人ID", requiredMode = Schema.RequiredMode.REQUIRED)
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
