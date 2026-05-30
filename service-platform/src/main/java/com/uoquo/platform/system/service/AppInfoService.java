package com.uoquo.platform.system.service;

import com.uoquo.platform.system.model.dto.AppInfoDto;
import com.uoquo.platform.system.model.param.AppInfoListParam;
import com.uoquo.platform.system.model.param.AppInfoParam;
import com.uoquo.platform.system.model.param.AppInfoStateParam;
import com.uoquo.mybatis.page.PageResult;

import java.util.List;

public interface AppInfoService{

    /**
     * 插入：单条
     * 注：需要返回插入后的对象，前端需要插入后的ID
     */
    AppInfoDto addAppInfo(AppInfoParam param);

    /**
     * 更新：根据ID
     */
    int updateAppInfo(AppInfoParam param);

    /**
     * 更新：状态
     */
    void updateState(AppInfoStateParam param);

    /**
     * 更新：模板类型
     */
    void updateTemplateType(String appId, String templateType);

    /**
     * 删除：根据ID
     */
    int deleteByPrimaryKey(String id);

    /**
     * 查询：根据ID
     */
    AppInfoDto selectByPrimaryKey(String id);

    /**
     * 列表：分页
     */
    PageResult<AppInfoDto> listByPage(AppInfoListParam param);

    /**
     * 列表：模板列表
     * @param appId 应用ID
     * @return 若有appId则返回未关联的模板，若无appId则返回全部模板
     */
    List<AppInfoDto> listByTemplate(String appId);

    /**
     * 列表：继承的父级应用列表
     */
    List<AppInfoDto> listInheritByApp(String appId);

    /**
     * 列表：继承的子级应用列表
     */
    List<AppInfoDto> listInheritBySub(String appId);

    /**
     * 缓存应用信息
     */
    void flushAppInfoCache();

    /**
     * 刷新APP权限缓存（全部）
     */
    void flushAppPermissionCache();

    /**
     * 刷新APP权限缓存（指定APP）
     */
    void flushAppPermissionCache(String appId);
}
