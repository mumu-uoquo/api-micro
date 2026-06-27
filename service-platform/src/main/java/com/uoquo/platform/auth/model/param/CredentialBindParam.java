package com.uoquo.platform.auth.model.param;

import com.uoquo.annotation.json.Sensitive;
import com.uoquo.annotation.json.SensitiveType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * 入参：凭证绑定（账号密码 + tempToken）
 */
@Schema(description = "凭证绑定（账号密码 + tempToken）")
public class CredentialBindParam extends BasicLoginParam {

    @NotBlank(message = "账号不能为空")
    @Schema(description = "登录账号（手机号或用户名）")
    @Sensitive(type = SensitiveType.CRYPT_RSA)
    private String account;

    @NotBlank(message = "密码不能为空")
    @Schema(description = "登录密码（RSA 加密）")
    @Sensitive(type = SensitiveType.CRYPT_RSA)
    private String password;

    @NotBlank(message = "临时Token不能为空")
    @Schema(description = "凭证登录返回的临时Token")
    private String tempToken;

    @Schema(description = "验证码（非必填）")
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

    public String getTempToken() {
        return tempToken;
    }

    public void setTempToken(String tempToken) {
        this.tempToken = tempToken;
    }

    public String getCaptcha() {
        return captcha;
    }

    public void setCaptcha(String captcha) {
        this.captcha = captcha;
    }
}
