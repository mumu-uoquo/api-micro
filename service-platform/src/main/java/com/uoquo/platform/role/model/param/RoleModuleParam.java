package com.uoquo.platform.role.model.param;

import io.swagger.v3.oas.annotations.media.Schema;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

@Schema(description = "角色模块关联")
public class RoleModuleParam {

    @NotBlank
    @Schema(description = "角色id")
    private String roleId;

    @NotEmpty
    @Schema(description = "模块id集合")
    private List<String> moduleIds;

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public List<String> getModuleIds() {
        return moduleIds;
    }

    public void setModuleIds(List<String> moduleIds) {
        this.moduleIds = moduleIds;
    }
}
