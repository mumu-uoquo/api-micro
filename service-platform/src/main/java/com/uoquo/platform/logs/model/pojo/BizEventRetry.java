package com.uoquo.platform.logs.model.pojo;

import java.util.Date;

/**
 * 业务事件重试记录
 */
public class BizEventRetry {

    /**
     * Column: id
     * Type: VARCHAR(32)
     * Remark: 日志 ID
     */
    private String id;

    /**
     * Column: record_id
     * Type: VARCHAR(32)
     * Remark: 记录 ID
     */
    private String recordId;

    /**
     * Column: business_id
     * Type: VARCHAR(32)
     * Remark: 业务数据 ID
     */
    private String businessId;

    /**
     * Column: business_institute_id
     * Type: VARCHAR(32)
     * Remark: 数据所属企业 ID
     */
    private String businessInstituteId;

    /**
     * Column: token
     * Type: VARCHAR(32)
     * Remark: 会话 ID
     */
    private String token;

    /**
     * Column: trace_id
     * Type: VARCHAR(32)
     * Remark: 请求追踪 ID
     */
    private String traceId;

    /**
     * Column: operator_id
     * Type: VARCHAR(32)
     * Remark: 操作人 ID
     */
    private String operatorId;

    /**
     * Column: operator_name
     * Type: VARCHAR(50)
     * Remark: 操作人姓名
     */
    private String operatorName;

    /**
     * Column: operator_institute_id
     * Type: VARCHAR(32)
     * Remark: 操作人所属企业 ID
     */
    private String operatorInstituteId;

    /**
     * Column: operation_time
     * Type: DATETIME
     * Remark: 操作时间
     */
    private Date operationTime;

    /**
     * Column: operation_type
     * Type: CHAR(6)
     * Remark: 操作类型（010）
     */
    private String operationType;

    /**
     * Column: operation_status
     * Type: VARCHAR(10)
     * Remark: 操作状态（同响应码）
     */
    private String operationStatus;

    /**
     * Column: operation_desc
     * Type: VARCHAR(500)
     * Remark: 操作描述
     */
    private String operationDesc;

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
        this.id = id;
    }

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    public String getBusinessId() {
        return businessId;
    }

    public void setBusinessId(String businessId) {
        this.businessId = businessId;
    }

    public String getBusinessInstituteId() {
        return businessInstituteId;
    }

    public void setBusinessInstituteId(String businessInstituteId) {
        this.businessInstituteId = businessInstituteId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(String operatorId) {
        this.operatorId = operatorId;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    public String getOperatorInstituteId() {
        return operatorInstituteId;
    }

    public void setOperatorInstituteId(String operatorInstituteId) {
        this.operatorInstituteId = operatorInstituteId;
    }

    public Date getOperationTime() {
        return operationTime;
    }

    public void setOperationTime(Date operationTime) {
        this.operationTime = operationTime;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public String getOperationStatus() {
        return operationStatus;
    }

    public void setOperationStatus(String operationStatus) {
        this.operationStatus = operationStatus;
    }

    public String getOperationDesc() {
        return operationDesc;
    }

    public void setOperationDesc(String operationDesc) {
        this.operationDesc = operationDesc;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
