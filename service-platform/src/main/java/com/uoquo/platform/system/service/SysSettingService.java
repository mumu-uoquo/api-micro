package com.uoquo.platform.system.service;

import com.uoquo.platform.system.model.dto.SettingDto;
import com.uoquo.platform.system.model.param.SettingSaveParam;

import java.util.List;

/**
 * 系统配置<br/>
 * 作用范围说明
 * <ul>
 *   <li>内置：登录后查询，可返给调用方</li>
 *   <li>通用：可直接查询，常用于公钥等配置的查询（无需登录）</li>
 *   <li>私有：仅内部查询，可存于redis，不返回给调用方</li>
 * </ul>
 */
public interface SysSettingService {

    /**
     * 保存系统配置（存在则更新，不存在则创建）
     */
    void saveSetting(List<SettingSaveParam> list);

    /**
     * 根据配置标识删除系统配置
     * @param code 配置标识
     */
    void deleteByCode(String code);

    /**
     * 获取指定编码的置信息
     * @param code 配置标识
     * @return 配置详情
     */
    String getValueByCode(String code);

    /**
     * 获取指定编码开头的配置信息<br>
     * 注：只查内置和通用的，不查私有（私有配置不对外公开）
     * @param prefix 配置标识前缀
     * @return 配置列表
     */
    List<SettingDto> listByPrefix(String prefix);

    /**
     * 查询通用配置（无需登录查询的配置）
     */
    List<SettingDto> listPublicSettings();

    /**
     * 查询私有配置（该方法仅用于项目内部，不对外公开）
     */
    List<SettingDto> listPrivateSettings();
}
