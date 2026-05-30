package com.uoquo.platform.user.model.param;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;

/**
 * 入参：修改用户头像
 */
@Schema(description = "修改用户头像")
public class UpdateAvatarParam {

    @Schema(description = "用户头像")
    @NotBlank
    private String avatar;

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

}
