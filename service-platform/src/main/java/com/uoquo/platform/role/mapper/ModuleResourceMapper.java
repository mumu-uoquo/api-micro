package com.uoquo.platform.role.mapper;

import com.uoquo.platform.role.model.pojo.ModuleResource;

import java.util.List;

public interface ModuleResourceMapper {

    /**
     * 新增：单条
     */
    int insert(ModuleResource row);

    /**
     * 新增：批量
     */
    int batchInsert(List<ModuleResource> list);

    /**
     * 删除：主键
     */
    int deleteByPrimaryKey(String id);

    /**
     * 删除：模块ID
     */
    int deleteByModuleId(String moduleId);

    /**
     * 删除：资源ID
     */
    int deleteByResourceId(String resourceId);

    /**
     * 查询：单条
     */
    ModuleResource selectByPrimaryKey(String id);

    /**
     * 列表：模块ID
     */
    List<ModuleResource> listByModuleId(String moduleId);

    /**
     * 列表：资源ID
     */
    List<ModuleResource> listByResourceId(String resourceId);
}