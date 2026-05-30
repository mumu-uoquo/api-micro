package com.uoquo.platform.message.model.dto;

import com.uoquo.platform.message.model.pojo.MsgTemplateVariable;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Date;
import java.util.List;

/**
 * 出参：消息模板
 */
@Schema(description = "消息模板")
public class MsgTemplateDto {
    @Schema(description = "模板ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String id;

    @Schema(description = "模板编码")
    private String templateCode;

    @Schema(description = "模板名称")
    private String templateName;

    @Schema(description = "消息分类（020）")
    private String messageType;

    @Schema(description = "推送方式（021）")
    private String pushWay;

    @Schema(description = "标题模板")
    private String titleTemplate;

    @Schema(description = "内容模板")
    private String contentTemplate;

    @Schema(description = "变量说明")
    private List<MsgTemplateVariable> variables;

    @Schema(description = "备注")
    private String description;

    @Schema(description = "是否默认")
    private Boolean defaulted;

    @Schema(description = "可用状态（001）")
    private String status;

    @Schema(description = "状态时间")
    private Date statusTime;

    @Schema(description = "状态备注")
    private String statusMemo;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "更新时间")
    private Date updateTime;

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

    public String getContentTemplate() {
        return contentTemplate;
    }

    public void setContentTemplate(String contentTemplate) {
        this.contentTemplate = contentTemplate == null ? null : contentTemplate.trim();
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

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}