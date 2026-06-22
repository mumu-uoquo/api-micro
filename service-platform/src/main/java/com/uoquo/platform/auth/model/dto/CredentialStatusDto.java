package com.uoquo.platform.auth.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 出参：第三方扫码登录状态
 */
@Schema(description = "第三方扫码登录状态")
public class CredentialStatusDto {

    @Schema(description = "状态：waiting=等待授权，confirmed=已回调拿到 code")
    private String status;

    @Schema(description = "第三方回调的 code（confirmed 时有值）")
    private String code;

    public CredentialStatusDto() {
    }

    public CredentialStatusDto(String status, String code) {
        this.status = status;
        this.code = code;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
