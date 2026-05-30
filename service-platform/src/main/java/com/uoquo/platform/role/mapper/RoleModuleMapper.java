package com.uoquo.platform.role.mapper;

import com.uoquo.platform.role.model.pojo.RoleModule;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface RoleModuleMapper {

    /**
     * 新增：单条
     */
    int insert(RoleModule row);

    /**
     * 新增：批量
     */
    int batchInsert(List<RoleModule> list);

    /**
     * 删除：主键
     */
    int deleteByPrimaryKey(String id);

    /**
     * 删除：根据角色ID和模块ID
     */
    int deleteByRoleIdAndModuleId(@Param("roleId") String roleId, @Param("moduleId") String moduleId);


    /**
     * 删除：批量删除指定角色下的关联模块信息
     */
    void batchDeleteByModuleId(@Param("roleId") String roleId, @Param("list") List<String> moduleIdList);

    /**
     * 删除：模块ID
     */
    int deleteByModuleId(String moduleId);

    /**
     * 删除：角色ID
     */
    int deleteByRoleId(String roleId);

    /**
     * 列表：模块ID
     */
    List<RoleModule> listByModuleId(String moduleId);

    /**
     * 列表：角色ID
     */
    List<RoleModule> listByRoleId(String roleId);

    /**
     * 根据模块ID查找授权的角色
     */
    List<String> listRoleIdByModuleId(@Param("moduleId") String moduleId, @Param("roleType") String roleType);
}