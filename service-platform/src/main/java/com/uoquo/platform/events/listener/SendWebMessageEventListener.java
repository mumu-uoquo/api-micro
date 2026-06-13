package com.uoquo.platform.events.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.uoquo.cloud.events.RemoteEvent;
import com.uoquo.platform.common.DictionaryCodeEnum;
import com.uoquo.platform.message.model.pojo.SseMessage;
import com.uoquo.platform.message.service.SseEmitterService;
import com.uoquo.utils.StringUtil;
import com.uoquo.utils.json.JsonUtil;

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
     * 处理站内消息推送事件.<br>
     * eventName 优先取 {@link SseMessage#getEventName()}；
     * 未设置时根据 messageType 映射：
     * <ul>
     *   <li>020001（通知公告）→ notice</li>
     *   <li>020002（业务消息）→ message</li>
     *   <li>020003（待办任务）→ todo</li>
     *   <li>其他 → message（兜底）</li>
     * </ul>
     */
    @EventListener
    public void handleMessageEvent(RemoteEvent<SseMessage> event) {
        if (logger.isDebugEnabled()) {
            logger.debug("处理站内消息事件：{}", JsonUtil.serialize(event));
        }
        SseMessage message = event.getNewData();
        // 解析 eventName：优先使用消息自身携带的，否则按 messageType 映射，结果写回消息对象
        if (StringUtil.isNull(message.getEventName())) {
            String eventName = resolveEventName(message);
            message.setEventName(eventName);
        }
        try {
            // 踢人通知：按被踢会话的 token 精准推送，不受新登录连接替换影响
            Object kickToken = message.getBusinessExtend() != null
                    ? message.getBusinessExtend().get("token") : null;
            if (kickToken != null) {
                boolean sent = sseEmitterService.publishByToken(kickToken.toString(), message);
                logger.info("给用户[{}]按 token 推送[{}]踢人消息完成，连接存在[{}].",
                        message.getReceiverId(), message.getEventName(), sent);
                return;
            }
            // 普通消息：定向推送（指定了 targetAppKey）或推送到用户所有在线客户端
            int count = sseEmitterService.publish(message.getReceiverId(), message.getTargetAppKey(), message);
            logger.info("给用户[{}]推送[{}]事件消息[{}]完成，送达连接数[{}].",
                    message.getReceiverId(), message.getEventName(), message.getMessageContent(), count);
        } catch (Exception e) {
            logger.warn("给用户[{}]推送[{}]事件消息[{}]异常：{}",
                    message.getReceiverId(), message.getEventName(), JsonUtil.serialize(message), e);
        }
    }

    /**
     * 解析 SSE 事件名.<br>
     * 优先取 {@link SseMessage#getEventName()}，未设置时按 messageType 映射。
     */
    private String resolveEventName(SseMessage message) {
        String messageType = message.getMessageType();
        if (DictionaryCodeEnum.MESSAGE_TYPE_NOTICE.getCode().equals(messageType)) {
            return "notice";
        } else if (DictionaryCodeEnum.MESSAGE_TYPE_TODO.getCode().equals(messageType)) {
            return "todo";
        } else {
            // MESSAGE_TYPE_SYSTEM 及未知类型，兜底为 message
            return "message";
        }
    }
}
