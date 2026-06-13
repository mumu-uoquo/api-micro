package com.uoquo.platform.auth.model.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * 入参：刷新 Token 登录
 */
@Schema(description = "刷新Token登录")
public class TokenLoginParam extends BasicLoginParam {

    @NotBlank(message = "刷新码不能为空")
    @Schema(description = "刷新token")
    private String refreshToken;

    @Schema(description = "当前角色")
    private String currentRoleId;

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getCurrentRoleId() {
        return currentRoleId;
    }

    public void setCurrentRoleId(String currentRoleId) {
        this.currentRoleId = currentRoleId;
    }
}
