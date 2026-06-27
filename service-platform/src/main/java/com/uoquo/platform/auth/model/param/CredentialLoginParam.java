package com.uoquo.platform.auth.model.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * 入参：第三方凭证登录
 */
@Schema(description = "第三方凭证登录")
public class CredentialLoginParam extends BasicLoginParam {

    @NotBlank(message = "凭证类型不能为空")
    @Schema(description = "凭证类型（wechat/wecom）")
    private String credentialType;

    @NotBlank(message = "凭证标识不能为空")
    @Schema(description = "凭证标识值（如微信 openid）")
    private String credentialValue;

    @NotBlank(message = "state不能为空")
    @Schema(description = "授权 state（须与 /credential/config 下发的一致）")
    private String state;

    public String getCredentialType() {
        return credentialType;
    }

    public void setCredentialType(String credentialType) {
        this.credentialType = credentialType;
    }

    public String getCredentialValue() {
        return credentialValue;
    }

    public void setCredentialValue(String credentialValue) {
        this.credentialValue = credentialValue;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
