package com.uoquo.platform.events.listener;

import com.uoquo.cloud.events.RemoteEvent;
import com.uoquo.platform.common.BusinessTypeEnum;
import com.uoquo.platform.message.model.dto.MsgInfoViewDto;
import com.uoquo.platform.message.model.pojo.MsgInfo;
import com.uoquo.platform.message.model.pojo.SseMessage;
import com.uoquo.platform.message.service.SseEmitterService;
import com.uoquo.utils.json.JsonUtil;
import com.uoquo.web.SystemReturnCode;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * 站内消息事件监听器
 * @author xuhz
 */
@Component
public class SendWebMessageEventListener {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final SseEmitterService sseEmitterService;

    /**
     * 构造器注入（若 Service 不存在，启动时直接报错，而非运行时 NPE）
     */
    public SendWebMessageEventListener(SseEmitterService sseEmitterService) {
        this.sseEmitterService = sseEmitterService;
    }

    @PostConstruct
    public void setProperties(){
        logger.debug("SendWebMessageEventListener init ...");
    }

    /**
     * 处理账户登出事件
     */
    @EventListener
    public void handleMessageEvent(RemoteEvent<SseMessage> event) {
        if (logger.isDebugEnabled()) {
            logger.debug("处理站内消息事件：{}", JsonUtil.serialize(event));
        }
        SseMessage message = event.getNewData();
        try {
            if (BusinessTypeEnum.AUTH.getCode().equals(message.getBusinessType()) &&
                    SystemReturnCode.ACCOUNT_KICK_OUT.getCode().equals(message.getOperationStatus())) {
                // 通知消息：被踢下线通知
                sseEmitterService.publishNotice(message.getReceiverId(), message.getAppKey(), message);
                logger.info("给用户[{}]推送到客户端[{}]的[NOTICE]消息[{}]完成.", message.getReceiverId(), message.getAppKey(), message.getMessageContent());
            } else {
                // 系统消息：如通知公告等
                sseEmitterService.publishMessage(message.getReceiverId(), null, message);
                // TODO 推送成功后，记录消息送达时间
                logger.info("给用户[{}]推送[{}]消息[{}]完成.", message.getReceiverId(), message.getMessageId(), message.getMessageContent());
            }
        } catch (Exception e) {
            logger.warn("给用户[{}]推送[{}]消息[{}]异常.", message.getReceiverId(), message.getMessageId(), message.getMessageContent(), e);
        }
    }

    @NotNull
    private MsgInfoViewDto getMsgInfoViewDto(String receiverId, MsgInfo data) {
        MsgInfoViewDto message = new MsgInfoViewDto();
        message.setMessageId(data.getId());
        message.setReceiverId(receiverId);
        message.setMessageTitle(data.getMessageTitle());
        message.setMessageContent(data.getMessageContent());
        message.setMessageType(data.getMessageType());
        message.setMessageLevel(data.getMessageLevel());
        message.setBusinessType(data.getBusinessType());
        message.setBusinessId(data.getBusinessId());
        message.setBusinessExtend(data.getBusinessExtend());
        message.setSenderId(data.getSenderId());
        message.setSenderName(data.getSenderName());
        message.setSenderTime(data.getSenderTime());
        return message;
    }
}
