package com.uoquo.platform.message.service;

import com.uoquo.platform.message.model.dto.MsgReceiverDto;
import com.uoquo.platform.message.model.dto.MsgReceiverSearchDto;
import com.uoquo.platform.message.model.param.MsgInfoListParam;
import com.uoquo.platform.message.model.param.MsgInfoReceiveParam;
import com.uoquo.platform.message.model.param.MsgReceiverSearchParam;
import com.uoquo.mybatis.page.PageResult;

import java.util.List;

/**
 * 接收消息服务
 * @author xuhz
 */
public interface MsgReceiverService {
    /**
     * 新增接收消息<br>
     * 注：调用前需判断 messageId 的有效性
     */
    String addReceiver(MsgInfoReceiveParam param);

    /**
     * 删除单条记录
     */
    void deleteReceiver(List<String> recordIds);

    /**
     * 批量删除记录
     */
    void deleteReceiver(String messageId, String receiverId);

    /**
     * 消息标记为已读（单条）
     */
    void markReceiverRead(String messageId, String receiverId, String description);

    /**
     * 消息标记为已读（批量）
     * @param recordIds 记录ID
     */
    void markReceiverRead(List<String> recordIds);

    /**
     * 消息标记为已读（所有）
     */
    void markAllUnreadReceiver();

    /**
     * 消息标记为已处理（单条）
     */
    void markReceiverProcessed(String messageId, String receiverId, String description);

    /**
     * 消息标记为已处理（批量）
     * @param recordIds 记录ID
     */
    void markReceiverProcessed(List<String> recordIds);

    /**
     * 消息标记为已处理（所有）
     */
    void markAllUnprocessedReceiver();

    /**
     * 获取消息接收列表
     */
    PageResult<MsgReceiverDto> listReceiverByMessageId(MsgInfoListParam param);

    /**
     * 根据发布范围搜索合适的目标信息
     */
    PageResult<MsgReceiverSearchDto> searchReceiverByRange(String rootInstituteId, MsgReceiverSearchParam param);
}
