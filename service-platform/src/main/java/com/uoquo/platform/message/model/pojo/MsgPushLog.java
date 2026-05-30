package com.uoquo.platform.message.model.pojo;

import java.util.Date;

/**
 * Table: msg_push_log
 */
public class MsgPushLog {
    /**
     * Column: id
     * Type: VARCHAR(32)
     * Remark: 日志ID
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
     * Column: push_way
     * Type: CHAR(6)
     * Remark: 推送方式（021）
     */
    private String pushWay;

    /**
     * Column: push_status
     * Type: CHAR(6)
     * Remark: 推送状态（022）
     */
    private String pushStatus;

    /**
     * Column: push_time
     * Type: DATETIME
     * Remark: 推送时间
     */
    private Date pushTime;

    /**
     * Column: push_result
     * Type: VARCHAR(500)
     * Remark: 推送结果
     */
    private String pushResult;

    /**
     * Column: retry_count
     * Type: TINYINT(3)
     * Default value: 0
     * Remark: 重试次数
     */
    private Integer retryCount;

    /**
     * Column: description
     * Type: VARCHAR(200)
     * Remark: 备注
     */
    private String description;

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