package com.uoquo.platform.role.model.param;

import com.uoquo.platform.role.model.pojo.ModuleParam;
import io.swagger.v3.oas.annotations.media.Schema;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * 入参：添加/修改模块
 */
@Schema(description = "添加/修改模块")
public class ModuleInfoParam {

    @Schema(description = "主键")
    private String id;

    @Schema(description = "父模块id")
    private String parentId;

    @NotBlank
    @Schema(description = "模块名字")
    private String moduleName;

    @NotBlank
    @Schema(description = "模块编码")
    private String moduleCode;

    @NotBlank
    @Schema(description = "模块类型")
    private String moduleType;

    @Schema(description = "菜单名称")
    private String menuName;

    @Schema(description = "是否可见")
    private Boolean visible;

    @Schema(description = "图标")
    private String icon;

    @Schema(description = "微前端名，仅允许字母、数字、中横线和下划线")
    @Size(max = 20, message = "微前端名长度不能超过20个字符")
    @Pattern(regexp = "^[a-zA-Z0-9_-]*$", message = "微前端名只能包含字母、数字、下划线和中横线")
    private String microApp;

    @Schema(description = "菜单路由")
    private String path;

    @Schema(description = "跳转链接")
    private String url;

    @Schema(description = "请求参数")
    private List<ModuleParam> params;

    @Schema(description = "是否新页打开")
    private Boolean popup;

    @Schema(description = "排序")
    private int sortIdx;

    @Schema(description = "备注描述")
    private String description;

    @Schema(description = "授权角色（仅当前模块，不级联处理）")
    private List<String> roleIdList;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getModuleCode() {
        return moduleCode;
    }

    public void setModuleCode(String moduleCode) {
        this.moduleCode = moduleCode;
    }

    public String getModuleType() {
        return moduleType;
    }

    public void setModuleType(String moduleType) {
        this.moduleType = moduleType;
    }

    public String getMenuName() {
        return menuName;
    }

    public void setMenuName(String menuName) {
        this.menuName = menuName;
    }

    public Boolean getVisible() {
        return visible;
    }

    public void setVisible(Boolean visible) {
        this.visible = visible;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getMicroApp() {
        return microApp;
    }

    public void setMicroApp(String microApp) {
        this.microApp = microApp == null ? null : microApp.trim();
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<ModuleParam> getParams() {
        return params;
    }

    public void setParams(List<ModuleParam> params) {
        this.params = params;
    }

    public Boolean getPopup() {
        return popup;
    }

    public void setPopup(Boolean popup) {
        this.popup = popup;
    }

    public int getSortIdx() {
        return sortIdx;
    }

    public void setSortIdx(int sortIdx) {
        this.sortIdx = sortIdx;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getRoleIdList() {
        return roleIdList;
    }

    public void setRoleIdList(List<String> roleIdList) {
        this.roleIdList = roleIdList;
    }
}
