package com.uoquo.platform.system.mapper;

import java.util.List;

import com.uoquo.platform.system.model.pojo.LicenseRecord;

public interface LicenseRecordMapper {

    /**
     * 新增导入记录
     */
    int insert(LicenseRecord row);

    /**
     * 将所有记录的 is_current 置为 false（导入新 License 前调用）
     */
    int clearAllCurrent();

    /**
     * 分页查询：所有记录，按 create_time 倒序
     */
    List<LicenseRecord> selectByPage();
}
