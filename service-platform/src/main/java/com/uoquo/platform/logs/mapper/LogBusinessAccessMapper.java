package com.uoquo.platform.logs.mapper;

import com.uoquo.platform.logs.model.pojo.LogBusinessAccess;

import java.util.List;
import java.util.Map;

public interface LogBusinessAccessMapper {

    /**
     * 新增
     * @mbg.generated generated automatically, do not modify!
     */
    int insert(LogBusinessAccess row);

    /**
     * 单条查询
     * @mbg.generated generated automatically, do not modify!
     */
    LogBusinessAccess selectByPrimaryKey(String id);

    /**
     * 列表查询
     * @mbg.generated generated automatically, do not modify!
     */
    List<LogBusinessAccess> listBySearch(Map<String, Object> map);
}