package com.uoquo.platform.message.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 出参：消息记录
 */
@Schema(description = "消息记录")
public class MsgInfoDto {
    @Schema(description = "消息ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String id;

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

    @Schema(description = "发送时间")
    private Date senderTime;

    @Schema(description = "过期时间")
    private Date expireTime;

    @Schema(description = "发布范围（024）")
    private String receiverRange;

    @Schema(description = "目标所属机构")
    private String receiverInstituteId;
    @Schema(description = "目标所属机构名称")
    private String receiverInstituteName;

    @Schema(description = "接收人ID列表（为空表示全员）")
    private String receiverIds;

    @Schema(description = "备注")
    private String description;

    @Schema(description = "发布状态（023）")
    private String status;

    @Schema(description = "状态时间")
    private Date statusTime;

    @Schema(description = "状态备注")
    private String statusMemo;

    @Schema(description = "创建人")
    private String createUser;

    @Schema(description = "创建人名称")
    private String createUserName;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "更新人")
    private String updateUser;

    @Schema(description = "更新人名称")
    private String updateUserName;

    @Schema(description = "更新时间")
    private Date updateTime;

    @Schema(description = "附件列表")
    private List<MsgAttachmentDto> attachments;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null ? null : id.trim();
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

    public String getBusinessId() {
        return businessId;
    }

    public void setBusinessId(String businessId) {
        this.businessId = businessId == null ? null : businessId.trim();
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
        this.senderId = senderId == null ? null : senderId.trim();
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName == null ? null : senderName.trim();
    }

    public Date getSenderTime() {
        return senderTime;
    }

    public void setSenderTime(Date senderTime) {
        this.senderTime = senderTime;
    }

    public String getReceiverRange() {
        return receiverRange;
    }

    public void setReceiverRange(String receiverRange) {
        this.receiverRange = receiverRange;
    }

    public String getReceiverInstituteId() {
        return receiverInstituteId;
    }

    public void setReceiverInstituteId(String receiverInstituteId) {
        this.receiverInstituteId = receiverInstituteId;
    }

    public String getReceiverInstituteName() {
        return receiverInstituteName;
    }

    public void setReceiverInstituteName(String receiverInstituteName) {
        this.receiverInstituteName = receiverInstituteName;
    }

    public String getReceiverIds() {
        return receiverIds;
    }

    public void setReceiverIds(String receiverIds) {
        this.receiverIds = receiverIds;
    }

    public Date getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(Date expireTime) {
        this.expireTime = expireTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description == null ? null : description.trim();
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status == null ? null : status.trim();
    }

    public Date getStatusTime() {
        return statusTime;
    }

    public void setStatusTime(Date statusTime) {
        this.statusTime = statusTime;
    }

    public String getStatusMemo() {
        return statusMemo;
    }

    public void setStatusMemo(String statusMemo) {
        this.statusMemo = statusMemo == null ? null : statusMemo.trim();
    }

    public String getCreateUser() {
        return createUser;
    }

    public void setCreateUser(String createUser) {
        this.createUser = createUser == null ? null : createUser.trim();
    }

    public String getCreateUserName() {
        return createUserName;
    }

    public void setCreateUserName(String createUserName) {
        this.createUserName = createUserName;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getUpdateUser() {
        return updateUser;
    }

    public void setUpdateUser(String updateUser) {
        this.updateUser = updateUser == null ? null : updateUser.trim();
    }

    public String getUpdateUserName() {
        return updateUserName;
    }

    public void setUpdateUserName(String updateUserName) {
        this.updateUserName = updateUserName;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public List<MsgAttachmentDto> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<MsgAttachmentDto> attachments) {
        this.attachments = attachments;
    }
}