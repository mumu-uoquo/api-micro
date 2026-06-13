package com.uoquo.platform.logs.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Date;
import java.util.Map;

/**
 * 出参：业务事件记录 <br>
 * 注：去除了事件详细内容，一是减少数据量，另一个是防止信息泄露
 */
@Schema(description = "出参：业务事件记录")
public class BizEventRecordDto {

    @Schema(description = "日志 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String id;

    @Schema(description = "会话 ID")
    private String token;

    @Schema(description = "请求追踪 ID")
    private String traceId;

    @Schema(description = "业务类型（009）")
    private String businessType;

    @Schema(description = "业务子类")
    private String businessSubType;

    @Schema(description = "业务数据的表名")
    private String businessTable;

    @Schema(description = "业务数据 ID")
    private String businessId;

    @Schema(description = "数据所属企业 ID")
    private String businessInstituteId;

    @Schema(description = "操作人 ID")
    private String operatorId;

    @Schema(description = "操作人姓名")
    private String operatorName;

    @Schema(description = "操作人所属企业 ID")
    private String operatorInstituteId;

    @Schema(description = "操作时间")
    private Date operationTime;

    @Schema(description = "操作类型（010）")
    private String operationType;

    @Schema(description = "操作状态（同响应码）")
    private String operationStatus;

    @Schema(description = "操作描述")
    private String operationDesc;

    @Schema(description = "操作内容")
    private Map<String, Object> operationContent;

    @Schema(description = "变更数据")
    private Map<String, Object> changeData;

    @Schema(description = "变更字段")
    private String changeFields;

    @Schema(description = "重试次数")
    private Integer retryCount;

    @Schema(description = "最后重试操作人 ID")
    private String retryOperatorId;

    @Schema(description = "最后重试操作人姓名")
    private String retryOperatorName;

    @Schema(description = "最后重试时间")
    private Date retryTime;

    @Schema(description = "备注")
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
        this.businessId = businessId == null ? null : businessId.trim();
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
        this.operatorId = operatorId == null ? null : operatorId.trim();
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName == null ? null : operatorName.trim();
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
        this.operationType = operationType == null ? null : operationType.trim();
    }

    public String getOperationStatus() {
        return operationStatus;
    }

    public void setOperationStatus(String operationStatus) {
        this.operationStatus = operationStatus == null ? null : operationStatus.trim();
    }

    public String getOperationDesc() {
        return operationDesc;
    }

    public void setOperationDesc(String operationDesc) {
        this.operationDesc = operationDesc == null ? null : operationDesc.trim();
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
