package com.uoquo.platform.system.mapper;

import com.uoquo.platform.system.model.pojo.AppPermission;

import java.util.List;

public interface AppPermissionMapper {

    /**
     * 新增：单条
     */
    int insert(AppPermission row);

    /**
     * 新增：批量
     */
    int batchInsert(List<AppPermission> list);

    /**
     * 删除：按主键
     */
    int deleteByPrimaryKey(String id);

    /**
     * 删除：按AppId批量
     */
    int deleteByAppId(String appId);

    /**
     * 删除：按ResourceId批量
     */
    int deleteByResourceId(String resourceId);

    /**
     * 查询：单条
     */
    AppPermission selectByPrimaryKey(String id);

    /**
     * 按AppId查询
     */
    List<AppPermission> selectByAppId(String appId);
}