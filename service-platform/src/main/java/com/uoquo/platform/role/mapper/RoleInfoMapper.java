package com.uoquo.platform.role.mapper;

import com.uoquo.platform.role.model.dto.RoleInfoDto;
import com.uoquo.platform.role.model.pojo.RoleInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface RoleInfoMapper {

    /**
     * 新增
     */
    int insert(RoleInfo row);

    /**
     * 删除（物理删除）
     */
    int deleteByPrimaryKey(String id);

    /**
     * 修改
     */
    int updateByPrimaryKey(RoleInfo row);

    /**
     * 查询：单条
     */
    RoleInfo selectByPrimaryKey(String id);

    /**
     * 列表查询：单机构
     */
    List<RoleInfo> selectByInstitute(Map<String, Object> map);

    /**
     * 列表查询
     */
    List<RoleInfoDto> selectBySearch(Map<String, Object> map);

    /**
     * 列表查询：根据用户ID查询
     */
    List<RoleInfo> listByUserId(String userId);

    /**
     * 角色名称校验
     */
    int checkRoleNameIsExist(@Param("id") String id, @Param("roleName") String roleName, @Param("instituteId") String instituteId);

}