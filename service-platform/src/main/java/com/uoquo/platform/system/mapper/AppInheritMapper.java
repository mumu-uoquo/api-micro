package com.uoquo.platform.system.mapper;

import com.uoquo.platform.system.model.pojo.AppInherit;

import java.util.List;

public interface AppInheritMapper {

    /**
     * 新增：单条
     */
    int insert(AppInherit row);

    /**
     * 新增：批量
     */
    int batchInsert(List<AppInherit> rows);

    /**
     * 删除：按主键
     */
    int deleteByPrimaryKey(String id);

    /**
     * 根据APPID删除
     */
    int deleteByAppId(String appid);

    /**
     * 根据ParentId删除
     */
    int deleteByParentId(String parentId);

    /**
     * 查询：单条
     */
    AppInherit selectByPrimaryKey(String id);

    /**
     * 查询Appid的继承关系
     */
    List<AppInherit> selectByAppId(String appid);

    /**
     * 查询ParentId的被继承关系
     */
    List<AppInherit> selectByParentId(String parentId);
}