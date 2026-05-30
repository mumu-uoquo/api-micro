package com.uoquo.platform.institute.service;

import com.uoquo.platform.institute.model.dto.AreaInfoDto;
import com.uoquo.platform.institute.model.param.AreaInfoParam;

import java.util.List;


public interface AreaInfoService {

    /**
     * 新增分区
     */
    String addAreaInfo(AreaInfoParam param);

    /**
     * 更新分区
     */
    void updateAreaInfo(AreaInfoParam param);

    /**
     * 删除分区<br>
     * 同时删除与部门的对应关系
     */
    void deleteAreaInfo(String areaId);

    /**
     * 分区详情：根据主键ID
     */
    AreaInfoDto getAreaInfo(String areaId);

    /**
     * 分区列表：列表
     */
    List<AreaInfoDto> listAreaInfoByList(String instituteId);

}
