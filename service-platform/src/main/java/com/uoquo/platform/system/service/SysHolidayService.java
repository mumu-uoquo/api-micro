package com.uoquo.platform.system.service;

import com.uoquo.platform.system.model.dto.SysHolidayDto;
import com.uoquo.platform.system.model.param.SysHolidayInfoParam;
import com.uoquo.platform.system.model.param.SysHolidaySearchParam;

import java.util.List;

public interface SysHolidayService {

    /**
     * 单条新增
     * @param param 单条日期
     */
    String saveHolidayInfo(SysHolidayInfoParam param);

    /**
     * 批量新增
     * @param params 批量日期
     */
    void batchSaveHolidayInfo(List<SysHolidayInfoParam> params);

    /**
     * 单条更新
     * @param param 单条日期
     */
    void updateHolidayInfo(SysHolidayInfoParam param);

    /**
     * 删除节假日
     */
    void deleteHolidayInfo(String id);

    /**
     * 列表查询
     * @param param 指定时间范围（默认为当年）
     * @return 指定范围内的节假日列表信息
     */
    List<SysHolidayDto> listHolidayInfo(SysHolidaySearchParam param);
}
