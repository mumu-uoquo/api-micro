package com.uoquo.platform.message.model.pojo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * SSE消息，业务类型、操作动作、结果状态都与源事件信息一致
 */
@Schema(description = "SSE消息详情")
public class SseMessage {

    @Schema(description = "接收记录ID（唯一ID，receiverId + messageId）")
    private String recordId;

    @Schema(description = "消息ID")
    private String messageId;

    @Schema(description = "接收人ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String receiverId;

    @Schema(description = "发送人ID")
    private String senderId;

    @Schema(description = "发送人名称")
    private String senderName;

    @Schema(description = "发送人头像")
    private String senderAvatar;

    @Schema(description = "发送时间")
    private Date senderTime;

    @Schema(description = "消息标题")
    private String messageTitle;

    @Schema(description = "消息内容", requiredMode = Schema.RequiredMode.REQUIRED)
    private String messageContent;

    @Schema(description = "消息分类（020）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String messageType;

    @Schema(description = "消息级别（008）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String messageLevel;

    // 大多数情况下，以下信息与源事件一致
    @Schema(description = "业务类型（009）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String businessType;

    @Schema(description = "业务子类型（可自由定义）")
    protected String businessSubType;

    @Schema(description = "业务ID")
    private String businessId;

    @Schema(description = "业务扩展")
    private Map<String, Object> businessExtend;

    @Schema(description = "操作类型（即：业务动作，010）", requiredMode = Schema.RequiredMode.REQUIRED)
    protected String operationType;

    @Schema(description = "执行状态（011）")
    protected String operationStatus;

    @Schema(description = "指定展示端")
    protected String appKey;


    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

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

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderAvatar() {
        return senderAvatar;
    }

    public void setSenderAvatar(String senderAvatar) {
        this.senderAvatar = senderAvatar;
    }

    public Date getSenderTime() {
        return senderTime;
    }

    public void setSenderTime(Date senderTime) {
        this.senderTime = senderTime;
    }

    public String getMessageTitle() {
        return messageTitle;
    }

    public void setMessageTitle(String messageTitle) {
        this.messageTitle = messageTitle;
    }

    public String getMessageContent() {
        return messageContent;
    }

    public void setMessageContent(String messageContent) {
        this.messageContent = messageContent;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getMessageLevel() {
        return messageLevel;
    }

    public void setMessageLevel(String messageLevel) {
        this.messageLevel = messageLevel;
    }

    public String getBusinessType() {
        return businessType;
    }

    public void setBusinessType(String businessType) {
        this.businessType = businessType;
    }

    public String getBusinessSubType() {
        return businessSubType;
    }

    public void setBusinessSubType(String businessSubType) {
        this.businessSubType = businessSubType;
    }

    public String getBusinessId() {
        return businessId;
    }

    public void setBusinessId(String businessId) {
        this.businessId = businessId;
    }

    public Map<String, ?> getBusinessExtend() {
        return businessExtend;
    }

    public void setBusinessExtend(Map<String, ?> businessExtend) {
        if (businessExtend == null) {
            this.businessExtend = new HashMap<>();
        } else {
            this.businessExtend = new HashMap<>(businessExtend);
        }
    }

    public <E> void addBusinessExtend(String key, E val) {
        if (this.businessExtend == null) {
            this.businessExtend = new HashMap<>();
        }
        this.businessExtend.put(key, val);
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public String getOperationStatus() {
        return operationStatus;
    }

    public void setOperationStatus(String operationStatus) {
        this.operationStatus = operationStatus;
    }

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }
}

