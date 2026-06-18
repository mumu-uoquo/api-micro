package com.uoquo.platform.auth.model.param;

import com.uoquo.annotation.json.Sensitive;
import com.uoquo.annotation.json.SensitiveType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * 入参：手机号短信码登录
 */
@Schema(description = "手机号短信码登录")
public class SmsLoginParam extends BasicLoginParam {

    @NotBlank(message = "手机号不能为空")
    @Sensitive(type = SensitiveType.CRYPT_RSA)
    @Schema(description = "手机号（RSA 加密）")
    private String phone;

    @NotBlank(message = "短信验证码不能为空")
    @Schema(description = "短信验证码")
    private String smsCode;

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
}
