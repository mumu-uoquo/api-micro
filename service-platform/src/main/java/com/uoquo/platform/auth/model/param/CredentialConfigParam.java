package com.uoquo.platform.auth.model.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * 入参：第三方扫码登录配置
 */
@Schema(description = "第三方扫码登录配置")
public class CredentialConfigParam {

    @NotBlank(message = "场景不能为空")
    @Schema(description = "场景（wechat/wecom）")
    private String scene;

    public String getScene() {
        return scene;
    }

    public void setScene(String scene) {
        this.scene = scene;
    }
}
