package com.uoquo.platform.logs.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Date;

/**
 * 业务事件重试记录 DTO
 */
@Schema(description = "业务事件重试记录 DTO")
public class BizEventRetryDto {

    @Schema(description = "日志 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String id;

    @Schema(description = "记录 ID")
    private String recordId;

    @Schema(description = "业务数据 ID")
    private String businessId;

    @Schema(description = "数据所属企业 ID")
    private String businessInstituteId;

    @Schema(description = "会话 ID")
    private String token;

    @Schema(description = "请求追踪 ID")
    private String traceId;

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

    @Schema(description = "备注")
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
        this.businessId = businessId == null ? null : businessId.trim();
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
        this.token = token == null ? null : token.trim();
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId == null ? null : traceId.trim();
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
