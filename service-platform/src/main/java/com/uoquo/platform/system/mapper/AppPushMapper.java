package com.uoquo.platform.system.mapper;

import com.uoquo.platform.system.model.dto.AppPushDto;
import com.uoquo.platform.system.model.pojo.AppPush;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AppPushMapper {

    /**
     * 新增
     */
    int insert(AppPush row);

    /**
     * 删除（逻辑删）
     */
    int deleteByPrimaryKey(@Param("id") String id, @Param("deleteState") Long deleteState);

    /**
     * 修改
     */
    int updateByPrimaryKey(AppPush row);

    /**
     * 单条：根据ID
     */
    AppPush selectByPrimaryKey(String id);

    /**
     * 列表：根据 appId 查询（不分页）
     */
    List<AppPushDto> selectByAppId(String appId);
}
