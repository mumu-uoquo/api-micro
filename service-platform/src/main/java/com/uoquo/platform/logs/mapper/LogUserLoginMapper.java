package com.uoquo.platform.logs.mapper;

import com.uoquo.platform.logs.model.pojo.LogUserLogin;

import java.util.List;
import java.util.Map;

public interface LogUserLoginMapper {

    /**
     * 新增（不填充logout相关字段）
     * @mbg.generated generated automatically, do not modify!
     */
    int insert(LogUserLogin row);

    /**
     * 更新登出信息（根据token更新）
     * @mbg.generated generated automatically, do not modify!
     */
    int updateLogout4Token(LogUserLogin row);

    /**
     * 更新登出信息（根据userId更新）
     * @mbg.generated generated automatically, do not modify!
     */
    int updateLogout4User(LogUserLogin row);

    /**
     * 单条查询
     * @mbg.generated generated automatically, do not modify!
     */
    LogUserLogin selectByPrimaryKey(String id);

    /**
     * 列表查询
     * @mbg.generated generated automatically, do not modify!
     */
    List<LogUserLogin> listBySearch(Map<String, Object> map);
}