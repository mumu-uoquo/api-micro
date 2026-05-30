package com.uoquo.platform.user.mapper;

import com.uoquo.platform.user.model.pojo.UserGroup;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserGroupMapper {

    /**
     * 单条新增
     */
    int insert(UserGroup row);

    /**
     * 批量新增
     */
    int batchInsert(@Param("list") List<UserGroup> list);

    /**
     * 根据用户删除
     */
    int deleteByUserId(String userId);

    /**
     * 根据用户查询
     */
    List<UserGroup> selectByUserId(String userId);

    /**
     * 根据用户删除
     */
    int deleteByGroupId(String groupId);

    /**
     * 根据用户查询
     */
    List<UserGroup> selectByGroupId(String groupId);
}