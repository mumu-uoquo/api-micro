package com.uoquo.platform.system.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * 出参：系统字典信息（简版）
 */
@Schema(description = "出参：系统字典信息（简版）")
public class SysDictionarySimpleDto {

    @Schema(description = "字典编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String dicCode;

    @Schema(description = "字典内容", requiredMode = Schema.RequiredMode.REQUIRED)
    private String dicValue;

    @Schema(description = "标签样式")
    private String tagStyle;

    @Schema(description = "子节点")
    private List<SysDictionarySimpleDto> children;

    public String getDicCode() {
        return this.dicCode;
    }

    public void setDicCode(String dicCode) {
        this.dicCode = dicCode;
    }

    public String getDicValue() {
        return this.dicValue;
    }

    public void setDicValue(String dicValue) {
        this.dicValue = dicValue;
    }

    public String getTagStyle() {
        return tagStyle;
    }

    public void setTagStyle(String tagStyle) {
        this.tagStyle = tagStyle;
    }

    public List<SysDictionarySimpleDto> getChildren() {
        return children;
    }

    public void setChildren(List<SysDictionarySimpleDto> children) {
        this.children = children;
    }
}
