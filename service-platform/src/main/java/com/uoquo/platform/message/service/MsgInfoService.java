package com.uoquo.platform.message.service;

import com.uoquo.platform.message.model.dto.MsgFanoutDto;
import com.uoquo.platform.message.model.dto.MsgInfoDto;
import com.uoquo.platform.message.model.dto.MsgInfoViewDto;
import com.uoquo.platform.message.model.param.MsgFanoutParam;
import com.uoquo.platform.message.model.param.MsgInfoListParam;
import com.uoquo.platform.message.model.param.MsgInfoParam;
import com.uoquo.mybatis.page.PageResult;

import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

/**
 * 消息记录服务
 * @author xuhz
 */
public interface MsgInfoService {

    /**
     * 新增消息
     */
    String addMessage(MsgInfoParam param);

    /**
     * 删除消息<br>
     * 调用前已判断权限
     */
    void deleteMessage(@NotNull String messageId);

    /**
     * 更新消息<br>
     * 调用前已判断权限
     */
    void updateMessage(MsgInfoParam param);

    /**
     * 发布消息
     * <ol>
     *     <li>调用前已判断权限</li>
     *     <li>消息只能发布一次</li>
     *     <li>该方法仅修改message_info的信息，然后发布事件</li>
     *     <li>真正由事件监听器入消息接收表</li>
     * </ol>
     */
    void publishMessage(MsgInfoParam param);

    /**
     * 取消定时发布（只清空发布时间，消息仍是待发布状态）<br>
     * 调用前已判断权限
     */
    void cancelSchedule(@NotNull String messageId);

    /**
     * 发布到点的定时消息（由 scheduler-center 定时调用，可多节点并发调用：发布使用 CAS，每条消息只会发布一次）
     * @param limit 单次最多处理的条数
     * @return 本次实际发布的条数
     */
    int publishDueMessages(int limit);

    /**
     * 清理过期消息（由 scheduler-center 定时调用）：逻辑删除过期超过 retainDays 天的消息及其接收记录，
     * 接收记录分批删除，单次调用超过 maxSeconds 秒则提前结束，剩余的下次继续
     * @return 本次清理的消息条数；因超时提前结束（还有没清理完的）时至少返回1，调用方可以继续调用；返回0表示没有需要清理的了
     */
    int clearExpiredMessages(int retainDays, int limit, int maxSeconds);

    /**
     * 领取群发任务（CAS）：同一条消息同一时刻只有一个节点能领取成功
     */
    MsgFanoutDto claimFanout(MsgFanoutParam param);

    /**
     * 更新群发进度
     * @return false 表示消息已被撤回/删除，或任务已被其他节点接管，调用方应停止群发
     */
    boolean updateFanoutProgress(MsgFanoutParam param);

    /**
     * 群发心跳：处理一批的过程中定期调用，只更新进度时间，避免被误判为中断
     * @return false 表示消息已被撤回/删除，或任务已被其他节点接管，调用方应停止群发
     */
    boolean heartbeatFanout(MsgFanoutParam param);

    /**
     * 查询需要继续群发的消息（已发布，但群发未开始或中断超过 staleSeconds 秒），含正文
     */
    List<MsgInfoDto> listStalledFanout(int staleSeconds, int maxRetry, int limit);

    /**
     * 消息重推<br>
     * 调用前已判断权限
     */
    void retryPushMessage(@NotNull String logId, @NotNull String messageId, @NotNull String receiverId);

    /**
     * 撤回消息<br>
     * 调用前已判断权限
     */
    void withdrawMessage(@NotNull String messageId);

    /**
     * 消息列表（仅消息本身详情）
     */
    PageResult<MsgInfoDto> listMessage(MsgInfoListParam param);

    /**
     * 我收到的消息（含阅读状态、处理状态）
     */
    PageResult<MsgInfoViewDto> listMessage4View(MsgInfoListParam param);

    /**
     * 我的未读消息<br>
     * 已发布且未过期的未读消息
     */
    List<MsgInfoViewDto> listUnreadMessage(@Nullable Date startTime, @Nullable Date endTime);

    /**
     * 消息详情（管理用）<br>
     * 仅消息本身详情
     */
    MsgInfoDto getMessageDetail(@NotNull String messageId);

    /**
     * 消息详情（阅读用）<br>
     * 含阅读状态、处理状态等
     */
    MsgInfoViewDto getMessage4View(@NotNull String messageId);

    /**
     * 下载附件
     */
    void downloadAttachment(@NotNull String attachmentId, boolean checkAuth, HttpServletRequest request, HttpServletResponse response);

}
