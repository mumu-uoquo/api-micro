package com.uoquo.platform.auth.model.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 入参：获取手机短信验证码
 */
@Schema(description = "获取手机短信验证码")
public class PhoneCaptchaParam {

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "手机号", requiredMode = Schema.RequiredMode.REQUIRED)
    private String phone;

    /**
     * 使用场景：
     * <ul>
     *     <li>sms_login  - 短信码登录</li>
     *     <li>register   - 用户注册</li>
     * </ul>
     */
    @NotBlank(message = "场景不能为空")
    @Schema(description = "使用场景：sms_login / register（需与获取图形验证码时的 scene 一致）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String scene;

    @Schema(description = "图形验证码（若当前场景需要则必填）")
    private String captcha;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getScene() {
        return scene;
    }

    public void setScene(String scene) {
        this.scene = scene;
    }

    public String getCaptcha() {
        return captcha;
    }

    public void setCaptcha(String captcha) {
        this.captcha = captcha;
    }
}
