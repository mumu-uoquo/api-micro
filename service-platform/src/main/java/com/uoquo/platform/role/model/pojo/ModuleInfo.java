package com.uoquo.platform.role.model.pojo;

import java.util.Date;
import java.util.List;

/**
 * Table: bko_module
 */
public class ModuleInfo {
    /**
     * Column: id
     * Type: VARCHAR(32)
     * Remark: 模块id
     */
    private String id;

    /**
     * Column: parent_id
     * Type: VARCHAR(32)
     * Remark: 父级id
     */
    private String parentId;

    /**
     * Column: module_name
     * Type: VARCHAR(24)
     * Remark: 模块名称
     */
    private String moduleName;

    /**
     * Column: module_code
     * Type: VARCHAR(50)
     * Remark: 模块编码
     */
    private String moduleCode;

    /**
     * Column: module_type
     * Type: CHAR(6)
     * Remark: 模块类型
     */
    private String moduleType;

    /**
     * Column: is_visible
     * Type: BIT
     * Default value: 1
     * Remark: 是否可见
     */
    private Boolean visible;

    /**
     * Column: menu_name
     * Type: VARCHAR(24)
     * Remark: 菜单名称
     */
    private String menuName;

    /**
     * Column: icon
     * Type: VARCHAR(50)
     * Remark: 图标
     */
    private String icon;

    /**
     * Column: micro_app
     * Type: VARCHAR(20)
     * Remark: 微前端名
     */
    private String microApp;

    /**
     * Column: path
     * Type: VARCHAR(100)
     * Remark: 菜单路由
     */
    private String path;

    /**
     * Column: url
     * Type: VARCHAR(100)
     * Remark: 跳转链接
     */
    private String url;

    /**
     * Column: params
     * Type: VARCHAR(500)
     * Remark: 请求参数
     */
    private List<ModuleParam> params;

    /**
     * Column: popup
     * Type: BIT
     * Remark: 是否新页打开
     */
    private Boolean popup;

    /**
     * Column: sort_idx
     * Type: INT
     * Default value: 99
     * Remark: 排序
     */
    private Integer sortIdx;

    /**
     * Column: description
     * Type: VARCHAR(100)
     * Remark: 备注描述
     */
    private String description;

    /**
     * Column: create_user
     * Type: VARCHAR(32)
     * Remark: 创建人
     */
    private String createUser;

    /**
     * Column: create_time
     * Type: DATETIME
     * Remark: 创建时间
     */
    private Date createTime;

    /**
     * Column: update_user
     * Type: VARCHAR(32)
     * Remark: 更新人
     */
    private String updateUser;

    /**
     * Column: update_time
     * Type: DATETIME
     * Remark: 更新时间
     */
    private Date updateTime;

    /**
     * Column: delete_state
     * Type: BIGINT
     * Default value: 0
     * Remark: 删除标识
     */
    private Long deleteState;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId == null ? null : parentId.trim();
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName == null ? null : moduleName.trim();
    }

    public String getModuleCode() {
        return moduleCode;
    }

    public void setModuleCode(String moduleCode) {
        this.moduleCode = moduleCode == null ? null : moduleCode.trim();
    }

    public String getModuleType() {
        return moduleType;
    }

    public void setModuleType(String moduleType) {
        this.moduleType = moduleType == null ? null : moduleType.trim();
    }

    public Boolean getVisible() {
        return visible;
    }

    public void setVisible(Boolean visible) {
        this.visible = visible;
    }

    public String getMenuName() {
        return menuName;
    }

    public void setMenuName(String menuName) {
        this.menuName = menuName == null ? null : menuName.trim();
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon == null ? null : icon.trim();
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
        this.url = url == null ? null : url.trim();
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

    public Integer getSortIdx() {
        return sortIdx;
    }

    public void setSortIdx(Integer sortIdx) {
        this.sortIdx = sortIdx;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description == null ? null : description.trim();
    }

    public String getCreateUser() {
        return createUser;
    }

    public void setCreateUser(String createUser) {
        this.createUser = createUser == null ? null : createUser.trim();
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getUpdateUser() {
        return updateUser;
    }

    public void setUpdateUser(String updateUser) {
        this.updateUser = updateUser == null ? null : updateUser.trim();
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Long getDeleteState() {
        return deleteState;
    }

    public void setDeleteState(Long deleteState) {
        this.deleteState = deleteState;
    }
}