package com.uoquo.platform.message.mapper;

import com.uoquo.platform.message.model.pojo.MsgPushLog;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface MsgPushLogMapper {

    /**
     * 新增
     * @mbg.generated generated automatically, do not modify!
     */
    int insert(MsgPushLog row);

    /**
     * 更新推送状态
     * @mbg.generated generated automatically, do not modify!
     */
    int updatePushStatus(@Param("id") String id, @Param("pushStatus") String pushStatus);

    /**
     * 单条查询
     * @mbg.generated generated automatically, do not modify!
     */
    MsgPushLog selectByPrimaryKey(String id);

    /**
     * 批量查询
     * @mbg.generated generated automatically, do not modify!
     */
    List<MsgPushLog> listByMessageId(String messageId);

    /**
     * 批量查询
     * @mbg.generated generated automatically, do not modify!
     */
    List<MsgPushLog> listBySearch(Map<String, Object> map);

}