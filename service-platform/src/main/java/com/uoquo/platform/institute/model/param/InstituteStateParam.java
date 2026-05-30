package com.uoquo.platform.institute.model.param;

import io.swagger.v3.oas.annotations.media.Schema;


import jakarta.validation.constraints.NotBlank;

/**
 * 入参：修改企业状态信息
 */
@Schema(description = "修改企业状态信息")
public class InstituteStateParam {

    @Schema(description = "主键")
    @NotBlank
    private String id;

    @Schema(description = "状态")
    @NotBlank
    private String status;

    @Schema(description = "状态备注")
    private String statusMemo;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatusMemo() {
        return statusMemo;
    }

    public void setStatusMemo(String statusMemo) {
        this.statusMemo = statusMemo;
    }
}
