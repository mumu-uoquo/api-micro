package com.uoquo.platform.system.model.dto;

import com.uoquo.mybatis.sensitive.SensitiveData;
import com.uoquo.mybatis.sensitive.SensitiveField;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 配置信息DTO（支持配置来源标识）
 * MyBatis 拦截器检测 @SensitiveData 类中的 @SensitiveField 字段自动解密
 */
@SensitiveData
@Schema(description = "配置信息")
public class SettingDto {

    @Schema(description = "配置名称")
    private String configName;

    @Schema(description = "配置标识", requiredMode = Schema.RequiredMode.REQUIRED)
    private String configCode;

    @SensitiveField
    @Schema(description = "配置内容")
    private String configValue;

    @Schema(description = "备注")
    private String description;

    @Schema(description = "配置来源：USER、INSTITUTE、SYSTEM")
    private String source;

    public String getConfigName() {
        return configName;
    }

    public void setConfigName(String configName) {
        this.configName = configName;
    }

    public String getConfigCode() {
        return configCode;
    }

    public void setConfigCode(String configCode) {
        this.configCode = configCode;
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

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
