package com.uoquo.platform.user.model.pojo;

import java.util.Date;

/**
 * Table: bko_user_credential
 */
public class UserCredential {
    /**
     * Column: id
     * Type: VARCHAR(26)
     * Remark: 主键 ULID
     */
    private String id;

    /**
     * Column: user_id
     * Type: VARCHAR(26)
     * Remark: 用户ID，关联 bko_user.id
     */
    private String userId;

    /**
     * Column: credential_type
     * Type: VARCHAR(20)
     * Remark: 凭证类型（weixin/wecom）
     */
    private String credentialType;

    /**
     * Column: credential_value
     * Type: VARCHAR(200)
     * Remark: 凭证标识值（如微信 openid）
     */
    private String credentialValue;

    /**
     * Column: institute_id
     * Type: VARCHAR(26)
     * Remark: 所属企业，全局唯一类型填 NULL
     */
    private String instituteId;

    /**
     * Column: create_time
     * Type: DATETIME
     * Remark: 创建时间
     */
    private Date createTime;

    /**
     * Column: update_time
     * Type: DATETIME
     * Remark: 更新时间
     */
    private Date updateTime;

    /**
     * Column: delete_state
     * Type: TINYINT
     * Default value: 0
     * Remark: 删除标识
     */
    private Integer deleteState;

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

    public String getCredentialType() {
        return credentialType;
    }

    public void setCredentialType(String credentialType) {
        this.credentialType = credentialType == null ? null : credentialType.trim();
    }

    public String getCredentialValue() {
        return credentialValue;
    }

    public void setCredentialValue(String credentialValue) {
        this.credentialValue = credentialValue == null ? null : credentialValue.trim();
    }

    public String getInstituteId() {
        return instituteId;
    }

    public void setInstituteId(String instituteId) {
        this.instituteId = instituteId == null ? null : instituteId.trim();
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Integer getDeleteState() {
        return deleteState;
    }

    public void setDeleteState(Integer deleteState) {
        this.deleteState = deleteState;
    }
}
