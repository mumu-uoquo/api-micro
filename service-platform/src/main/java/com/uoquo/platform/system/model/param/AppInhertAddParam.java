package com.uoquo.platform.system.model.param;

import io.swagger.v3.oas.annotations.media.Schema;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 入参：APP授权继承
 */
@Schema(description = "APP授权继承")
public class AppInhertAddParam {

    @Schema(description = "接入ID")
    @NotBlank
    private String appId;

    @Schema(description = "父级APPID")
    @NotEmpty
    private List<String> parentIdList;

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public List<String> getParentIdList() {
        return parentIdList;
    }

    public void setParentIdList(List<String> parentIdList) {
        this.parentIdList = parentIdList;
    }
}
