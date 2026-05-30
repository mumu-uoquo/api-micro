package com.uoquo.platform.role.model.dto;

import com.uoquo.platform.role.model.pojo.ModuleParam;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.List;

/**
 * 出参：模块信息（树状）
 */
@Schema(description = "模块信息（树状）")
public class ModuleTreeDto {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED)
    private String id;

    @Schema(description = "父模块id")
    private String parentId;

    @Schema(description = "模块名字")
    private String moduleName;

    @Schema(description = "模块编码")
    private String moduleCode;

    @Schema(description = "模块类型")
    private String moduleType;

    @Schema(description = "菜单名称")
    private String menuName;

    @Schema(description = "是否可见")
    private Boolean visible;

    @Schema(description = "图标")
    private String icon;

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

    @Schema(description = "子节点")
    private List<ModuleTreeDto> children;

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

    public List<ModuleTreeDto> getChildren() {
        return children;
    }

    public void setChildren(List<ModuleTreeDto> children) {
        this.children = children;
    }
}
