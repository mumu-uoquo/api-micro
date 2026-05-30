package com.uoquo.platform.auth.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 出参：Token信息
 * @author xuhz
 */
@Schema(description = "出参：Token信息")
public class TokenDto {

    @Schema(description = "会话token", requiredMode = Schema.RequiredMode.REQUIRED)
    private String accessToken;

    @Schema(description = "刷新token", requiredMode = Schema.RequiredMode.REQUIRED)
    private String refreshToken;

    @Schema(description = "过期时间（秒）", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer expireTime;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public Integer getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(Integer expireTime) {
        this.expireTime = expireTime;
    }
}
