package com.uoquo.platform.system.model.pojo;

import java.util.Date;

/**
 * Table: sys_setting
 */
public class SysSetting {
    /**
     * Column: id
     * Type: VARCHAR(32)
     * Remark: ID
     */
    private String id;

    /**
     * Column: config_name
     * Type: VARCHAR(100)
     * Remark: 配置名称
     */
    private String configName;

    /**
     * Column: config_code
     * Type: VARCHAR(100)
     * Remark: 配置标识
     */
    private String configCode;

    /**
     * Column: config_value
     * Type: VARCHAR(100)
     * Remark: 配置内容
     */
    private String configValue;

    /**
     * Column: description
     * Type: VARCHAR(100)
     * Remark: 备注
     */
    private String description;

    /**
     * Column: public_type
     * Type: CHAR(6)
     * Remark: 作用范围（003）
     */
    private String publicType;

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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    public String getConfigName() {
        return configName;
    }

    public void setConfigName(String configName) {
        this.configName = configName;
    }

    public String getConfigCode() {
        return configCode;
    }

    public void setConfigCode(String configCode) {
        this.configCode = configCode == null ? null : configCode.trim();
    }

    public String getConfigValue() {
        return configValue;
    }

    public void setConfigValue(String configValue) {
        this.configValue = configValue == null ? null : configValue.trim();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPublicType() {
        return publicType;
    }

    public void setPublicType(String publicType) {
        this.publicType = publicType == null ? null : publicType.trim();
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
}