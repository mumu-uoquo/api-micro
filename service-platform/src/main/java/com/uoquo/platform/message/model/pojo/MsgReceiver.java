package com.uoquo.platform.message.model.pojo;

import java.util.Date;

/**
 * Table: msg_receiver
 */
public class MsgReceiver {
    /**
     * Column: id
     * Type: VARCHAR(32)
     * Remark: 主键ID
     */
    private String id;

    /**
     * Column: message_id
     * Type: VARCHAR(32)
     * Remark: 消息ID
     */
    private String messageId;

    /**
     * Column: receiver_id
     * Type: VARCHAR(32)
     * Remark: 接收人ID
     */
    private String receiverId;

    /**
     * Column: receiver_name
     * Type: VARCHAR(50)
     * Remark: 接收人姓名
     */
    private String receiverName;

    /**
     * Column: is_read
     * Type: BIT
     * Remark: 是否已读
     */
    private Boolean readState;

    /**
     * Column: read_time
     * Type: DATETIME
     * Remark: 阅读时间
     */
    private Date readTime;

    /**
     * Column: is_processed
     * Type: BIT
     * Remark: 是否处理
     */
    private Boolean processedState;

    /**
     * Column: processed_time
     * Type: DATETIME
     * Remark: 处理时间
     */
    private Date processedTime;

    /**
     * Column: processed_result
     * Type: VARCHAR(200)
     * Remark: 处理结果
     */
    private String processedResult;

    /**
     * Column: description
     * Type: VARCHAR(200)
     * Remark: 备注
     */
    private String description;

    /**
     * Column: create_time
     * Type: DATETIME
     * Remark: 创建时间
     */
    private Date createTime;

    /**
     * Column: update_user
     * Type: VARCHAR(32)
     * Remark: 更新人
     */
    private String updateUser;

    /**
     * Column: update_time
     * Type: DATETIME
     * Remark: 更新时间
     */
    private Date updateTime;

    /**
     * Column: delete_state
     * Type: BIGINT
     * Default value: 0
     * Remark: 删除标识
     */
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