package com.uoquo.scheduler.platform.listener;

import com.uoquo.cloud.events.RemoteEvent;
import com.uoquo.scheduler.common.BusinessOperationEnum;
import com.uoquo.scheduler.common.BusinessTypeEnum;
import com.uoquo.scheduler.common.DictionaryCodeEnum;
import com.uoquo.scheduler.platform.model.dto.UserInfoDto;
import com.uoquo.scheduler.platform.model.param.MsgInfoReceiveParam;
import com.uoquo.scheduler.platform.model.param.MsgPushLogParam;
import com.uoquo.scheduler.platform.model.param.UserListByRangeParam;
import com.uoquo.scheduler.platform.model.pojo.MsgInfo;
import com.uoquo.scheduler.platform.model.pojo.SseMessage;
import com.uoquo.scheduler.platform.remote.MessageRemoteService;
import com.uoquo.scheduler.platform.remote.UserRemoteService;
import com.uoquo.utils.StringUtil;
import com.uoquo.utils.json.JsonUtil;
import com.uoquo.web.SystemReturnCode;
import com.uoquo.web.events.UoquoEventPublisher;
import com.uoquo.mybatis.page.PageResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.Date;
import java.util.List;

/**
 * 事件监听器：消息发布（009030） <br>
 * 只处理业务事件本身，记录操作日志等通用逻辑在{@link AllEventListener AllEventListener}中统一处理
 * @author xuhz
 */
@Component
public class MessageInfoEventListener {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private UoquoEventPublisher eventPublisher;

    @Autowired
    private MessageRemoteService messageRemoteService;

    @Autowired
    private UserRemoteService userRemoteService;

    @PostConstruct
    public void setProperties(){
        logger.debug("MessageInfoEventListener init ...");
    }

    /**
     * 监听事件：消息发布（009030）
     */
    @EventListener
    public void listenMessageInfoEvent(RemoteEvent<MsgInfo> event) {
        // 重发消息需忽略（常见于运维重发）
        if (event.isRetry()) {
            logger.debug("重发的事件：{id:\"{}\", bizType:\"{}\", bizSub:\"{}\", bizId:\"{}\", opsType:\"{}\", opsId:\"{}\"}",
                    event.getId(), event.getBusinessType(), event.getBusinessSubType(), event.getBusinessId(), event.getOperationType(), event.getOperatorId());
            return;
        }
        // 正常消息处理
        try {
            // 只需要处理发布事件，其他操作仅需记日志
            if (BusinessOperationEnum.MESSAGE_PUBLISH.getCode().equals(event.getOperationType())) {
                MsgInfo message = event.getNewData();
                // 查询指定范围的用户
                UserListByRangeParam param = new UserListByRangeParam();
                param.setPageNum(1);
                param.setPageSize(1000);
                param.setReceiverRange(message.getReceiverRange());
                param.setReceiverInstituteId(message.getReceiverInstituteId());
                if (StringUtil.notNull(message.getReceiverIds())) {
                    param.setReceiverIds(List.of(message.getReceiverIds().split(",")));
                }
                PageResult<UserInfoDto> result = userRemoteService.listUserByRange(param);
                while (result.getSize() > 0) {
                    this.pushMessage2User(event, message, result.getResult());
                    // 若还有下一页，则继续请求
                    if (result.isNextPage()) {
                        param.setPageNum(param.getPageNum() + 1);
                        result = userRemoteService.listUserByRange(param);
                    } else {
                        break;
                    }
                }
            }
            if (logger.isDebugEnabled()) {
                logger.debug("账号[{}]发布消息[{}]完毕：{}", event.getOperatorId(), event.getBusinessId(), JsonUtil.serialize(event));
            }
        } catch (Exception e) {
            logger.error("账号[{}]发布消息[{}]失败：", event.getOperatorId(), event.getBusinessId(), e);
        }
    }

    private void pushMessage2User(RemoteEvent<MsgInfo> event, MsgInfo message, List<UserInfoDto> users) {
        for (UserInfoDto user : users) {
            try {
                // 1. 添加接收记录
                String recodeId = this.saveReceiver4Message(message.getId(), user.getId(), user.getUserName());
                // 2. 按指定方式推送消息
                String pushResult = null;
                if (DictionaryCodeEnum.PUSH_TYPE_WX.getCode().equals(message.getPushWay())) {
                    pushResult = this.sendMessage2Weixin(message, user);
                } else if (DictionaryCodeEnum.PUSH_TYPE_APP.getCode().equals(message.getPushWay())) {
                    pushResult = this.sendMessage2App(message, user);
                } else if (DictionaryCodeEnum.PUSH_TYPE_SMS.getCode().equals(message.getPushWay())) {
                    pushResult = this.sendMessage2Sms(message, user);
                }
                // 3. 推送站内信息
                String pushWebResult = this.sendMessage2Web(event, message, recodeId, user.getId());
                // 4. 记录推送日志
                this.savePushLog4Message(message, user, (pushResult == null) ? pushWebResult : pushWebResult);
                logger.debug("将消息[{}]以方式[{}]给用户[{}]推送完毕", message.getId(), message.getPushWay(), user.getId());
            } catch (Exception e) {
                logger.error("将消息[{}]以方式[{}]给用户[{}]推送失败：", message.getId(), message.getPushWay(), user.getId(), e);
            }
        }
    }

    /**
     * 添加记录：接收信息
     */
    private String saveReceiver4Message(String messageId, String receiverId, String receiverName) {
        MsgInfoReceiveParam param = new MsgInfoReceiveParam();
        param.setMessageId(messageId);
        param.setReceiverId(receiverId);
        param.setReceiverName(receiverName);
        return messageRemoteService.addReceiver4Message(param);
    }

    /**
     * 添加记录：推送日志
     */
    private void savePushLog4Message(MsgInfo message, UserInfoDto user, String pushResult) {
        MsgPushLogParam param = new MsgPushLogParam();
        // 消息信息
        param.setMessageId(message.getId());
        param.setPushWay(message.getPushWay());
        // 接收信息
        param.setReceiverId(user.getId());
        param.setReceiverName(user.getUserName());
        // 推送情况
        param.setPushStatus(StringUtil.isNull(pushResult) ? DictionaryCodeEnum.PUSH_STATUS_PUSHED.getCode() : DictionaryCodeEnum.PUSH_STATUS_FAILED.getCode());
        param.setPushResult(pushResult);
        param.setPushTime(new Date());

        messageRemoteService.addPushLog4Message(param);
    }
    /**
     * 消息推送：微信
     */
    private String sendMessage2Weixin(MsgInfo message, UserInfoDto user) {
        // TODO
        messageRemoteService.send2Weixin();
        return DictionaryCodeEnum.PUSH_STATUS_PUSHED.getCode();
    }

    /**
     * 消息推送：APP
     */
    private String sendMessage2App(MsgInfo message, UserInfoDto user) {
        // TODO
        messageRemoteService.send2App();
        return DictionaryCodeEnum.PUSH_STATUS_PUSHED.getCode();
    }

    /**
     * 消息推送：短信
     */
    private String sendMessage2Sms(MsgInfo message, UserInfoDto user) {
        // TODO
        messageRemoteService.send2Sms();
        return DictionaryCodeEnum.PUSH_STATUS_PUSHED.getCode();
    }

    /**
     * 消息推送：站内
     */
    private String sendMessage2Web(RemoteEvent<MsgInfo> event, MsgInfo info, String recodeId, String receiverId) {
        // 1.1 构建SSE事件信息
        RemoteEvent<SseMessage> sseEvent = new RemoteEvent<>(BusinessTypeEnum.SSE.getCode(), BusinessOperationEnum.MESSAGE_SEND.getCode(), SystemReturnCode.SUCCESS.getCode(), "service-platform");
        sseEvent.copy(event);
        // 由于是内部发送的，不需要token和traceId
        sseEvent.setToken(null);
        sseEvent.setTraceId(null);
        // 1.2 具体的SSE信息
        SseMessage message = new SseMessage();
        // 基本信息
        message.setRecordId(recodeId);
        message.setReceiverId(receiverId);
        message.setMessageId(info.getId());
        message.setMessageType(info.getMessageType());
        message.setMessageLevel(info.getMessageLevel());
        // 发给所有客户端
        message.setAppKey(null);
        // 业务相关（与源事件一致）
        message.setBusinessId(event.getBusinessId());
        message.setBusinessType(event.getBusinessType());
        message.setBusinessSubType(event.getBusinessSubType());
        message.setOperationType(event.getOperationType());
        message.setOperationStatus(event.getOperationStatus());
        // 消息内容
        message.setMessageTitle(info.getMessageTitle());
        message.setMessageContent(info.getMessageContent());
        message.setBusinessExtend(info.getBusinessExtend());
        // 1.3 发送消息
        sseEvent.setNewData(message);
        eventPublisher.publishEvent(sseEvent);
        logger.info("用户[{}]在设备[{}]上登录客户端[{}]，其他设备将被踢下线消息发送成功", event.getOperatorId(), event.getAppDeviceId(), event.getAppKey());
        return DictionaryCodeEnum.PUSH_STATUS_PUSHED.getCode();
    }

}
