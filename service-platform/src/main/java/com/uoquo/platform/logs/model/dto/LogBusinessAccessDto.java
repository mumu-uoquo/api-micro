package com.uoquo.platform.logs.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Date;

/**
 * 出参：业务数据访问记录
 */
@Schema(description = "出参：业务数据访问记录")
public class LogBusinessAccessDto {

    @Schema(description = "记录id", requiredMode = Schema.RequiredMode.REQUIRED)
    private String id;

    @Schema(description = "会话ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String token;

    @Schema(description = "请求追踪ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String traceId;

    @Schema(description = "业务类型（009）")
    private String businessType;

    @Schema(description = "业务子类")
    private String businessSubType;

    @Schema(description = "业务ID")
    private String businessId;

    @Schema(description = "数据表名")
    private String tableName;

    @Schema(description = "操作人ID")
    private String operatorId;

    @Schema(description = "操作人姓名")
    private String operatorName;

    @Schema(description = "访问类型（010）")
    private String accessType;

    @Schema(description = "访问时间")
    private Date accessTime;

    @Schema(description = "访问条件")
    private String accessCondition;

    @Schema(description = "访问IP")
    private String accessIp;

    @Schema(description = "备注")
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