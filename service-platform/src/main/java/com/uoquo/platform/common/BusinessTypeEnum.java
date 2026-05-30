package com.uoquo.platform.common;

/**
 * 字典编码：业务类型（009xxx）
 * @author xuhz
 */
public enum BusinessTypeEnum {
    UNKNOWN("000000", "未知"),

    AUTH("009005", "认证"),
    SYSTEM("009009", "系统"),

    ACCOUNT("009010", "账户"),
    INSTITUTE("009020", "机构"),
    MESSAGE("009030", "消息"),

    // 内置信息，不维护字典表
    SSE("009901", "SSE信息"),

    ;

    private final String code;
    private final String text;

    BusinessTypeEnum(String code, String text) {
        this.code = code;
        this.text = text;
    }

    public String getCode() {
        return code;
    }

    public String getText() {
        return text;
    }

    public static BusinessTypeEnum getByCode(String code) {
        for (BusinessTypeEnum item : BusinessTypeEnum.values()) {
            if (item.getCode().equals(code)) {
                return item;
            }
        }
        return UNKNOWN;
    }

    public static String getTextByCode(String code) {
        return getTextByCode(code, UNKNOWN);
    }

    public static String getTextByCode(String code, BusinessTypeEnum defaultValue) {
        for (BusinessTypeEnum item : BusinessTypeEnum.values()) {
            if (item.getCode().equals(code)) {
                return item.getText();
            }
        }
        return defaultValue.getText();
    }
}
