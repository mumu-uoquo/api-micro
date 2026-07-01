package com.uoquo.platform.auth.model.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * 入参：紧急登录（仅账号 + MFA 验证码，无需密码）
 */
@Schema(description = "紧急登录参数")
public class EmergencyLoginParam extends BasicLoginParam {

    @NotBlank(message = "账号不能为空")
    @Schema(description = "登录账号")
    private String account;

    @NotBlank(message = "MFA验证码不能为空")
    @Schema(description = "MFA动态验证码")
    private String totpCode;

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getTotpCode() {
        return totpCode;
    }

    public void setTotpCode(String totpCode) {
        this.totpCode = totpCode;
    }
}
