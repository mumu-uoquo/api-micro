package com.uoquo.platform.system.service;

import com.uoquo.platform.system.model.param.LicenseImportParam;

/**
 * License 相关服务
 */
public interface LicenseService {

    /**
     * 获取机器码
     *
     * @return 机器码
     */
    String getMachineCode();

    /**
     * 导入 license（保存至本地）
     *
     * @param param license 导入参数（目前仅包含 license 字符串，后续可扩展其他内容）
     */
    void importLicense(LicenseImportParam param);
}
