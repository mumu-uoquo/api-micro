package com.uoquo.platform.user.service;

import com.uoquo.platform.system.model.dto.SettingDto;
import com.uoquo.platform.system.model.param.SettingSaveParam;

import java.util.List;

/**
 * 用户配置服务接口
 */
public interface UserSettingService {

    /**
     * 保存用户配置（存在则更新，不存在则创建）
     * @param list 配置保存参数
     * @return 配置详情
     */
    void saveSetting(String userId, List<SettingSaveParam> list);

    /**
     * 根据配置标识删除用户配置
     * @param configCode 配置标识
     */
    void deleteByCode(String userId, String configCode);

    /**
     * 根据配置标识查询配置（实现继承逻辑：用户->机构->系统）
     * @param configCode 配置标识
     * @return 配置详情
     */
    String getValueByCode(String userId, String configCode);

    /**
     * 根据前缀查询用户配置列表（实现继承逻辑：用户->机构->系统）
     * @param prefix 配置标识前缀
     * @return 配置列表
     */
    List<SettingDto> listByPrefix(String userId, String prefix);
}
