package com.uoquo.platform.role.service;

import com.uoquo.platform.role.model.dto.ModuleInfoDto;
import com.uoquo.platform.role.model.dto.ModuleTreeDto;
import com.uoquo.platform.role.model.param.ModuleInfoParam;
import com.uoquo.platform.role.model.param.ModuleResourceParam;
import com.uoquo.platform.role.model.pojo.ModuleResource;

import java.util.List;

public interface ModuleInfoService {

    /**
     * 新增：单条模块信息
     */
    void addModule(ModuleInfoParam param);

    /**
     * 更新：单条模块信息
     */
    void updateModuleInfo(ModuleInfoParam param);

    /**
     * 删除：单条模块信息
     */
    void deleteModule(String id);

    /**
     * 单条：根据主键查
     */
    ModuleInfoDto selectByPrimaryKey(String id);

    /**
     * 列表：获取一级模块信息<br>
     * 适用：APP授权
     */
    List<ModuleInfoDto> listModuleByRoot();

    /**
     * 列表：获取指定角色的模块信息<br>
     */
    List<ModuleInfoDto> listModuleByRoleId(String roleId);

    /**
     * 树状：获取所有模块信息<br>
     * 适用：模块管理、超管角色授权
     */
    List<ModuleTreeDto> listModuleTreeByAll();

    /**
     * 树状：获取指定角色的模块信息<br>
     * 适用：普通角色授权（只能授权自己有的模块给他人）
     */
    List<ModuleTreeDto> listModuleTreeByRoleId(String roleId, String parentId);

    /**
     * 插入：资源授权（单条）
     */
    String addModuleResource(String moduleId, String resourceId);

    /**
     * 插入：资源授权（批量）
     */
    int batchInsertRelationResource(ModuleResourceParam param);

    /**
     * 删除：单条资源授权
     */
    ModuleResource deleteRelationResourceByPrimaryKey(String id);
}
