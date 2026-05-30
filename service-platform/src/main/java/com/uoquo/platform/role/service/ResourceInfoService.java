package com.uoquo.platform.role.service;


import com.uoquo.platform.role.model.dto.ResourceInfoDto;
import com.uoquo.platform.role.model.param.ResourceInfoParam;

import java.util.List;

public interface ResourceInfoService {

    /* *******************  资源管理 ******************* */
    /**
     * 单条插入
     */
    String addResource(ResourceInfoParam param);

    /**
     * 单条修改
     */
    int updateResource(ResourceInfoParam param);

    /**
     * 单条删除：根据ID
     */
    int deleteResource(String id);

    /**
     * 查询所有资源
     */
    List<ResourceInfoDto> listAllResource();

    /**
     * 列表：根据角色ID
     */
    List<ResourceInfoDto> listByRoleId(String roleId);

    /* *******************  资源与AppId的关联管理 ******************* */
    /**
     * 列表：AppId关联的资源
     */
    List<ResourceInfoDto> listByAppId(String appId);

    /**
     * 列表：AppId未关联的资源
     */
    List<ResourceInfoDto> listNotInApp(String appId);

    /* *******************  资源与模块的关联管理 ******************* */
    /**
     * 列表：模块ID关联的资源
     */
    List<ResourceInfoDto> listByModuleId(String moduleId);

    /**
     * 列表：模块ID未关联的资源
     */
    List<ResourceInfoDto> listNotInModule(String moduleId);
}
