package com.uoquo.platform.message.model.param;

import com.uoquo.web.param.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Date;

/**
 * 入参：消息记录列表查询
 */
@Schema(description = "消息记录列表查询")
public class MsgInfoListParam extends PageRequest {

    @Schema(description = "消息ID")
    private String messageId;

    @Schema(description = "创建人")
    private String createUser;

    @Schema(description = "接收人ID")
    private String receiverId;

    @Schema(description = "消息标题")
    private String messageTitle;

    @Schema(description = "消息内容")
    private String messageContent;

    @Schema(description = "消息分类（020）")
    private String messageType;

    @Schema(description = "消息级别（008）")
    private String messageLevel;

    @Schema(description = "推送方式（021）")
    private String pushWay;

    @Schema(description = "业务类型（009）")
    private String businessType;

    @Schema(description = "发布状态（023）")
    private String status;

    @Schema(description = "是否已读")
    private Boolean readState;

    @Schema(description = "是否处理")
    private Boolean processedState;

    @Schema(description = "过期起始时间")
    private Date expireTimeStart;
    @Schema(description = "过期结束时间")
    private Date expireTimeEnd;

    @Schema(description = "发送起始时间")
    private Date senderTimeStart;
    @Schema(description = "发送结束时间")
    private Date senderTimeEnd;

    @Schema(description = "创建起始时间")
    private Date createTimeStart;
    @Schema(description = "创建结束时间")
    private Date createTimeEnd;

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getCreateUser() {
        return createUser;
    }

    public void setCreateUser(String createUser) {
        this.createUser = createUser == null ? null : createUser.trim();
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId == null ? null : receiverId.trim();
    }

    public String getMessageTitle() {
        return messageTitle;
    }

    public void setMessageTitle(String messageTitle) {
        this.messageTitle = messageTitle == null ? null : messageTitle.trim();
    }

    public String getMessageContent() {
        return messageContent;
    }

    public void setMessageContent(String messageContent) {
        this.messageContent = messageContent == null ? null : messageContent.trim();
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType == null ? null : messageType.trim();
    }

    public String getMessageLevel() {
        return messageLevel;
    }

    public void setMessageLevel(String messageLevel) {
        this.messageLevel = messageLevel == null ? null : messageLevel.trim();
    }

    public String getPushWay() {
        return pushWay;
    }

    public void setPushWay(String pushWay) {
        this.pushWay = pushWay == null ? null : pushWay.trim();
    }

    public String getBusinessType() {
        return businessType;
    }

    public void setBusinessType(String businessType) {
        this.businessType = businessType == null ? null : businessType.trim();
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status == null ? null : status.trim();
    }

    public Boolean getReadState() {
        return readState;
    }

    public void setReadState(Boolean readState) {
        this.readState = readState;
    }

    public Boolean getProcessedState() {
        return processedState;
    }

    public void setProcessedState(Boolean processedState) {
        this.processedState = processedState;
    }

    public Date getExpireTimeStart() {
        return expireTimeStart;
    }

    public void setExpireTimeStart(Date expireTimeStart) {
        this.expireTimeStart = expireTimeStart;
    }

    public Date getExpireTimeEnd() {
        return expireTimeEnd;
    }

    public void setExpireTimeEnd(Date expireTimeEnd) {
        this.expireTimeEnd = expireTimeEnd;
    }

    public Date getSenderTimeStart() {
        return senderTimeStart;
    }

    public void setSenderTimeStart(Date senderTimeStart) {
        this.senderTimeStart = senderTimeStart;
    }

    public Date getSenderTimeEnd() {
        return senderTimeEnd;
    }

    public void setSenderTimeEnd(Date senderTimeEnd) {
        this.senderTimeEnd = senderTimeEnd;
    }

    public Date getCreateTimeStart() {
        return createTimeStart;
    }

    public void setCreateTimeStart(Date createTimeStart) {
        this.createTimeStart = createTimeStart;
    }

    public Date getCreateTimeEnd() {
        return createTimeEnd;
    }

    public void setCreateTimeEnd(Date createTimeEnd) {
        this.createTimeEnd = createTimeEnd;
    }
}