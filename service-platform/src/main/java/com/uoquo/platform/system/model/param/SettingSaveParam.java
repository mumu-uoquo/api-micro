package com.uoquo.platform.system.model.param;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 配置保存参数
 */
@Schema(description = "配置保存参数")
public class SettingSaveParam {

    @Schema(description = "机构ID（机构配置专用，可选）")
    private String instituteId;

    @Schema(description = "配置标识", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "配置标识不能为空")
    @Size(max = 50, message = "配置标识长度不能超过50")
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9-_.]*$", message = "配置标识必须以字母开头，且只能包含字母、数字、中横线、下划线和小数点")
    private String configCode;

    @Schema(description = "配置名称")
    @Size(max = 100, message = "配置名称长度不能超过100")
    private String configName;

    @Schema(description = "配置值")
    @Size(max = 1000, message = "配置值长度不能超过1000")
    private String configValue;

    @Schema(description = "备注")
    @Size(max = 100, message = "备注长度不能超过100")
    private String description;

    @Schema(description = "作用范围（003001内置/003002通用/003003私有）")
    @Size(max = 6, message = "作用范围编码长度不能超过6")
    private String publicType;

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

    public String getConfigName() {
        return configName;
    }

    public void setConfigName(String configName) {
        this.configName = configName;
    }

    public String getConfigValue() {
        return configValue;
    }

    public void setConfigValue(String configValue) {
        this.configValue = configValue;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPublicType() {
        return publicType;
    }

    public void setPublicType(String publicType) {
        this.publicType = publicType;
    }
}
