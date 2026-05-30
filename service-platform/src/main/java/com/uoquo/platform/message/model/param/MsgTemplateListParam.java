package com.uoquo.platform.message.model.param;

import com.uoquo.web.param.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Date;

/**
 * 入参：消息模板列表查询
 */
@Schema(description = "消息模板列表查询")
public class MsgTemplateListParam extends PageRequest {

    @Schema(description = "模板编码")
    private String templateCode;

    @Schema(description = "模板名称")
    private String templateName;

    @Schema(description = "消息分类（020）")
    private String messageType;

    @Schema(description = "推送方式（021）")
    private String pushWay;

    @Schema(description = "是否默认")
    private Boolean defaulted;

    @Schema(description = "可用状态（001）")
    private String status;

    @Schema(description = "起始时间")
    private Date createTimeStart;
    @Schema(description = "结束时间")
    private Date createTimeEnd;

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
