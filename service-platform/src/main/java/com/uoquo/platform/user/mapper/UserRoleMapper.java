package com.uoquo.platform.user.mapper;

import com.uoquo.platform.user.model.pojo.UserRole;

import java.util.List;

public interface UserRoleMapper {

    /**
     * 单条新增
     */
    int insert(UserRole row);

    /**
     * 批量新增
     */
    int batchInsert(List<UserRole> list);

    /**
     * 根据用户删除
     */
    int deleteByUserId(String userId);

    /**
     * 根据用户查询
     */
    List<UserRole> selectByUserId(String userId);

    /**
     * 根据角色删除
     */
    int deleteByRoleId(String roleId);

    /**
     * 根据角色查询
     */
    List<UserRole> selectByRoleId(String roleId);
}