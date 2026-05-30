package com.uoquo.platform.system.model.param;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;

/**
 * 入参：系统响应码
 */
@Schema(description = "系统响应码请求参数")
public class SysReturnCodeParam {

    @Schema(description = "响应码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "响应码不能为空")
    private String returnCode;

    @Schema(description = "响应描述")
    private String returnValue;

    @Schema(description = "备注")
    private String description;

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
