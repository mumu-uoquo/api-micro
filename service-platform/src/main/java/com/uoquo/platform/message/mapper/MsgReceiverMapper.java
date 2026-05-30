package com.uoquo.platform.message.mapper;

import com.uoquo.platform.message.model.pojo.MsgReceiver;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface MsgReceiverMapper {

    /**
     * @mbg.generated generated automatically, do not modify!
     */
    int insert(MsgReceiver row);

    /**
     * 删除（逻辑删）
     * @mbg.generated generated automatically, do not modify!
     */
    int deleteByMap(Map<String, Object> map);

    /**
     * 更新已读状态
     * @mbg.generated generated automatically, do not modify!
     */
    int updateReadStatus(MsgReceiver row);

    /**
     * 更新处理状态
     * @mbg.generated generated automatically, do not modify!
     */
    int updateProcessedStatus(MsgReceiver row);

    /**
     * 单条查询
     * @mbg.generated generated automatically, do not modify!
     */
    MsgReceiver selectByPrimaryKey(String id);

    /**
     * 单条查询
     * @mbg.generated generated automatically, do not modify!
     */
    MsgReceiver selectByMessageAndReceiver(@Param("messageId") String messageId, @Param("receiverId") String receiverId);

    /**
     * 列表：仅查消息接收表（主要用于获取是否有新消息）
     * @mbg.generated generated automatically, do not modify!
     */
    List<MsgReceiver> listBySearch(Map<String, Object> map);

}