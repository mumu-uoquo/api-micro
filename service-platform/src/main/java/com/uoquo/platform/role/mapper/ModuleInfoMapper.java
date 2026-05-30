package com.uoquo.platform.role.mapper;

import com.uoquo.platform.role.model.pojo.ModuleInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface ModuleInfoMapper {

    /**
     * 新增
     */
    int insert(ModuleInfo row);

    /**
     * 删除（物理删除）
     */
    int deleteByPrimaryKey(String id);

    /**
     * 修改
     */
    int updateByPrimaryKey(ModuleInfo row);

    /**
     * 查询：单条
     */
    ModuleInfo selectByPrimaryKey(String id);

    /**
     * 列表：条件查询
     */
    List<ModuleInfo> listBySearch(Map<String, Object> map);

    /**
     * 列表：子模块查询
     */
    List<ModuleInfo> listByParentId(String parentId);

    /**
     * 列表：根据角色ID查询
     */
    List<ModuleInfo> listByRoleId(String roleId);

    /**
     * 校验：编码是否存在
     */
    int checkCodeIsExist(String moduleCode);

    /**
     * 校验：名称是否存在
     */
    int checkNameIsExist(@Param("id") String id, @Param("parentId") String parentId, @Param("moduleName") String moduleName);
}