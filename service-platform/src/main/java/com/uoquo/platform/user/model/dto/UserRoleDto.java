package com.uoquo.platform.user.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;


/**
 * 出参：用户角色信息（简化RoleInfo的信息）
 */
@Schema(description = "用户角色信息")
public class UserRoleDto {

    @Schema(description = "角色id", requiredMode = Schema.RequiredMode.REQUIRED)
    private String id;

    @Schema(description = "角色名字", requiredMode = Schema.RequiredMode.REQUIRED)
    private String roleName;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }
}
