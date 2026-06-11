package com.uoquo.platform.system.mapper;

import com.uoquo.platform.system.model.dto.SettingDto;
import com.uoquo.platform.system.model.pojo.SysSetting;
import com.uoquo.mybatis.sensitive.SensitiveField;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Set;

public interface SysSettingMapper {

    /**
     * 单条新增
     */
    int insert(SysSetting row);

    /**
     * 根据编码删除
     */
    int deleteByCode(String code);

    /**
     * 单条更新
     */
    int updateByCode(SysSetting row);

    /**
     * 批量更新
     */
    int batchUpdate(List<SysSetting> list);

    /**
     * 根据编码查询
     */
    SysSetting selectByCode(String code);

    /**
     * 根据关键字查列表
     */
    List<SysSetting> listByCodePrefix(@Param("prefix") String prefix, @Param("types") Set<String> types);

    /**
     * 查询作用范围查询
     */
    List<SysSetting> listByPublicType(@Param("publicType") String publicType);

}