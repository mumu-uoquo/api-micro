package com.uoquo.platform.message.service;

import com.uoquo.platform.message.model.dto.MsgInfoDto;
import com.uoquo.platform.message.model.dto.MsgInfoViewDto;
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
