package com.uoquo.platform.common.exception;

import com.uoquo.web.BaseReturnCode;
import com.uoquo.web.ReturnLevel;

/**
 * 其他相关响应码（24XXX）
 * <ul>
 *     <li>240XX：文件存储相关</li>
 *     <li>245XX：Kafka 运维相关</li>
 * </ul>
 */
public class PlatformReturnCode extends BaseReturnCode {
    PlatformReturnCode(String code, String text) {
        this(code, text, ReturnLevel.ERROR);
    }
    PlatformReturnCode(String code, String text, ReturnLevel level) {
        super(code, text, level);
    }

    /** =============================== 240XX：文件存储相关 =============================== **/
    public static BaseReturnCode FILE_UPLOAD_INVALID   = new PlatformReturnCode("24001", "无效的上传码");
    public static BaseReturnCode FILE_UPLOAD_BROKEN    = new PlatformReturnCode("24002", "文件已损坏，请重新传输");
    public static BaseReturnCode FILE_SAVE_FAILED      = new PlatformReturnCode("24003", "文件保存失败");
    public static BaseReturnCode FILE_INVALID          = new PlatformReturnCode("24004", "文件不合法");
    public static BaseReturnCode FILE_EXISTS           = new PlatformReturnCode("24005", "文件已经存在");
    public static BaseReturnCode FILE_NOT_EXIST        = new PlatformReturnCode("24006", "文件不存在");
    public static BaseReturnCode FILE_DOWNLOAD_INVALID = new PlatformReturnCode("24015", "无效的下载码");
    public static BaseReturnCode FILE_TOO_BIG          = new PlatformReturnCode("24016", "文件过大，不可以用Base64传输");

    /** =============================== 245XX：运维相关 =============================== **/
    public static BaseReturnCode KAFKA_SEND_FAILED    = new PlatformReturnCode("24501", "Kafka 发送失败");

}
