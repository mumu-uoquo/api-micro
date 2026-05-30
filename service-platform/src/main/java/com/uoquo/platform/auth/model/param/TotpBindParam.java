package com.uoquo.platform.auth.model.param;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;

/**
 * 入参：TOTP 绑定
 *
 * @author xuhz
 */
@Schema(description = "TOTP 绑定参数")
public class TotpBindParam {

    @NotBlank
    @Schema(description = "TOTP 动态码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String totpCode;

    public String getTotpCode() {
        return totpCode;
    }

    public void setTotpCode(String totpCode) {
        this.totpCode = totpCode;
    }
}
