package com.uoquo.platform.auth.model.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * 入参：第三方扫码登录状态
 */
@Schema(description = "第三方扫码登录状态")
public class CredentialStatusParam {

    @NotBlank(message = "场景不能为空")
    @Schema(description = "场景（wechat/wecom）")
    private String scene;

    @NotBlank(message = "授权的 state不能为空")
    @Schema(description = "本次授权的 state（用于回调与状态轮询）")
    private String state;

    public String getScene() {
        return scene;
    }

    public void setScene(String scene) {
        this.scene = scene;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
