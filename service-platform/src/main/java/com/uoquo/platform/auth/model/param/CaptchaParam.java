package com.uoquo.platform.auth.model.param;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 入参：获取图形验证码
 */
@Schema(description = "获取图形验证码")
public class CaptchaParam {

    /**
     * 验证码使用场景：
     * <ul>
     *     <li>login    - 账号密码登录（密码多次出错后触发）</li>
     *     <li>register - 用户注册</li>
     *     <li>phone    - 获取手机短信码前的人机验证</li>
     * </ul>
     */
    @Schema(description = "使用场景：login / register / phone", example = "login")
    private String scene;

    public String getScene() {
        return scene;
    }

    public void setScene(String scene) {
        this.scene = scene;
    }
}
