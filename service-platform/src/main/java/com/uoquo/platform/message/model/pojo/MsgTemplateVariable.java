package com.uoquo.platform.message.model.pojo;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 模板变量对象
 */
@Schema(description = "模板变量对象")
public class MsgTemplateVariable {

    @Schema(description = "名称")
    private String name;

    @Schema(description = "说明")
    private String description;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}