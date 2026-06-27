package com.uoquo.platform.auth.model.param;

import com.uoquo.annotation.json.Sensitive;
import com.uoquo.annotation.json.SensitiveType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * 入参：账号密码登录
 */
@Schema(description = "账号密码登录")
public class AccountLoginParam extends BasicLoginParam {

    @NotBlank(message = "账号不能为空")
    @Schema(description = "登录账号")
    @Sensitive(type = SensitiveType.CRYPT_RSA)
    private String account;

    @NotBlank(message = "密码不能为空")
    @Schema(description = "登录密码")
    @Sensitive(type = SensitiveType.CRYPT_RSA)
    private String password;

    @Schema(description = "验证码")
    private String captcha;

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCaptcha() {
        return captcha;
    }

    public void setCaptcha(String captcha) {
        this.captcha = captcha;
    }
}
