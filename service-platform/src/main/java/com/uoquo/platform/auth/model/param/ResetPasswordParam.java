package com.uoquo.platform.auth.model.param;

import com.uoquo.annotation.json.Sensitive;
import com.uoquo.annotation.json.SensitiveType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 入参：密码找回（手机号 + 短信码 + 新密码）
 */
@Schema(description = "密码找回")
public class ResetPasswordParam {

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "手机号")
    private String phone;

    @NotBlank(message = "短信验证码不能为空")
    @Schema(description = "短信验证码")
    private String smsCode;

    @NotBlank(message = "新密码不能为空")
    @Sensitive(type = SensitiveType.CRYPT_RSA)
    @Schema(description = "新密码（RSA 加密）")
    private String newPassword;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getSmsCode() {
        return smsCode;
    }

    public void setSmsCode(String smsCode) {
        this.smsCode = smsCode;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
