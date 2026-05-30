package com.uoquo.platform.auth.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 出参：TOTP 二维码信息
 *
 * @author xuhz
 */
@Schema(description = "TOTP 二维码信息")
public class TotpDto {

    @Schema(description = "otpauth URI（用于生成二维码）")
    private String otpAuthUri;

    @Schema(description = "二维码 Base64 图片")
    private String qrCodeBase64;

    public String getOtpAuthUri() {
        return otpAuthUri;
    }

    public void setOtpAuthUri(String otpAuthUri) {
        this.otpAuthUri = otpAuthUri;
    }

    public String getQrCodeBase64() {
        return qrCodeBase64;
    }

    public void setQrCodeBase64(String qrCodeBase64) {
        this.qrCodeBase64 = qrCodeBase64;
    }
}
