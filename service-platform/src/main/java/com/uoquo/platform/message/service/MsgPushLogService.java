package com.uoquo.platform.message.service;

import com.uoquo.platform.message.model.dto.MsgPushLogDto;
import com.uoquo.platform.message.model.param.MsgInfoListParam;
import com.uoquo.platform.message.model.param.MsgPushLogParam;
import com.uoquo.mybatis.page.PageResult;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

/**
 * 消息推送日志服务
 * @author uoquo
 */
public interface MsgPushLogService {
    /**
     * 添加消息推送日志
     */
    String addPushLog(MsgPushLogParam param);

    /**
     * 获取指定消息的推送日志
     */
    PageResult<MsgPushLogDto> listPushLogByMessageId(MsgInfoListParam param);

    /**
     * 获取推送失败的消息推送日志（每次最多返回100条）<br>
     * 获取指定范围内状态为“失败”的记录
     */
    List<MsgPushLogDto> listPushLogByFailed(@Nullable String messageId, @Nullable Date startTime, @Nullable Date endTime);

    /**
     * 日志详情
     */
    MsgPushLogDto getPushLogDetail(@NotNull String logId);
}
