package com.uoquo.platform.role.model.pojo;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 请求参数
 */
@Schema(description = "请求参数")
public class ModuleParam {

    @Schema(description = "键", requiredMode = Schema.RequiredMode.REQUIRED)
    private String key;

    @Schema(description = "值", requiredMode = Schema.RequiredMode.REQUIRED)
    private String val;

    @Schema(description = "是否可见")
    private Boolean enabled;

    @Schema(description = "说明")
    private String description;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getVal() {
        return val;
    }

    public void setVal(String val) {
        this.val = val;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
