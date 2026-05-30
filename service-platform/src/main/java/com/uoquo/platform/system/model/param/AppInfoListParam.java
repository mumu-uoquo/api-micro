package com.uoquo.platform.system.model.param;

import com.uoquo.web.param.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;


/**
 * 入参：接入授权列表查询
 */
@Schema(description = "接入授权列表查询")
public class AppInfoListParam extends PageRequest {

    @Schema(description = "机构id")
    private String instituteId;

    @Schema(description = "APP名称")
    private String appName;

    @Schema(description = "appkey")
    private String appkey;

    @Schema(description = "关键字")
    private String keyword;

    @Schema(description = "授权根模块")
    private String moduleId;

    @Schema(description = "模板类型")
    private String templateType;

    public String getInstituteId() {
        return instituteId;
    }

    public void setInstituteId(String instituteId) {
        this.instituteId = instituteId;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppkey() {
        return appkey;
    }

    public void setAppkey(String appkey) {
        this.appkey = appkey == null ? null : appkey.trim();
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId == null ? null : moduleId.trim();
    }

    public String getTemplateType() {
        return templateType;
    }

    public void setTemplateType(String templateType) {
        this.templateType = templateType;
    }
}
