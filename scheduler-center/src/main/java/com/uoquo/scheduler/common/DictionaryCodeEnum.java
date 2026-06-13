package com.uoquo.scheduler.common;

/**
 * 字典编码（不是全部字典，仅代码逻辑中需要写死的）
 */
public enum DictionaryCodeEnum {
    UNKNOWN("000000", "未知"),

    /** ********** 001 可用状态（通用） ********** **/
    STATE_NORMAL("001001", "正常"),
    STATE_DISABLE("001002", "停用"),

    /** ********** 003 作用范围 ********** **/
    ROLE_TYPE_INNER("003001", "内置"),
    ROLE_TYPE_NORMAL("003002", "通用"),
    ROLE_TYPE_PRIVATE("003003", "私有"),

    /** ********** 005 模块类型 ********** **/
    MODULE_TYPE_MENU("005001", "菜单"),
    MODULE_TYPE_BUTTON("005002", "按钮"),

    /** ********** 006 模板类型 ********** **/
    TEMPLATE_TYPE_NONE("006001", "不是模板"),
    TEMPLATE_TYPE_INNER("006002", "内置模板"),
    TEMPLATE_TYPE_SYSTEM("006003", "系统模板"),
    TEMPLATE_TYPE_NORMAL("006010", "普通模板"),

    /** ********** 007 日期类型 ********** **/
    DAY_TYPE_WORKDAY("007001", "工作日"),
    DAY_TYPE_HOLIDAY("007002", "节假日"),
    DAY_TYPE_WEEKEND("007003", "周六日"),

    /** ********** 008 优先级别 ********** **/
    LEVEL_VERY_LOW("008010", "很低"),
    LEVEL_LOW("008020", "较低"),
    LEVEL_MIDDLE("008030", "一般"),
    LEVEL_IMPORTANT("008040", "重要"),
    LEVEL_HIGH("008050", "紧急"),
    LEVEL_VERY_HIGH("008060", "特急"),

    /** ********** 020 消息分类 ********** **/
    MESSAGE_TYPE_NOTICE("020001", "通知公告"),
    MESSAGE_TYPE_SYSTEM("020002", "业务消息"),
    MESSAGE_TYPE_TODO("020003", "待办任务"),

    /** ********** 021 推送方式 ********** **/
    PUSH_TYPE_ALL("021001", "不限"),
    PUSH_TYPE_EMAIL("021002", "邮件"),
    PUSH_TYPE_SMS("021003", "短信"),
    PUSH_TYPE_CALL("021004", "电话"),
    PUSH_TYPE_WX("021005", "微信"),
    PUSH_TYPE_APP("021006", "APP"),
    PUSH_TYPE_WEB("021007", "站内"),

    /** ********** 022 推送状态 ********** **/
    PUSH_STATUS_WAITING("022001", "未推"),
    PUSH_STATUS_PUSHED("022002", "已推"),
    PUSH_STATUS_FAILED("022003", "失败"),
    PUSH_STATUS_RETRY("022004", "重试"),
    PUSH_STATUS_SKIP("022005", "丢弃"),

    /** ********** 023 发布状态 ********** **/
    PUBLISH_STATUS_WAIT("023001", "待发布"),
    PUBLISH_STATUS_DONE("023002", "已发布"),
    PUBLISH_STATUS_UNDO("023003", "已撤回"),

    /** ********** 023 发布范围 ********** **/
    PUBLISH_RANGE_ALL("024001", "所有人员"),
    PUBLISH_RANGE_ROLE("024002", "指定角色"),
    PUBLISH_RANGE_USER("024003", "指定用户"),
    PUBLISH_RANGE_INSTITUTE("024004", "指定机构"),

    /** ********** 121 企业状态 ********** **/
    INSTITUTE_STATUS_NORMAL("121060", "正常"),
    INSTITUTE_STATUS_DISABLE("121070", "停用"),



    ;

    private final String code;
    private final String text;

    DictionaryCodeEnum(String code, String text) {
        this.code = code;
        this.text = text;
    }

    public String getCode() {
        return code;
    }

    public String getText() {
        return text;
    }

    public static DictionaryCodeEnum getByCode(String code) {
        for (DictionaryCodeEnum item : DictionaryCodeEnum.values()) {
            if (item.getCode().equals(code)) {
                return item;
            }
        }
        return UNKNOWN;
    }

    public static String getTextByCode(String code) {
        return getTextByCode(code, UNKNOWN);
    }

    public static String getTextByCode(String code, DictionaryCodeEnum defaultValue) {
        for (DictionaryCodeEnum item : DictionaryCodeEnum.values()) {
            if (item.getCode().equals(code)) {
                return item.getText();
            }
        }
        return defaultValue.getText();
    }

}
