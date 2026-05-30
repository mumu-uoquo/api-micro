package com.uoquo.platform.logs.mapper;

import com.uoquo.platform.logs.model.pojo.LogUserOnline;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface LogUserOnlineMapper {

    /**
     * 插入或更新在线用户记录（INSERT ... ON DUPLICATE KEY UPDATE）
     */
    int upsert(LogUserOnline row);

    /**
     * 根据 userId 和 appKey 删除在线记录
     */
    int deleteByUserIdAndAppKey(@Param("userId") String userId, @Param("appKey") String appKey);

    /**
     * 根据 userId 删除在线记录（appKey 为空时的兜底删除）
     */
    int deleteByUserId(String userId);

    /**
     * 根据主键 id 删除在线记录
     */
    int deleteById(String id);

    /**
     * 根据主键 id 查询单条在线记录
     */
    LogUserOnline selectById(String id);

    /**
     * 动态条件查询在线用户列表
     */
    List<LogUserOnline> listBySearch(Map<String, Object> param);
}
