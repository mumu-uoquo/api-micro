package com.uoquo.platform.logs.model.pojo;

import java.util.Date;
import java.util.Map;

/**
 * Table: log_business_change
 */
public class LogBusinessChange {
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
     * Column: operation_type
     * Type: CHAR(6)
     * Remark: 操作类型（010）
     */
    private String operationType;

    /**
     * Column: operation_time
     * Type: DATETIME
     * Remark: 操作时间
     */
    private Date operationTime;

    /**
     * Column: operation_desc
     * Type: VARCHAR(500)
     * Remark: 操作描述
     */
    private String operationDesc;

    /**
     * Column: before_data
     * Type: JSON(0)
     * Remark: 变更前数据
     */
    private Map<String, Object> beforeData;

    /**
     * Column: after_data
     * Type: JSON(0)
     * Remark: 变更后数据
     */
    private Map<String, Object> afterData;

    /**
     * Column: change_fields
     * Type: VARCHAR(500)
     * Remark: 变更字段
     */
    private String changeFields;

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

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType == null ? null : operationType.trim();
    }

    public Date getOperationTime() {
        return operationTime;
    }

    public void setOperationTime(Date operationTime) {
        this.operationTime = operationTime;
    }

    public String getOperationDesc() {
        return operationDesc;
    }

    public void setOperationDesc(String operationDesc) {
        this.operationDesc = operationDesc == null ? null : operationDesc.trim();
    }

    public Map<String, Object> getBeforeData() {
        return beforeData;
    }

    public void setBeforeData(Map<String, Object> beforeData) {
        this.beforeData = beforeData;
    }

    public Map<String, Object> getAfterData() {
        return afterData;
    }

    public void setAfterData(Map<String, Object> afterData) {
        this.afterData = afterData;
    }

    public String getChangeFields() {
        return changeFields;
    }

    public void setChangeFields(String changeFields) {
        this.changeFields = changeFields == null ? null : changeFields.trim();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description == null ? null : description.trim();
    }
}