package com.uoquo.platform.system.service;

import com.uoquo.platform.system.model.param.AppInhertAddParam;
import com.uoquo.platform.system.model.param.AppPermissionAddParam;
import com.uoquo.platform.system.model.pojo.AppInherit;
import com.uoquo.platform.system.model.pojo.AppPermission;

public interface AppPermissionService {

    /**
     * 复制权限
     */
    void copyAppPermission(String fromAppId, String toAppId);

    /**
     * 插入：资源授权<br>
     * 注：不刷新缓存，由调用方刷新（或手动刷新）
     */
    String addAppResourcePermission(String appId, String resourceId);

    /**
     * 插入：资源授权<br>
     * 注：不刷新缓存，由调用方刷新（或手动刷新）
     */
    int batchInsertPermission(AppPermissionAddParam param);

    /**
     * 删除：单条资源授权<br>
     * 注：不刷新缓存，由调用方刷新（或手动刷新）
     */
    AppPermission deletePermissionByPrimaryKey(String relateId);

    /**
     * 插入：资源继承（只能继承模板类的APP权限）<br>
     * 注：不刷新缓存，由调用方刷新（或手动刷新）
     */
    int batchInsertInherit(AppInhertAddParam param);

    /**
     * 删除：单条资源继承<br>
     * 注：不刷新缓存，由调用方刷新（或手动刷新）
     */
    AppInherit deleteInheritByPrimaryKey(String relateId);
}
