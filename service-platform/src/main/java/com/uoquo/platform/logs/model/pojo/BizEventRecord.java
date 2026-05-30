package com.uoquo.platform.logs.model.pojo;

import java.util.Date;
import java.util.Map;

/**
 * 业务事件原始记录
 */
public class BizEventRecord {

    /**
     * Column: id
     * Type: VARCHAR(32)
     * Remark: 日志 ID
     */
    private String id;

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
     * Column: business_table
     * Type: VARCHAR(50)
     * Remark: 业务数据的表名
     */
    private String businessTable;

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
     * Column: operation_content
     * Type: JSON(0)
     * Remark: 操作内容
     */
    private Map<String, Object> operationContent;

    /**
     * Column: change_data
     * Type: JSON(0)
     * Remark: 变更数据
     */
    private Map<String, Object> changeData;

    /**
     * Column: change_fields
     * Type: VARCHAR(500)
     * Remark: 变更字段
     */
    private String changeFields;

    /**
     * Column: remote_event
     * Type: BOOLEAN
     * Remark: 是否远程事件
     */
    private Boolean remoteEvent;

    /**
     * Column: event_class
     * Type: VARCHAR(200)
     * Remark: 事件类名
     */
    private String eventClass;

    /**
     * Column: event_content
     * Type: TEXT
     * Remark: 事件内容
     */
    private String eventContent;

    /**
     * Column: retry_count
     * Type: int
     * Remark: 重试次数
     */
    private Integer retryCount;

    /**
     * Column: retry_operator_id
     * Type: VARCHAR(32)
     * Remark: 最后重试操作人 ID
     */
    private String retryOperatorId;

    /**
     * Column: retry_operator_name
     * Type: VARCHAR(50)
     * Remark: 最后重试操作人姓名
     */
    private String retryOperatorName;

    /**
     * Column: retry_time
     * Type: DATETIME
     * Remark: 最后重试时间
     */
    private Date retryTime;

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

    public String getBusinessType() {
        return businessType;
    }

    public void setBusinessType(String businessType) {
        this.businessType = businessType;
    }

    public String getBusinessSubType() {
        return businessSubType;
    }

    public void setBusinessSubType(String businessSubType) {
        this.businessSubType = businessSubType;
    }

    public String getBusinessTable() {
        return businessTable;
    }

    public void setBusinessTable(String businessTable) {
        this.businessTable = businessTable;
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

    public Map<String, Object> getOperationContent() {
        return operationContent;
    }

    public void setOperationContent(Map<String, Object> operationContent) {
        this.operationContent = operationContent;
    }

    public Map<String, Object> getChangeData() {
        return changeData;
    }

    public void setChangeData(Map<String, Object> changeData) {
        this.changeData = changeData;
    }

    public String getChangeFields() {
        return changeFields;
    }

    public void setChangeFields(String changeFields) {
        this.changeFields = changeFields == null ? null : changeFields.trim();
    }

    public Boolean getRemoteEvent() {
        return remoteEvent;
    }

    public void setRemoteEvent(Boolean remoteEvent) {
        this.remoteEvent = remoteEvent;
    }

    public String getEventClass() {
        return eventClass;
    }

    public void setEventClass(String eventClass) {
        this.eventClass = eventClass;
    }

    public String getEventContent() {
        return eventContent;
    }

    public void setEventContent(String eventContent) {
        this.eventContent = eventContent;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    public String getRetryOperatorId() {
        return retryOperatorId;
    }

    public void setRetryOperatorId(String retryOperatorId) {
        this.retryOperatorId = retryOperatorId;
    }

    public String getRetryOperatorName() {
        return retryOperatorName;
    }

    public void setRetryOperatorName(String retryOperatorName) {
        this.retryOperatorName = retryOperatorName;
    }

    public Date getRetryTime() {
        return retryTime;
    }

    public void setRetryTime(Date retryTime) {
        this.retryTime = retryTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
