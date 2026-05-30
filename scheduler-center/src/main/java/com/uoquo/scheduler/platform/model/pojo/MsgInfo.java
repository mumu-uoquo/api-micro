package com.uoquo.scheduler.platform.model.pojo;

import java.util.Date;
import java.util.Map;

/**
 * Table: msg_info
 */
public class MsgInfo {
    /**
     * Column: id
     * Type: VARCHAR(32)
     * Remark: 消息ID
     */
    private String id;

    /**
     * Column: message_title
     * Type: VARCHAR(100)
     * Remark: 消息标题
     */
    private String messageTitle;

    /**
     * Column: message_type
     * Type: CHAR(6)
     * Remark: 消息分类（020）
     */
    private String messageType;

    /**
     * Column: message_level
     * Type: CHAR(6)
     * Remark: 消息级别（008）
     */
    private String messageLevel;

    /**
     * Column: push_way
     * Type: CHAR(6)
     * Remark: 推送方式（021）
     */
    private String pushWay;

    /**
     * Column: business_type
     * Type: CHAR(6)
     * Remark: 业务类型（009）
     */
    private String businessType;

    /**
     * Column: business_id
     * Type: VARCHAR(32)
     * Remark: 业务ID
     */
    private String businessId;

    /**
     * Column: business_extend
     * Type: JSON(0)
     * Remark: 业务扩展
     */
    private Map<String, Object> businessExtend;

    /**
     * Column: sender_id
     * Type: VARCHAR(32)
     * Remark: 发送人ID
     */
    private String senderId;

    /**
     * Column: sender_name
     * Type: VARCHAR(100)
     * Remark: 发送人名称
     */
    private String senderName;

    /**
     * Column: sender_time
     * Type: DATETIME
     * Remark: 发送时间
     */
    private Date senderTime;

    /**
     * Column: expire_time
     * Type: DATETIME
     * Remark: 过期时间
     */
    private Date expireTime;

    /**
     * Column: receiver_range
     * Type: CHAR(6)
     * Remark: 发布范围（024）
     */
    private String receiverRange;

    /**
     * Column: receiver_institute_id
     * Type: VARCHAR(32)
     * Remark: 目标所属机构
     */
    private String receiverInstituteId;

    /**
     * Column: receiver_ids
     * Type: VARCHAR(500)
     * Remark: 目标ID集合
     */
    private String receiverIds;

    /**
     * Column: description
     * Type: VARCHAR(200)
     * Remark: 备注
     */
    private String description;

    /**
     * Column: status
     * Type: CHAR(6)
     * Remark: 发布状态（023）
     */
    private String status;

    /**
     * Column: status_time
     * Type: DATETIME
     * Remark: 状态时间
     */
    private Date statusTime;

    /**
     * Column: status_memo
     * Type: VARCHAR(100)
     * Remark: 状态备注
     */
    private String statusMemo;

    /**
     * Column: create_user
     * Type: VARCHAR(32)
     * Remark: 创建人
     */
    private String createUser;

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

    /**
     * Column: message_content
     * Type: TEXT
     * Remark: 消息内容
     */
    private String messageContent;

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

    public Date getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(Date expireTime) {
        this.expireTime = expireTime;
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

    public String getReceiverIds() {
        return receiverIds;
    }

    public void setReceiverIds(String receiverIds) {
        this.receiverIds = receiverIds;
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

    public String getMessageContent() {
        return messageContent;
    }

    public void setMessageContent(String messageContent) {
        this.messageContent = messageContent == null ? null : messageContent.trim();
    }
}