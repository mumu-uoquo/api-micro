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
     * 批量新增（INSERT IGNORE：依赖唯一索引 uk_message_receiver(message_id, receiver_id)，已存在的记录自动跳过，可重复调用）
     * @return 实际新增的行数
     */
    int insertIgnoreBatch(@Param("list") List<MsgReceiver> list);

    /**
     * 按消息逻辑删除接收记录（每次最多 limit 条，用于过期清理时分批处理，避免一次更新过多行）
     * @return 本次删除的行数；小于 limit 表示该消息的接收记录已删完
     */
    int deleteByMessageLimit(@Param("messageId") String messageId, @Param("deleteState") Long deleteState, @Param("limit") int limit);

    /**
     * 按消息和接收人批量查询（仅未删除的记录），用于获取接收记录ID
     */
    List<MsgReceiver> listByMessageAndReceivers(@Param("messageId") String messageId, @Param("receiverIds") List<String> receiverIds);

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