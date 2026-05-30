package com.uoquo.platform.message.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 出参：消息记录查看<br>
 * 除消息本身详情外，还含阅读状态、处理状态等
 */
@Schema(description = "消息记录查看")
public class MsgInfoViewDto {
    @Schema(description = "消息接收ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String recordId;

    @Schema(description = "消息标题", requiredMode = Schema.RequiredMode.REQUIRED)
    private String messageTitle;

    @Schema(description = "消息内容", requiredMode = Schema.RequiredMode.REQUIRED)
    private String messageContent;

    @Schema(description = "消息分类（020）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String messageType;

    @Schema(description = "消息级别（008）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String messageLevel;

    @Schema(description = "推送方式（021）")
    private String pushWay;

    @Schema(description = "业务类型（009）")
    private String businessType;

    @Schema(description = "业务ID")
    private String businessId;

    @Schema(description = "业务扩展")
    private Map<String, Object> businessExtend;

    @Schema(description = "发送人ID")
    private String senderId;

    @Schema(description = "发送人名称")
    private String senderName;

    @Schema(description = "发送人头像")
    private String senderAvatar;

    @Schema(description = "发送时间")
    private Date senderTime;

    @Schema(description = "过期时间")
    private Date expireTime;

    @Schema(description = "附件列表")
    private List<MsgAttachmentDto> attachments;

    @Schema(description = "消息ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String messageId;

    @Schema(description = "接收人ID")
    private String receiverId;

    @Schema(description = "接收人姓名")
    private String receiverName;

    @Schema(description = "是否已读")
    private Boolean readState;

    @Schema(description = "阅读时间")
    private Date readTime;

    @Schema(description = "是否处理")
    private Boolean processedState;

    @Schema(description = "处理时间")
    private Date processedTime;

    @Schema(description = "处理结果")
    private String processedResult;

    @Schema(description = "备注")
    private String description;

    @Schema(description = "创建时间")
    private Date createTime;

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
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

    public String getPushWay() {
        return pushWay;
    }

    public void setPushWay(String pushWay) {
        this.pushWay = pushWay;
    }

    public String getBusinessType() {
        return businessType;
    }

    public void setBusinessType(String businessType) {
        this.businessType = businessType;
    }

    public String getBusinessId() {
        return businessId;
    }

    public void setBusinessId(String businessId) {
        this.businessId = businessId;
    }

    public Map<String, Object> getBusinessExtend() {
        return businessExtend;
    }

    public void setBusinessExtend(Map<String, Object> businessExtend) {
        this.businessExtend = businessExtend;
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

    public Date getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(Date expireTime) {
        this.expireTime = expireTime;
    }

    public List<MsgAttachmentDto> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<MsgAttachmentDto> attachments) {
        this.attachments = attachments;
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

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public Boolean getReadState() {
        return readState;
    }

    public void setReadState(Boolean readState) {
        this.readState = readState;
    }

    public Date getReadTime() {
        return readTime;
    }

    public void setReadTime(Date readTime) {
        this.readTime = readTime;
    }

    public Boolean getProcessedState() {
        return processedState;
    }

    public void setProcessedState(Boolean processedState) {
        this.processedState = processedState;
    }

    public Date getProcessedTime() {
        return processedTime;
    }

    public void setProcessedTime(Date processedTime) {
        this.processedTime = processedTime;
    }

    public String getProcessedResult() {
        return processedResult;
    }

    public void setProcessedResult(String processedResult) {
        this.processedResult = processedResult;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}