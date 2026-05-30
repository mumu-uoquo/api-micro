package com.uoquo.platform.user.model.param;

import io.swagger.v3.oas.annotations.media.Schema;


import jakarta.validation.constraints.NotBlank;

/**
 * 入参：修改用户密码
 */
@Schema(description = "新增用户信息")
public class ChangePasswordParam {

    @Schema(description = "用户id")
    private String id;

    @Schema(description = "旧密碼")
    private String oldPassword;

    @Schema(description = "新密碼")
    @NotBlank
    private String newPassword;

    @Schema(description = "新密码强度")
    private String newPwdLevel;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getNewPwdLevel() {
        return newPwdLevel;
    }

    public void setNewPwdLevel(String newPwdLevel) {
        this.newPwdLevel = newPwdLevel;
    }
}
