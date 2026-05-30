package com.uoquo.platform.logs.model.pojo;

import java.util.Date;

/**
 * Table: log_data_access
 */
public class LogBusinessAccess {
    /**
     * Column: id
     * Type: VARCHAR(32)
     * Remark: 日志ID
     */
    private String id;

    /**
     * Column: token
     * Type: VARCHAR(32)
     * Remark: 会话ID
     */
    private String token;

    /**
     * Column: trace_id
     * Type: VARCHAR(32)
     * Remark: 请求追踪ID
     */
    private String traceId;

    /**
     * Column: business_type
     * Type: CHAR(6)
     * Remark: 业务类型（009）
     */
    private String businessType;

    /**
     * Column: business_sub_type
     * Type: VARCHAR(20)
     * Remark: 业务子类
     */
    private String businessSubType;

    /**
     * Column: business_id
     * Type: VARCHAR(32)
     * Remark: 业务ID
     */
    private String businessId;

    /**
     * Column: business_institute_id
     * Type: VARCHAR(32)
     * Remark: 数据所属企业 ID
     */
    private String businessInstituteId;

    /**
     * Column: table_name
     * Type: VARCHAR(50)
     * Remark: 数据表名
     */
    private String tableName;

    /**
     * Column: operator_id
     * Type: VARCHAR(32)
     * Remark: 操作人ID
     */
    private String operatorId;

    /**
     * Column: operator_name
     * Type: VARCHAR(50)
     * Remark: 操作人姓名
     */
    private String operatorName;

    /**
     * Column: access_type
     * Type: CHAR(6)
     * Remark: 访问类型（010）
     */
    private String accessType;

    /**
     * Column: access_time
     * Type: DATETIME
     * Remark: 访问时间
     */
    private Date accessTime;

    /**
     * Column: access_condition
     * Type: VARCHAR(500)
     * Remark: 访问条件
     */
    private String accessCondition;

    /**
     * Column: access_ip
     * Type: VARCHAR(50)
     * Remark: 访问IP
     */
    private String accessIp;

    /**
     * Column: description
     * Type: VARCHAR(200)
     * Remark: 备注
     */
    private String description;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token == null ? null : token.trim();
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId == null ? null : traceId.trim();
    }

    public String getBusinessType() {
        return businessType;
    }

    public void setBusinessType(String businessType) {
        this.businessType = businessType == null ? null : businessType.trim();
    }

    public String getBusinessSubType() {
        return businessSubType;
    }

    public void setBusinessSubType(String businessSubType) {
        this.businessSubType = businessSubType;
    }

    public String getBusinessId() {
        return businessId;
    }

    public void setBusinessId(String businessId) {
        this.businessId = businessId == null ? null : businessId.trim();
    }

    public String getBusinessInstituteId() {
        return businessInstituteId;
    }

    public void setBusinessInstituteId(String businessInstituteId) {
        this.businessInstituteId = businessInstituteId;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName == null ? null : tableName.trim();
    }

    public String getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(String operatorId) {
        this.operatorId = operatorId == null ? null : operatorId.trim();
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName == null ? null : operatorName.trim();
    }

    public String getAccessType() {
        return accessType;
    }

    public void setAccessType(String accessType) {
        this.accessType = accessType == null ? null : accessType.trim();
    }

    public Date getAccessTime() {
        return accessTime;
    }

    public void setAccessTime(Date accessTime) {
        this.accessTime = accessTime;
    }

    public String getAccessCondition() {
        return accessCondition;
    }

    public void setAccessCondition(String accessCondition) {
        this.accessCondition = accessCondition == null ? null : accessCondition.trim();
    }

    public String getAccessIp() {
        return accessIp;
    }

    public void setAccessIp(String accessIp) {
        this.accessIp = accessIp == null ? null : accessIp.trim();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description == null ? null : description.trim();
    }
}