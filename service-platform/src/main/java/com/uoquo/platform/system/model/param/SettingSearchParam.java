package com.uoquo.platform.system.model.param;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;

/**
 * 配置列表查询参数
 */
@Schema(description = "配置列表查询参数")
public class SettingSearchParam {

    @Schema(description = "机构ID（机构配置专用，可选）")
    private String instituteId;

    @Schema(description = "配置标识前缀（必须输入）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "前缀参数不能为空")
    private String prefix;

    public String getInstituteId() {
        return instituteId;
    }

    public void setInstituteId(String instituteId) {
        this.instituteId = instituteId;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
}
