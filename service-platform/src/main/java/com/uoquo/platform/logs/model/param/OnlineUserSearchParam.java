package com.uoquo.platform.logs.model.param;

import com.uoquo.web.param.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 入参：在线用户列表查询
 * @author uoquo
 */
@Schema(description = "入参：在线用户列表查询")
public class OnlineUserSearchParam extends PageRequest {

    @Schema(description = "用户名（模糊匹配）")
    private String userName;

    @Schema(description = "所属企业ID（精确匹配）")
    private String instituteId;

    @Schema(description = "登录IP（精确匹配）")
    private String loginIp;

    @Schema(description = "应用平台ID（精确匹配）")
    private String appModuleId;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName == null ? null : userName.trim();
    }

    public String getInstituteId() {
        return instituteId;
    }

    public void setInstituteId(String instituteId) {
        this.instituteId = instituteId == null ? null : instituteId.trim();
    }

    public String getLoginIp() {
        return loginIp;
    }

    public void setLoginIp(String loginIp) {
        this.loginIp = loginIp == null ? null : loginIp.trim();
    }

    public String getAppModuleId() {
        return appModuleId;
    }

    public void setAppModuleId(String appModuleId) {
        this.appModuleId = appModuleId == null ? null : appModuleId.trim();
    }
}
