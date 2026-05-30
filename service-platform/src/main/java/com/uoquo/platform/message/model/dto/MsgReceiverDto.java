package com.uoquo.platform.message.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Date;

/**
 * 出参：消息接收
 */
@Schema(description = "消息接收")
public class MsgReceiverDto {
    @Schema(description = "主键ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String id;

    @Schema(description = "消息ID")
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

    @Schema(description = "更新人")
    private String updateUser;

    @Schema(description = "更新时间")
    private Date updateTime;

    @Schema(description = "删除标识")
    private Long deleteState;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null ? null : id.trim();
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
        this.processedResult = processedResult == null ? null : processedResult.trim();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description == null ? null : description.trim();
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

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Long getDeleteState() {
        return deleteState;
    }

    public void setDeleteState(Long deleteState) {
        this.deleteState = deleteState;
    }
}