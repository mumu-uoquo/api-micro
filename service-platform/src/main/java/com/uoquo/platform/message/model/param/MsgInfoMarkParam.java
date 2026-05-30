package com.uoquo.platform.message.model.param;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * 入参：消息标记
 * @author xuhz
 */
@Schema(description = "消息标记")
public class MsgInfoMarkParam {
    @Schema(description = "记录ID集合")
    private List<String> recordIds;

    @Schema(description = "消息ID")
    private String messageId;

    @Schema(description = "备注描述")
    private String description;

    public List<String> getRecordIds() {
        return recordIds;
    }

    public void setRecordIds(List<String> recordIds) {
        this.recordIds = recordIds;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}