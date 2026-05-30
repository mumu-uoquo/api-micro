package com.uoquo.platform.message.model.pojo;

import java.util.Date;
import java.util.List;

/**
 * Table: msg_template
 */
public class MsgTemplate {
    /**
     * Column: id
     * Type: VARCHAR(32)
     * Remark: 模板ID
     */
    private String id;

    /**
     * Column: template_code
     * Type: VARCHAR(50)
     * Remark: 模板编码
     */
    private String templateCode;

    /**
     * Column: template_name
     * Type: VARCHAR(100)
     * Remark: 模板名称
     */
    private String templateName;

    /**
     * Column: message_type
     * Type: CHAR(6)
     * Remark: 消息分类（020）
     */
    private String messageType;

    /**
     * Column: push_way
     * Type: CHAR(6)
     * Remark: 推送方式（021）
     */
    private String pushWay;

    /**
     * Column: title_template
     * Type: VARCHAR(200)
     * Remark: 标题模板
     */
    private String titleTemplate;

    /**
     * Column: variables
     * Type: VARCHAR(500)
     * Remark: 变量说明
     */
    private List<MsgTemplateVariable> variables;

    /**
     * Column: description
     * Type: VARCHAR(200)
     * Remark: 备注
     */
    private String description;

    /**
     * Column: is_default
     * Type: BIT
     * Remark: 是否默认
     */
    private Boolean defaulted;

    /**
     * Column: status
     * Type: CHAR(6)
     * Remark: 可用状态（001）
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
     * Column: content_template
     * Type: TEXT
     * Remark: 内容模板
     */
    private String contentTemplate;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    public String getTemplateCode() {
        return templateCode;
    }

    public void setTemplateCode(String templateCode) {
        this.templateCode = templateCode == null ? null : templateCode.trim();
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName == null ? null : templateName.trim();
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType == null ? null : messageType.trim();
    }

    public String getPushWay() {
        return pushWay;
    }

    public void setPushWay(String pushWay) {
        this.pushWay = pushWay == null ? null : pushWay.trim();
    }

    public String getTitleTemplate() {
        return titleTemplate;
    }

    public void setTitleTemplate(String titleTemplate) {
        this.titleTemplate = titleTemplate == null ? null : titleTemplate.trim();
    }

    public List<MsgTemplateVariable> getVariables() {
        return variables;
    }

    public void setVariables(List<MsgTemplateVariable> variables) {
        this.variables = variables;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description == null ? null : description.trim();
    }

    public Boolean getDefaulted() {
        return defaulted;
    }

    public void setDefaulted(Boolean defaulted) {
        this.defaulted = defaulted;
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

    public String getContentTemplate() {
        return contentTemplate;
    }

    public void setContentTemplate(String contentTemplate) {
        this.contentTemplate = contentTemplate == null ? null : contentTemplate.trim();
    }
}