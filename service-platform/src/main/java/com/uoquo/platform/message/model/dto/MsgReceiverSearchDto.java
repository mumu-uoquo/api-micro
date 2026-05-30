package com.uoquo.platform.message.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 出参：发布目标记录
 */
@Schema(description = "发布目标记录")
public class MsgReceiverSearchDto {

    @Schema(description = "主键ID")
    private String id;

    @Schema(description = "显示名称")
    private String name;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
