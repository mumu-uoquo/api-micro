package com.uoquo.scheduler.platform.model.param;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import java.util.Date;

/**
 * 入参：登出日志
 * @author uoquo
 */
@Schema(description = "入参：登出日志")
public class LogUserLogoutParam {

    @Schema(description = "会话ID")
    @NotBlank
    private String token;

    @Schema(description = "请求追踪ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String traceId;

    @Schema(description = "退出状态（081）")
    private String logoutStatus;

    @Schema(description = "退出时间")
    private Date logoutTime;

    @Schema(description = "退出信息")
    private String logoutDesc;

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "操作端KEY")
    private String appKey;

    @Schema(description = "备注")
    private String description;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token == null ? null : token.trim();
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId == null ? null : traceId.trim();
    }

    public String getLogoutStatus() {
        return logoutStatus;
    }

    public void setLogoutStatus(String logoutStatus) {
        this.logoutStatus = logoutStatus == null ? null : logoutStatus.trim();
    }

    public Date getLogoutTime() {
        return logoutTime;
    }

    public void setLogoutTime(Date logoutTime) {
        this.logoutTime = logoutTime;
    }

    public String getLogoutDesc() {
        return logoutDesc;
    }

    public void setLogoutDesc(String logoutDesc) {
        this.logoutDesc = logoutDesc == null ? null : logoutDesc.trim();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description == null ? null : description.trim();
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId == null ? null : userId.trim();
    }

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey == null ? null : appKey.trim();
    }
}