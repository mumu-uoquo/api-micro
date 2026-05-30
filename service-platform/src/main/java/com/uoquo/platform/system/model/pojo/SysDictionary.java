package com.uoquo.platform.system.model.pojo;

/**
 * Table: sys_dictionary
 */
public class SysDictionary {
    /**
     * Column: id
     * Type: VARCHAR(32)
     * Remark: ID
     */
    private String id;

    /**
     * Column: dic_type
     * Type: CHAR(6)
     * Remark: 作用范围（003）
     */
    private String dicType;

    /**
     * Column: dic_code
     * Type: CHAR(6)
     * Remark: 字典编码
     */
    private String dicCode;

    /**
     * Column: dic_value
     * Type: VARCHAR(100)
     * Remark: 字典内容
     */
    private String dicValue;

    /**
     * Column: tag_style
     * Type: VARCHAR(20)
     * Remark: 标签样式
     */
    private String tagStyle;

    /**
     * Column: sort_idx
     * Type: INT
     * Default value: 99
     * Remark: 显示顺序
     */
    private Integer sortIdx;

    /**
     * Column: delete_state
     * Type: BIGINT
     * Default value: 0
     * Remark: 删除标识
     */
    private Long deleteState;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    public String getDicType() {
        return dicType;
    }

    public void setDicType(String dicType) {
        this.dicType = dicType;
    }

    public String getDicCode() {
        return dicCode;
    }

    public void setDicCode(String dicCode) {
        this.dicCode = dicCode == null ? null : dicCode.trim();
    }

    public String getDicValue() {
        return dicValue;
    }

    public void setDicValue(String dicValue) {
        this.dicValue = dicValue == null ? null : dicValue.trim();
    }

    public String getTagStyle() {
        return tagStyle;
    }

    public void setTagStyle(String tagStyle) {
        this.tagStyle = tagStyle;
    }

    public Integer getSortIdx() {
        return sortIdx;
    }

    public void setSortIdx(Integer sortIdx) {
        this.sortIdx = sortIdx;
    }

    public Long getDeleteState() {
        return deleteState;
    }

    public void setDeleteState(Long deleteState) {
        this.deleteState = deleteState;
    }
}