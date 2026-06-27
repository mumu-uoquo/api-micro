package com.uoquo.platform.auth.model.param;

import com.uoquo.annotation.json.Sensitive;
import com.uoquo.annotation.json.SensitiveType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 入参：用户注册（手机号 + 短信码 + 账号 + 密码）
 */
@Schema(description = "用户注册")
public class RegisterParam {

    @NotBlank(message = "用户所属机构不能为空")
    @Schema(description = "用户所属机构id")
    private String instituteId;

    @NotBlank(message = "手机号不能为空")
    @Schema(description = "手机号（RSA 加密）")
    @Sensitive(type = SensitiveType.CRYPT_RSA)
    private String phone;

    @NotBlank(message = "短信验证码不能为空")
    @Schema(description = "短信验证码")
    private String smsCode;

    @NotBlank(message = "用户账号不能为空")
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9_-]*$", message = "用户账号只能包含字母、数字、下划线、中横线，且必须以字母开头")
    @Schema(description = "用户账号")
    private String userName;

    @NotBlank(message = "密码不能为空")
    @Schema(description = "用户密码（RSA 加密）")
    @Sensitive(type = SensitiveType.CRYPT_RSA)
    private String password;

    @Schema(description = "真实姓名")
    private String realName;

    public String getInstituteId() {
        return instituteId;
    }

    public void setInstituteId(String instituteId) {
        this.instituteId = instituteId;
    }

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

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }
}
