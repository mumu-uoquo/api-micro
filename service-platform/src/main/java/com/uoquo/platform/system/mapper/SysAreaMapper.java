package com.uoquo.platform.system.mapper;

import com.uoquo.platform.system.model.pojo.SysArea;

import java.util.List;

public interface SysAreaMapper {

    /**
     * 根据编码查询
     */
    SysArea selectByPrimaryKey(String id);

    /**
     * 根据关键字查列表
     */
    List<SysArea> selectByPrevCode(String code);
}