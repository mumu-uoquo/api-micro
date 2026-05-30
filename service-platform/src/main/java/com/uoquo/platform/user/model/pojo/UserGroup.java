package com.uoquo.platform.user.model.pojo;

/**
 * Table: bko_user_group
 */
public class UserGroup {
    /**
     * Column: id
     * Type: VARCHAR(32)
     * Remark: ID
     */
    private String id;

    /**
     * Column: user_id
     * Type: VARCHAR(32)
     * Remark: 用户ID
     */
    private String userId;

    /**
     * Column: group_id
     * Type: VARCHAR(32)
     * Remark: 组ID
     */
    private String groupId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId == null ? null : userId.trim();
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId == null ? null : groupId.trim();
    }
}