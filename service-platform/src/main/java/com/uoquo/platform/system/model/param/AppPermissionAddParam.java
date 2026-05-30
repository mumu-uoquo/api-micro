package com.uoquo.platform.system.model.param;

import io.swagger.v3.oas.annotations.media.Schema;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 入参：APP授权
 */
@Schema(description = "APP授权")
public class AppPermissionAddParam {

    @Schema(description = "接入ID")
    @NotBlank
    private String appId;

    @Schema(description = "资源集合")
    @NotEmpty
    private List<String> resourceIdList;

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public List<String> getResourceIdList() {
        return resourceIdList;
    }

    public void setResourceIdList(List<String> resourceIdList) {
        this.resourceIdList = resourceIdList;
    }
}
