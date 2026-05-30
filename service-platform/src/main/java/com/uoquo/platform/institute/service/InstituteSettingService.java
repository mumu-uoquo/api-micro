package com.uoquo.platform.institute.service;

import com.uoquo.platform.system.model.dto.SettingDto;
import com.uoquo.platform.system.model.param.SettingSaveParam;

import java.util.List;

/**
 * 机构配置服务接口
 */
public interface InstituteSettingService {

    /**
     * 保存机构配置（存在则更新，不存在则创建）
     * @param list 配置保存参数
     * @return 配置详情
     */
    void saveSetting(String instituteId, List<SettingSaveParam> list);

    /**
     * 根据配置标识删除机构配置
     * @param instituteId 机构ID
     * @param configCode 配置标识
     */
    void deleteByCode(String instituteId, String configCode);

    /**
     * 根据配置标识查询配置（实现继承逻辑：机构->系统）
     * @param instituteId 机构ID
     * @param configCode 配置标识
     * @return 配置详情
     */
    String getValueByCode(String instituteId, String configCode);

    /**
     * 根据前缀查询机构配置列表（实现继承逻辑：机构->系统）
     * @param instituteId 机构ID
     * @param prefix 配置标识前缀
     * @return 配置列表
     */
    List<SettingDto> listByPrefix(String instituteId, String prefix);
}
