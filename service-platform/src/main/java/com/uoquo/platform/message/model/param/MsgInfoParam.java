package com.uoquo.platform.message.model.param;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotEmpty;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 入参：消息记录
 */
@Schema(description = "消息记录")
public class MsgInfoParam {
    @Schema(description = "消息ID")
    private String id;

    @Schema(description = "模板ID")
    private String templateId;

    @Schema(description = "消息标题")
    @NotEmpty
    private String messageTitle;

    @Schema(description = "消息内容")
    @NotEmpty
    private String messageContent;

    @Schema(description = "消息分类（020）")
    @NotEmpty
    private String messageType;

    @Schema(description = "消息级别（008）")
    @NotEmpty
    private String messageLevel;

    @Schema(description = "推送方式（021）")
    @NotEmpty
    private String pushWay;

    @Schema(description = "业务类型（009）")
    private String businessType;

    @Schema(description = "业务ID")
    private String businessId;

    @Schema(description = "业务扩展")
    private Map<String, Object> businessExtend;

    @Schema(description = "过期时间")
    private Date expireTime;

    @Schema(description = "备注")
    private String description;

    @Schema(description = "发布状态（023）")
    private String status;

    @Schema(description = "发布范围（024）")
    private String receiverRange;

    @Schema(description = "目标所属机构ID")
    private String receiverInstituteId;

    @Schema(description = "目标所属机构名称")
    private String receiverInstituteName;

    @Schema(description = "目标ID集合")
    private String receiverIds;

    @Schema(description = "附件列表")
    private List<MsgAttachmentParam> attachments;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
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

    public List<MsgAttachmentParam> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<MsgAttachmentParam> attachments) {
        this.attachments = attachments;
    }
}