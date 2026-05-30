package com.uoquo.platform.role.model.param;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

@Schema(description = "模块资源关联")
public class ModuleResourceParam {

    @NotBlank
    @Schema(description = "模块id")
    private String moduleId;

    @Schema(description = "资源集合")
    @NotEmpty
    private List<String> resourceIdList;

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }

    public List<String> getResourceIdList() {
        return resourceIdList;
    }

    public void setResourceIdList(List<String> resourceIdList) {
        this.resourceIdList = resourceIdList;
    }
}
