package com.uoquo.platform.auth.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 出参：运维登录二维码配置
 */
@Schema(description = "运维登录二维码配置")
public class OpsConfigDto {

    @Schema(description = "二维码图片（base64 data uri）")
    private String qrCode;

    public OpsConfigDto() {
    }

    public OpsConfigDto(String qrCode) {
        this.qrCode = qrCode;
    }

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }
}
