package com.uoquo.platform.auth.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 出参：第三方扫码登录配置
 * <ul>
 *     <li>wechat：appid + redirectUri + state</li>
 *     <li>wecom：appid(corpid) + agentId + redirectUri + state</li>
 * </ul>
 */
@Schema(description = "第三方扫码登录配置")
public class CredentialConfigDto {

    @Schema(description = "场景（wechat/wecom）")
    private String scene;

    @Schema(description = "应用 appid（微信 appid 或企业微信 corpid）")
    private String appid;

    @Schema(description = "企业微信应用 agentId（仅 wecom 返回）")
    private String agentId;

    @Schema(description = "授权回调地址")
    private String redirectUri;

    @Schema(description = "本次授权的 state（用于回调与状态轮询）")
    private String state;

    @Schema(description = "渲染方式：wxjs=集成微信官方 JS（WxLogin / 企微 JS-SDK），oauth=自行拼接 OAuth2 URL 并展示二维码")
    private String renderType;

    public String getScene() {
        return scene;
    }

    public void setScene(String scene) {
        this.scene = scene;
    }

    public String getAppid() {
        return appid;
    }

    public void setAppid(String appid) {
        this.appid = appid;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getRenderType() {
        return renderType;
    }

    public void setRenderType(String renderType) {
        this.renderType = renderType;
    }
}
