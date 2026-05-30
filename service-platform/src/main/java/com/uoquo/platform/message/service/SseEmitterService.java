package com.uoquo.platform.message.service;

import com.uoquo.platform.common.SseMessageTypeEnum;
import com.uoquo.platform.message.model.pojo.SseMessage;
import com.uoquo.utils.CurrentUser;
import com.uoquo.utils.StringUtil;
import com.uoquo.utils.json.JsonUtil;
import com.uoquo.web.exception.SystemErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import jakarta.annotation.Nullable;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.validation.constraints.NotNull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * SSE连接管理器<br>
 * 内置的消息类型：{@link SseMessageTypeEnum SseMessageTypeEnum}
 * @author xuhz
 */
@Component
public class SseEmitterService {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Map<String, Map<String, SseEmitter>> emitters = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    @PostConstruct
    public void init() {
        // 全局心跳任务，每30秒执行一次
        scheduler.scheduleAtFixedRate(this::sendHeartbeats, 30, 30, TimeUnit.SECONDS);
    }

    @PreDestroy
    public void cleanup() {
        scheduler.shutdown();
    }

    /**
     * 订阅消息（默认10分钟超时）
     */
    public SseEmitter subscribe(@NotNull String userId, @NotNull String appkey) {
        SseEmitter emitter = new SseEmitter(10 * 60 * 1000L);
        return subscribe(userId, appkey, emitter);
    }

    /**
     * 订阅消息
     */
    public SseEmitter subscribe(@NotNull String userId, @NotNull String appkey, SseEmitter emitter) {
        if (StringUtil.isNull(userId) || StringUtil.isNull(appkey)) {
            return null;
        }
        // 1. 注册相关事件
        String traceId = CurrentUser.getTraceId();
        emitter.onCompletion(() ->{
            try {
                MDC.put("requestId", traceId);
                logger.info("用户[{}]客户端[{}]的 SSE 连接已关闭", userId, appkey);
                removeDeadEmitters(userId, List.of(appkey));
            } catch (Exception e) {
                logger.debug("用户[{}]客户端[{}]的 SSE 链接已出错", userId, appkey, e);
            } finally {
                MDC.remove("requestId");
            }
        });
        emitter.onTimeout(() -> {
            try {
                MDC.put("requestId", traceId);
                logger.warn("用户[{}]客户端[{}]的 SSE 连接已超时", userId, appkey);
                removeDeadEmitters(userId, List.of(appkey));
            } catch (Exception e) {
                logger.debug("用户[{}]客户端[{}]的 SSE 链接已出错", userId, appkey, e);
            } finally {
                MDC.remove("requestId");
            }
        });
        emitter.onError(e -> {
            try {
                MDC.put("requestId", traceId);
                logger.error("用户[{}]客户端[{}]的 SSE 连接已出错", userId, appkey, e);
                removeDeadEmitters(userId, List.of(appkey));
            } catch (Exception err) {
                logger.debug("用户[{}]客户端[{}]的 SSE 链接已出错", userId, appkey, err);
            } finally {
                MDC.remove("requestId");
            }
        });
        // 2. 关闭旧链接
        Map<String, SseEmitter> appEmitters = emitters.getOrDefault(userId, new ConcurrentHashMap<>());
        SseEmitter oldEmitter = appEmitters.get(appkey);
        if (oldEmitter != null) {
            try {
                oldEmitter.complete();
                logger.debug("关闭用户[{}]的 SSE 旧链接成功", userId);
            } catch (Exception e) {
                logger.warn("关闭用户[{}]的 SSE 旧链接出错", userId, e);
            }
        }
        // 3. 初始化
        try {
            // 发送一个初始事件保持连接
            emitter.send(SseEmitter.event().data(JsonUtil.serialize("连接已建立")));
            // 加入缓存
            appEmitters.put(appkey, emitter);
            logger.info("用户[{}]的 SSE 连接建立成功", userId);
        } catch (IOException e) {
            logger.error("用户[{}]的 SSE 推送初始事件出错", userId, e);
            emitter.completeWithError(e);
        }
        // 4. 缓存连接信息
        emitters.put(userId, appEmitters);
        return emitter;
    }

    /**
     * 下推消息：业务消息
     */
    public void publishMessage(@NotNull String userId, @Nullable String appkey, @NotNull SseMessage message) {
        if (StringUtil.isNull(userId)) {
            return;
        }
        // 构建消息
        SseEmitter.SseEventBuilder build = getMessageBuilder(SseMessageTypeEnum.MESSAGE, message);
        // 定向推送
        Map<String, SseEmitter> appEmitters = getEmitters(userId, appkey);
        List<String> deadAppkeys = this.publish(userId, appEmitters, List.of(build));
        // 删除无效链接
        int count = removeDeadEmitters(userId, deadAppkeys);
        // 如果无连接则抛出异常
        if (count == 0) {
            throw new SystemErrorException("用户[%s]无 SSE 连接，无法推送消息", userId);
        }
    }

    /**
     * 定向推送：系统通知（如被踢下线等）
     */
    public void publishNotice(@NotNull String userId, @Nullable String appkey, @NotNull SseMessage message) {
        if (StringUtil.isNull(userId) || StringUtil.isNull(appkey)) {
            return;
        }
        // 构建消息
        SseEmitter.SseEventBuilder build = getMessageBuilder(SseMessageTypeEnum.WARNING, message);
        // 定向推送
        Map<String, SseEmitter> appEmitters = getEmitters(userId, appkey);
        List<String> deadAppkeys = this.publish(userId, appEmitters, List.of(build));
        // 删除无效链接
        int count = removeDeadEmitters(userId, deadAppkeys);
        // 如果无连接则抛出异常
        if (count == 0) {
            throw new SystemErrorException("用户[%s]无 SSE 连接，无法推送消息", userId);
        }
    }

    /**
     * 全员广播：消息
     */
    public void broadcast(@NotNull SseMessage message) {
        this.broadcast(SseMessageTypeEnum.MESSAGE, message);
    }

    /**
     * 全员广播
     */
    private void broadcast(@NotNull SseMessageTypeEnum type, @Nullable SseMessage message) {
        // 消息构建
        SseEmitter.SseEventBuilder build = getMessageBuilder(type, message);
        // 消息广播
        List<String> deadUsers = new ArrayList<>();
        for (String userId : emitters.keySet()) {
            Map<String, SseEmitter> appEmitters = emitters.get(userId);
            List<String> deadAppkeys = this.publish(userId, appEmitters, List.of(build));
            // 删除失效的链接
            deadAppkeys.forEach(appEmitters::remove);
            // 记录无连接的用户信息
            if (appEmitters.isEmpty()) {
                deadUsers.add(userId);
            }
        }
        // 删除失效的用户连接
        deadUsers.forEach(emitters::remove);
    }

    private SseEmitter.SseEventBuilder getMessageBuilder(@NotNull SseMessageTypeEnum type, @Nullable SseMessage message) {
        SseEmitter.SseEventBuilder builder = SseEmitter.event().name(type.name());
        if (message != null) {
            builder.data(JsonUtil.serialize(message));
            if (StringUtil.notNull(message.getRecordId())) {
                builder.id(message.getRecordId());
            } else if (StringUtil.notNull(message.getMessageId())) {
                builder.id(message.getMessageId());
            }
        }
        return builder;
    }

    /**
     * 获取用户的SSE链接
     * @param userId 用户ID
     * @param appkey 应用ID（为空时返回所有链接）
     */
    private Map<String, SseEmitter> getEmitters(String userId, String appkey) {
        Map<String, SseEmitter> appEmitters = emitters.getOrDefault(userId, new ConcurrentHashMap<>());
        if (appEmitters.isEmpty()) {
            logger.debug("用户[{}]无 SSE 链接，无法推送消息.", userId);
            throw new SystemErrorException("用户[%s]无 SSE 链接，无法推送消息", userId);
        }
        if (StringUtil.isNull(appkey)) {
            return appEmitters;
        }
        Map<String, SseEmitter> tempEmitters = new ConcurrentHashMap<>();
        SseEmitter emitter = appEmitters.get(appkey);
        if (emitter == null) {
            logger.debug("用户[{}]无客户端[{}]的 SSE 链接，无法推送消息.", userId, appkey);
            throw new SystemErrorException("用户[%s]无[%s]SSE 链接，无法推送消息", userId, appkey);
        }
        tempEmitters.put(appkey, emitter);
        return tempEmitters;
    }

    /**
     * 给用户下发消息
     * @return 返回无效链接对应的appkey列表
     */
    private List<String> publish(String userId, Map<String, SseEmitter> appEmitters, List<SseEmitter.SseEventBuilder> list) {
        // 发送失败的列表
        List<String> deadAppkeys = new ArrayList<>();
        // 遍历用户的每个appkey
        for (String appkey : appEmitters.keySet()) {
            SseEmitter emitter = appEmitters.get(appkey);
            try {
                logger.debug("广播 SSE 消息给用户[{}]客户端[{}]开始.", userId, appkey);
                for (SseEmitter.SseEventBuilder item : list) {
                    emitter.send(item);
                }
                logger.debug("广播 SSE 消息给用户[{}]客户端[{}]成功.", userId, appkey);
            } catch (Exception e) {
                deadAppkeys.add(appkey);
                try {
                    // 此处不打印详细日志，交由 emitter.onError 处理
                    StringBuilder sb = new StringBuilder();
                    for (SseEmitter.SseEventBuilder item : list) {
                        item.build().forEach(d -> sb.append(d.getData()).append("\n"));
                    }
                    logger.error("广播 SSE 消息给用户[{}]客户端[{}]失败：{}, error={}", userId, appkey, sb.toString(), e.getMessage());
                    emitter.completeWithError(e);
                } catch (Exception e2) {
                    // do nothing
                }
            }
        }
        return deadAppkeys;
    }

    /**
     * 移除已关闭的SSE连接
     * @return 剩余的连接数量
     */
    private int removeDeadEmitters(String userId, List<String> deadAppkeys) {
        Map<String, SseEmitter> appEmitters = emitters.getOrDefault(userId, new ConcurrentHashMap<>());
        deadAppkeys.forEach(appEmitters::remove);
        if (appEmitters.isEmpty()) {
            emitters.remove(userId);
        }
        return appEmitters.size();
    }

    /**
     * 发送心跳包
     */
    private void sendHeartbeats() {
        synchronized (emitters) {
            this.broadcast(SseMessageTypeEnum.HEARTBEAT, null);
            logger.debug("发送全局心跳，当前在线用户数: {}", emitters.size());
        }
    }

//    @Async
//    public void testPublish(String userId, String lastId) {
//        int prevId = lastId == null ? 0 : Integer.parseInt(lastId) ;
//        for (int i = prevId; i < prevId+10; i++) {
//            String message = "测试消息-" + i;
//            try {
//                publish(userId, i+"", "MESSAGE", message);
//                Thread.sleep(1000L * 5);
//            } catch (Exception e) {
//                logger.error("轮询给用户[{}]发送SSE消息[{}]出错：", userId, message, e);
//                return;
//            }
//        }
//    }
}
