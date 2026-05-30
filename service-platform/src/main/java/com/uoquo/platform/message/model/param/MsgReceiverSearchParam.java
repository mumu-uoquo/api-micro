package com.uoquo.platform.message.model.param;

import com.uoquo.web.param.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;

/**
 * 入参：发布目标搜索
 */
@Schema(description = "发布目标搜索")
public class MsgReceiverSearchParam extends PageRequest {

    @Schema(description = "发布范围（024）")
    @NotNull
    private String receiverRange;

    @Schema(description = "关键字")
    private String keywords;

    @Schema(description = "所属机构")
    private String instituteId;

    public String getReceiverRange() {
        return receiverRange;
    }

    public void setReceiverRange(String receiverRange) {
        this.receiverRange = receiverRange;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public String getInstituteId() {
        return instituteId;
    }

    public void setInstituteId(String instituteId) {
        this.instituteId = instituteId;
    }

}
