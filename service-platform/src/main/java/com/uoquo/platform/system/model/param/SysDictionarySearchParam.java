package com.uoquo.platform.system.model.param;

import io.swagger.v3.oas.annotations.media.Schema;


import jakarta.validation.constraints.NotBlank;

/**
 * 入参：系统字典查询
 */
@Schema(description = "系统字典请求参数")
public class SysDictionarySearchParam {

    @Schema(description = "系统字典code")
    @NotBlank(message = "itemCode不能为空")
    private String itemCode;

    public String getItemCode() {
        return this.itemCode;
    }

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }
}
