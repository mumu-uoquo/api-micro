package com.uoquo.scheduler.common;

/**
 * 字典编码：业务操作类型（010xxx）
 * @author xuhz
 */
public enum BusinessOperationEnum {
    UNKNOWN("000000", "未知"),

    // ==================== 认证操作 (01000x) ====================
    /** 登录认证 */
    LOGIN("010001", "登录"),
    /** Token刷新 */
    REFRESH_TOKEN("010003", "刷新令牌"),
    /** 登出 */
    LOGOUT("010005", "登出"),
    /** 注册 */
    REGISTER("010007", "注册"),

    // ==================== 常规操作 (01001x) ====================
    /** 新增数据 */
    CREATE("010010", "新增"),
    /** 修改信息 */
    UPDATE("010011", "修改"),
    /** 删除数据 */
    DELETE("010012", "删除"),
    /** 查看详情 */
    VIEW("010013", "查看"),
    /** 查询列表 */
    QUERY("010014", "查询"),
    /** 启用 */
    ENABLE("010015", "启用"),
    /** 禁用 */
    DISABLE("010016", "禁用"),
    /** 导入 */
    IMPORT("010017", "导入"),
    /** 导出 */
    EXPORT("010018", "导出"),
    /** 打印 */
    PRINT("010019", "打印"),

    // ==================== 设置操作 (01002x) ====================
    /** 设为置顶 */
    SET_TOP("010020", "设为置顶"),
    /** 取消置顶 */
    UNSET_TOP("010021", "取消置顶"),
    /** 设为默认 */
    SET_DEFAULT("010022", "设为默认"),
    /** 取消默认 */
    UNSET_DEFAULT("010023", "取消默认"),
    /** 设为模板 */
    SET_TEMPLATE("010024", "设为模板"),
    /** 取消模板 */
    UNSET_TEMPLATE("010025", "取消模板"),
    /** 状态变更 */
    CHANGE_STATUS("010026", "状态变更"),
    /** 关联变更 */
    CHANGE_RELATION("010027", "关联变更"),
    /** 增加关联 */
    ADD_RELATION("010028", "增加关联"),
    /** 删除关联 */
    DEL_RELATION("010029", "删除关联"),

    // ==================== 文件操作 (01003x) ====================
    /** 下载 */
    DOWNLOAD("010030", "下载"),
    /** 上传 */
    UPLOAD("010031", "上传"),
    /** 改名 */
    RENAME("010032", "改名"),
    /** 移动 */
    MOVE("010033", "移动"),
    /** 分享 */
    SHARE("010034", "分享"),
    /** 清理 */
    CLEAR("010035", "清理"),
    /** 备份 */
    BACKUP("010036", "备份"),
    /** 还原 */
    RESTORE("010037", "还原"),

    // ==================== 流程操作 (01004x) ====================
    /** 提交审批 */
    PROCESS_SUBMIT("010040", "发起"),
    /** 审批通过 */
    PROCESS_APPROVE("010041", "通过"),
    /** 审批拒绝 */
    PROCESS_REJECT("010042", "拒绝"),
    /** 加签审批 */
    PROCESS_REVIEWER("010043", "加签"),
    /** 转办审批 */
    PROCESS_TRANSFER("010044", "转办"),
    /** 抄送审批 */
    PROCESS_COPY("010045", "抄送"),
    /** 添加备注 */
    PROCESS_REMARK("010046", "备注"),
    /** 审批撤回 */
    PROCESS_WITHDRAW("010048", "撤回"),
    /** 审批归档 */
    PROCESS_ARCHIVE("010049", "归档"),

    // ==================== 账户操作 (01005x) ====================
    /** 修改密码 */
    UPDATE_PASSWORD("010050", "修改密码"),
    /** 找回密码 */
    RETRIEVE_PASSWORD("010051", "找回密码"),
    /** 修改手机 */
    UPDATE_PHONE("010052", "修改手机"),
    /** 修改邮箱 */
    UPDATE_EMAIL("010053", "修改邮箱"),
    /** 修改头像 */
    UPDATE_AVATAR("010054", "修改头像"),

    // ==================== 消息通知模块 (01006x) ====================
    /** 发布消息 */
    MESSAGE_PUBLISH("010060", "发布消息"),
    /** 撤回消息 */
    MESSAGE_WITHDRAW("010061", "撤回消息"),
    /** 发送消息 */
    MESSAGE_SEND("010062", "发送消息"),

    ;

    private final String code;
    private final String text;

    BusinessOperationEnum(String code, String text) {
        this.code = code;
        this.text = text;
    }

    public String getCode() {
        return code;
    }

    public String getText() {
        return text;
    }

    public static BusinessOperationEnum getByCode(String code) {
        for (BusinessOperationEnum item : BusinessOperationEnum.values()) {
            if (item.getCode().equals(code)) {
                return item;
            }
        }
        return UNKNOWN;
    }

    public static String getTextByCode(String code) {
        return getTextByCode(code, UNKNOWN);
    }

    public static String getTextByCode(String code, BusinessOperationEnum defaultValue) {
        for (BusinessOperationEnum item : BusinessOperationEnum.values()) {
            if (item.getCode().equals(code)) {
                return item.getText();
            }
        }
        return defaultValue.getText();
    }
}
