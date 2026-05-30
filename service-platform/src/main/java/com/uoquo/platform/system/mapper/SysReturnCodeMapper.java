package com.uoquo.platform.system.mapper;

import com.uoquo.platform.system.model.pojo.SysReturnCode;

import java.util.List;
import java.util.Map;

public interface SysReturnCodeMapper {

    /**
     * 新增
     */
    int insert(SysReturnCode row);

    /**
     * 删除：按主键
     */
    int deleteByPrimaryKey(String id);

    /**
     * 修改
     */
    int updateByPrimaryKey(SysReturnCode row);

    /**
     * 查询：单条（按ID）
     */
    SysReturnCode selectByPrimaryKey(String id);

    /**
     * 查询：单条（按响应码）
     */
    SysReturnCode selectByReturnCode(String returnCode);

    /**
     * 查询：所有
     */
    List<SysReturnCode> selectAll();

    /**
     * 列表查询（支持条件搜索）
     */
    List<SysReturnCode> selectBySearch(Map<String, Object> map);
}
