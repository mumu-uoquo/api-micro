package com.uoquo.platform.system.service;

import com.uoquo.platform.system.model.dto.AppPushDto;
import com.uoquo.platform.system.model.param.AppPushParam;
import com.uoquo.platform.system.model.param.AppPushStateParam;

import java.util.List;

public interface AppPushService {

    /**
     * 新增
     */
    AppPushDto addAppPush(AppPushParam param);

    /**
     * 修改
     */
    int updateAppPush(AppPushParam param);

    /**
     * 修改状态
     */
    void updateState(AppPushStateParam param);

    /**
     * 删除（逻辑删）
     */
    int deleteByPrimaryKey(String id);

    /**
     * 查询：根据ID
     */
    AppPushDto selectByPrimaryKey(String id);

    /**
     * 列表：根据 appId 查询（不分页）
     */
    List<AppPushDto> listByAppId(String appId);
}
