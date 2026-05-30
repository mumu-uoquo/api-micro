package com.uoquo.platform.system.model.param;

import com.uoquo.web.param.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 入参：系统响应码查询
 */
@Schema(description = "系统响应码查询参数")
public class SysReturnCodeSearchParam extends PageRequest {

    @Schema(description = "响应码（模糊匹配）")
    private String returnCode;

    public String getReturnCode() {
        return returnCode;
    }

    public void setReturnCode(String returnCode) {
        this.returnCode = returnCode;
    }

}
