package com.uoquo.platform.common.exception;

import com.uoquo.web.BaseReturnCode;
import com.uoquo.web.ReturnLevel;

/**
 * 机构相关响应码（22XXX）
 * <ul>
 *     <li>220XX：机构相关</li>
 *     <li>221XX：科室相关</li>
 *     <li>222XX：区域相关</li>
 * </ul>
 */
public class InstituteReturnCode extends BaseReturnCode {
    InstituteReturnCode(String code, String text) {
        this(code, text, ReturnLevel.ERROR);
    }
    InstituteReturnCode(String code, String text, ReturnLevel level) {
        super(code, text, level);
    }

    /** =============================== 220XX 机构相关 =============================== **/
    public static BaseReturnCode INST_NAME_EXIST    = new InstituteReturnCode("22001", "机构名称已存在");
    public static BaseReturnCode INST_CODE_EXIST    = new InstituteReturnCode("22002", "机构编码已存在");
    public static BaseReturnCode INST_THIRDID_EXIST = new InstituteReturnCode("22003", "三方ID已存在");
    public static BaseReturnCode INST_DISABLE       = new InstituteReturnCode("22020", "机构被禁用");
    public static BaseReturnCode INST_DELETE        = new InstituteReturnCode("22021", "机构已删除");

    /** =============================== 221XX 科室相关 =============================== **/
    public static BaseReturnCode DEPT_NAME_EXIST    = new InstituteReturnCode("22101", "科室名称已存在");
    public static BaseReturnCode DEPT_CODE_EXIST    = new InstituteReturnCode("22102", "科室编码已存在");
    public static BaseReturnCode DEPT_THIRDID_EXIST = new InstituteReturnCode("22103", "三方ID已存在");

    /** =============================== 222XX 区域相关 =============================== **/
    public static BaseReturnCode AREA_NAME_EXIST    = new InstituteReturnCode("22201", "区域名称已存在");
    public static BaseReturnCode AREA_CODE_EXIST    = new InstituteReturnCode("22202", "区域编码已存在");
    public static BaseReturnCode AREA_THIRDID_EXIST = new InstituteReturnCode("22203", "三方ID已存在");

}
