package com.uoquo.platform.system.model.param;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;

/**
 * License 导入参数<br/>
 * 目前仅包含 license 字符串，后续可扩展其他内容。
 */
@Schema(description = "License导入参数")
public class LicenseImportParam {

    @Schema(description = "license内容", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "license内容不能为空")
    private String license;

    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }
}
