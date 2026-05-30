package com.uoquo.platform.logs.model.param;

import com.uoquo.web.param.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Date;

/**
 * 业务事件记录参数
 */
@Schema(description = "入参：业务事件记录查询")
public class BizEventRecordSearchParam extends PageRequest {

    @Schema(description = "业务类型（009）")
    private String businessType;

    @Schema(description = "业务子类")
    private String businessSubType;

    @Schema(description = "业务数据 ID")
    private String businessId;

    @Schema(description = "数据所属企业 ID")
    private String businessInstituteId;

    @Schema(description = "操作人 ID")
    private String operatorId;

    @Schema(description = "会话ID")
    private String token;

    @Schema(description = "操作人所属企业 ID")
    private String operatorInstituteId;

    @Schema(description = "操作类型（010）")
    private String operationType;

    @Schema(description = "操作时间起始")
    private Date operationTimeStart;

    @Schema(description = "操作时间终止")
    private Date operationTimeEnd;

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

    public String getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(String operatorId) {
        this.operatorId = operatorId == null ? null : operatorId.trim();
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token == null ? null : token.trim();
    }

    public String getOperatorInstituteId() {
        return operatorInstituteId;
    }

    public void setOperatorInstituteId(String operatorInstituteId) {
        this.operatorInstituteId = operatorInstituteId;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType == null ? null : operationType.trim();
    }
    public Date getOperationTimeStart() {
        return operationTimeStart;
    }

    public void setOperationTimeStart(Date operationTimeStart) {
        this.operationTimeStart = operationTimeStart;
    }

    public Date getOperationTimeEnd() {
        return operationTimeEnd;
    }

    public void setOperationTimeEnd(Date operationTimeEnd) {
        this.operationTimeEnd = operationTimeEnd;
    }
}
