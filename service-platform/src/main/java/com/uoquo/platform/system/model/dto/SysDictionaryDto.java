package com.uoquo.platform.system.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * 出参：系统字典信息
 */
@Schema(description = "出参：系统字典信息")
public class SysDictionaryDto {

    @Schema(description = "字典ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String id;

    @Schema(description = "作用范围（003）")
    private String dicType;

    @Schema(description = "字典编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String dicCode;

    @Schema(description = "字典内容", requiredMode = Schema.RequiredMode.REQUIRED)
    private String dicValue;

    @Schema(description = "标签样式")
    private String tagStyle;

    @Schema(description = "显示顺序")
    private Integer sortIdx;

    @Schema(description = "子节点")
    private List<SysDictionaryDto> children;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDicType() {
        return dicType;
    }

    public void setDicType(String dicType) {
        this.dicType = dicType;
    }

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

    public Integer getSortIdx() {
        return this.sortIdx;
    }

    public void setSortIdx(Integer sortIdx) {
        this.sortIdx = sortIdx;
    }

    public List<SysDictionaryDto> getChildren() {
        return children;
    }

    public void setChildren(List<SysDictionaryDto> children) {
        this.children = children;
    }
}
