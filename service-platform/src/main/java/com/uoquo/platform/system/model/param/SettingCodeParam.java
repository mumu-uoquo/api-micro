package com.uoquo.platform.system.model.param;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;

/**
 * 配置编码查询参数
 */
@Schema(description = "配置编码查询参数")
public class SettingCodeParam {

    @Schema(description = "机构ID（机构配置专用，可选）")
    private String instituteId;

    @Schema(description = "配置标识", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "配置标识不能为空")
    private String configCode;

    public String getInstituteId() {
        return instituteId;
    }

    public void setInstituteId(String instituteId) {
        this.instituteId = instituteId;
    }

    public String getConfigCode() {
        return configCode;
    }

    public void setConfigCode(String configCode) {
        this.configCode = configCode;
    }
}
