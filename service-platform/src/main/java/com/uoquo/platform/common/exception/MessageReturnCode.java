package com.uoquo.platform.common.exception;

import com.uoquo.web.BaseReturnCode;
import com.uoquo.web.ReturnLevel;

/**
 * 消息相关响应码（23XXX）
 * <ul>
 *     <li>230XX：消息模板相关</li>
 *     <li>231XX：消息管理相关</li>
 * </ul>
 */
public class MessageReturnCode extends BaseReturnCode {
    MessageReturnCode(String code, String text) {
        this(code, text, ReturnLevel.ERROR);
    }
    MessageReturnCode(String code, String text, ReturnLevel level) {
        super(code, text, level);
    }

    /** =============================== 230XX 消息模板相关 =============================== **/
    public static BaseReturnCode TEMPLATE_CODE_EXIST = new MessageReturnCode("23001", "模板编号已存在");

    /** =============================== 231XX 消息管理相关 =============================== **/
    public static BaseReturnCode RECEIVER_EXIST = new MessageReturnCode("23101", "消息接收人已存在");
    public static BaseReturnCode DOWNLOAD_ATTACHMENT_ERROR = new MessageReturnCode("23101", "附件下载出错");
    public static BaseReturnCode PUBLISH_ERROR = new MessageReturnCode("23101", "消息不是待发布状态，不可发布");

}
