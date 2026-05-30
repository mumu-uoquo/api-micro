package com.uoquo.platform.role.model.pojo;

import java.util.Date;

/**
 * Table: bko_resource
 */
public class ResourceInfo {
    /**
     * Column: id
     * Type: VARCHAR(32)
     * Remark: ID
     */
    private String id;

    /**
     * Column: resource_name
     * Type: VARCHAR(100)
     * Remark: 资源名称
     */
    private String resourceName;

    /**
     * Column: resource_url
     * Type: VARCHAR(100)
     * Remark: 资源URL
     */
    private String resourceUrl;

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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName == null ? null : resourceName.trim();
    }

    public String getResourceUrl() {
        return resourceUrl;
    }

    public void setResourceUrl(String resourceUrl) {
        this.resourceUrl = resourceUrl == null ? null : resourceUrl.trim();
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
}