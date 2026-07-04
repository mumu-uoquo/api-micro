package com.uoquo.platform.auth.model.param;

import com.uoquo.annotation.json.Sensitive;
import com.uoquo.annotation.json.SensitiveType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * 入参：获取运维登录二维码
 */
@Schema(description = "获取运维登录二维码")
public class OpsConfigParam {

    @NotBlank(message = "账号不能为空")
    @Schema(description = "运维账号")
    @Sensitive(type = SensitiveType.CRYPT_RSA)
    private String account;

    @NotBlank(message = "手机号不能为空")
    @Schema(description = "手机号")
    @Sensitive(type = SensitiveType.CRYPT_RSA)
    private String phone;

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
