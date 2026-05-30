package com.uoquo.platform.system.mapper;

import com.uoquo.platform.system.model.pojo.SysDictionary;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SysDictionaryMapper {

    /**
     * 新增
     */
    int insert(SysDictionary row);

    /**
     * 更新
     */
    int update(SysDictionary row);

    /**
     * 删除（逻辑删）
     */
    int deleteByPrimaryKey(@Param("id") String id, @Param("deleteState") Long deleteState);

    /**
     * 获取指定字典
     */
    SysDictionary selectById(String id);

    /**
     * 获取指定字典
     */
    SysDictionary selectByCode(String code);

    /**
     * 获取首层字典
     */
    List<SysDictionary> selectByRoot();

    /**
     * 获取子节点
     */
    List<SysDictionary> selectByPrevCode(String code);

    /**
     * 获取所有
     */
    List<SysDictionary> selectAll();
}