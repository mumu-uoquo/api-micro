package com.uoquo.platform.system.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 出参：系统响应码
 */
@Schema(description = "系统响应码")
public class SysReturnCodeDto {

    @Schema(description = "ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String id;

    @Schema(description = "响应码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String returnCode;

    @Schema(description = "响应描述")
    private String returnValue;

    @Schema(description = "备注")
    private String description;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getReturnCode() {
        return returnCode;
    }

    public void setReturnCode(String returnCode) {
        this.returnCode = returnCode;
    }

    public String getReturnValue() {
        return returnValue;
    }

    public void setReturnValue(String returnValue) {
        this.returnValue = returnValue;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
