package com.uoquo.platform.logs.mapper;

import com.uoquo.platform.logs.model.pojo.LogBusinessChange;

import java.util.List;
import java.util.Map;

public interface LogBusinessChangeMapper {

    /**
     * 新增
     * @mbg.generated generated automatically, do not modify!
     */
    int insert(LogBusinessChange row);

    /**
     * 单条查询
     * @mbg.generated generated automatically, do not modify!
     */
    LogBusinessChange selectByPrimaryKey(String id);

    /**
     * 列表查询
     * @mbg.generated generated automatically, do not modify!
     */
    List<LogBusinessChange> listBySearch(Map<String, Object> map);
}