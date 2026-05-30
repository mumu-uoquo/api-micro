package com.uoquo.platform.message.model.param;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Date;

/**
 * 入参：消息推送日志
 * @author uoquo
 */
@Schema(description = "消息推送日志")
public class MsgPushLogParam {

    @Schema(description = "旧日志ID（用于重推时更新对应记录的状态）")
    private String oldLogId;

    @Schema(description = "消息ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String messageId;

    @Schema(description = "接收人ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String receiverId;

    @Schema(description = "接收人姓名")
    private String receiverName;

    @Schema(description = "推送方式（021）")
    private String pushWay;

    @Schema(description = "推送状态（022）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String pushStatus;

    @Schema(description = "推送时间")
    private Date pushTime;

    @Schema(description = "推送结果")
    private String pushResult;

    @Schema(description = "重试次数")
    private Integer retryCount;

    @Schema(description = "备注")
    private String description;

    public String getOldLogId() {
        return oldLogId;
    }

    public void setOldLogId(String oldLogId) {
        this.oldLogId = oldLogId;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId == null ? null : messageId.trim();
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId == null ? null : receiverId.trim();
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName == null ? null : receiverName.trim();
    }

    public String getPushWay() {
        return pushWay;
    }

    public void setPushWay(String pushWay) {
        this.pushWay = pushWay == null ? null : pushWay.trim();
    }

    public String getPushStatus() {
        return pushStatus;
    }

    public void setPushStatus(String pushStatus) {
        this.pushStatus = pushStatus == null ? null : pushStatus.trim();
    }

    public Date getPushTime() {
        return pushTime;
    }

    public void setPushTime(Date pushTime) {
        this.pushTime = pushTime;
    }

    public String getPushResult() {
        return pushResult;
    }

    public void setPushResult(String pushResult) {
        this.pushResult = pushResult == null ? null : pushResult.trim();
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description == null ? null : description.trim();
    }

}