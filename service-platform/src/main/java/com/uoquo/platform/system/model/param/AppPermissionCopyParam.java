package com.uoquo.platform.system.model.param;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

/**
 * 入参：复制APP授权
 */
@Schema(description = "复制APP授权")
public class AppPermissionCopyParam {

    @Schema(description = "源AppId")
    @NotBlank
    private String fromAppId;

    @Schema(description = "目标AppId")
    @NotEmpty
    private String toAppId;

    public String getFromAppId() {
        return fromAppId;
    }

    public void setFromAppId(String fromAppId) {
        this.fromAppId = fromAppId;
    }

    public String getToAppId() {
        return toAppId;
    }

    public void setToAppId(String toAppId) {
        this.toAppId = toAppId;
    }
}
