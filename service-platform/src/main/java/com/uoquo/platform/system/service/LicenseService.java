package com.uoquo.platform.system.service;

import com.uoquo.mybatis.page.PageResult;
import com.uoquo.platform.system.model.dto.LicenseRecordDto;
import com.uoquo.platform.system.model.param.LicenseImportParam;
import com.uoquo.platform.system.model.param.LicenseRecordPageParam;

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
     * 导入 license（保存至本地，并插入导入记录）
     *
     * @param param license 导入参数
     */
    void importLicense(LicenseImportParam param);

    /**
     * 分页查询 License 导入记录（倒序，无过滤条件）
     *
     * @param param 分页参数
     * @return 分页结果
     */
    PageResult<LicenseRecordDto> listRecordByPage(LicenseRecordPageParam param);
}
