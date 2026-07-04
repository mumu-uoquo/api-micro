package com.uoquo.platform.auth.model.param;

import com.uoquo.annotation.json.Sensitive;
import com.uoquo.annotation.json.SensitiveType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * 入参：TOTP 双因子验证登录
 */
@Schema(description = "TOTP双因子验证登录")
public class MfaLoginParam extends BasicLoginParam {

    @NotBlank(message = "动态码不能为空")
    @Schema(description = "双因子动态码")
    @Sensitive(type = SensitiveType.CRYPT_RSA)
    private String totpCode;

    @NotBlank(message = "临时Token不能为空")
    @Schema(description = "TOTP验证临时Token")
    private String tempToken;

    public String getTotpCode() {
        return totpCode;
    }

    public void setTotpCode(String totpCode) {
        this.totpCode = totpCode;
    }

    public String getTempToken() {
        return tempToken;
    }

    public void setTempToken(String tempToken) {
        this.tempToken = tempToken;
    }
}
