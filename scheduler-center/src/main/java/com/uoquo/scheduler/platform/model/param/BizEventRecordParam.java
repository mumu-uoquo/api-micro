package com.uoquo.scheduler.platform.model.param;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 入参：业务事件记录参数<br>
 * 备注：该入参为内部使用，因此ID采用对应的消息ID，方便追踪查找
 */
@Schema(description = "入参：业务事件记录参数")
public class BizEventRecordParam {

    @Schema(description = "事件ID（消息ID）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "id 不能为空")
    private String id;

    @Schema(description = "会话 ID")
    private String token;

    @Schema(description = "请求追踪 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "traceId 不能为空")
    private String traceId;

    @Schema(description = "业务类型（009）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "businessType 不能为空")
    private String businessType;

    @Schema(description = "业务子类型")
    private String businessSubType;

    @Schema(description = "业务数据表名")
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

    @Schema(description = "是否远程事件")
    private Boolean remoteEvent;

    @Schema(description = "事件类名")
    @NotBlank(message = "eventClass 不能为空")
    private String eventClass;

    @Schema(description = "事件内容")
    @NotBlank(message = "eventContent 不能为空")
    private String eventContent;

    @Schema(description = "备注")
    private String description;

    @Schema(description = "是否重试")
    private Boolean retryFlag;

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

    public Map<String, ?> getOperationContent() {
        return operationContent;
    }

    public void setOperationContent(Map<String, ?> operationContent) {
        if (operationContent == null) {
            this.operationContent = new HashMap<>();
        } else {
            this.operationContent = new HashMap<>(operationContent);
        }
    }

    public <E> void addOperationContent(String key, E val) {
        if (this.operationContent == null) {
            this.operationContent = new HashMap<>();
        }
        this.operationContent.put(key, val);
    }

    public Map<String, Object> getChangeData() {
        return changeData;
    }

    public void setChangeData(Map<String, ?> changeData) {
        if (changeData == null) {
            this.changeData = new HashMap<>();
        } else {
            this.changeData = new HashMap<>(changeData);
        }
    }

    public <E> void addChangeData(String key, E val) {
        if (this.changeData == null) {
            this.changeData = new HashMap<>();
        }
        this.changeData.put(key, val);
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getRetryFlag() {
        return retryFlag;
    }

    public void setRetryFlag(Boolean retryFlag) {
        this.retryFlag = retryFlag;
    }
}
