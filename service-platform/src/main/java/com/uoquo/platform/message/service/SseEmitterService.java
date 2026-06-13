package com.uoquo.platform.message.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.uoquo.platform.message.model.pojo.SseMessage;
import com.uoquo.utils.CurrentUser;
import com.uoquo.utils.StringUtil;
import com.uoquo.utils.json.JsonUtil;
import com.uoquo.web.exception.SystemErrorException;

import jakarta.annotation.Nullable;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.validation.constraints.NotNull;

/**
 * SSE连接管理器
 * <p>
 * eventName 统一从 {@link SseMessage#getEventName()} 取得，调用方必须在消息对象上设置。
 * 内置 HEARTBEAT 心跳事件由服务端定时发送，不对外暴露。
 * </p>
 * @author xuhz
 */
@Component
public class SseEmitterService {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Map<String, Map<String, SseEmitter>> emitters = new ConcurrentHashMap<>();
    /** 调度线程：仅负责触发心跳任务，不执行实际 IO */
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    /** 广播线程池：执行实际的 send IO，避免阻塞调度线程 */
    private final ExecutorService broadcastExecutor = Executors.newCachedThreadPool();

    /** 内置心跳事件名，不对外暴露 */
    private static final String EVENT_HEARTBEAT = "HEARTBEAT";

    @PostConstruct
    public void init() {
        // 全局心跳任务，每30秒执行一次
        scheduler.scheduleAtFixedRate(this::sendHeartbeats, 30, 30, TimeUnit.SECONDS);
    }

    @PreDestroy
    public void cleanup() {
        scheduler.shutdown();
        broadcastExecutor.shutdown();
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
        // 1. 注册相关事件（onTimeout 触发后 Spring 会自动调用 onCompletion，统一由 onCompletion 清理连接）
        String traceId = CurrentUser.getTraceId();
        emitter.onCompletion(() -> {
            try {
                MDC.put("requestId", traceId);
                logger.info("用户[{}]客户端[{}]的 SSE 连接已关闭", userId, appkey);
                removeDeadEmitters(userId, List.of(appkey));
            } catch (Exception e) {
                logger.debug("用户[{}]客户端[{}]的 SSE 链接清理出错", userId, appkey, e);
            } finally {
                MDC.remove("requestId");
            }
        });
        emitter.onTimeout(() -> {
            // 超时后 Spring 自动调用 onCompletion，此处只记录日志
            logger.warn("用户[{}]客户端[{}]的 SSE 连接已超时", userId, appkey);
            emitter.complete();
        });
        emitter.onError(e -> {
            // 出错后调用 completeWithError，Spring 自动调用 onCompletion，此处只记录日志
            logger.error("用户[{}]客户端[{}]的 SSE 连接已出错", userId, appkey, e);
            emitter.completeWithError(e);
        });
        // 2. 关闭同一 appkey 的旧连接（computeIfAbsent 避免并发订阅时 userId 对应的 map 被覆盖）
        Map<String, SseEmitter> appEmitters = emitters.computeIfAbsent(userId, k -> new ConcurrentHashMap<>());
        SseEmitter oldEmitter = appEmitters.get(appkey);
        if (oldEmitter != null) {
            try {
                oldEmitter.complete();
                logger.debug("关闭用户[{}]客户端[{}]的 SSE 旧链接成功", userId, appkey);
            } catch (Exception e) {
                logger.warn("关闭用户[{}]客户端[{}]的 SSE 旧链接出错", userId, appkey, e);
            }
        }
        // 3. 先放入缓存，再发送初始事件（避免初始事件发送成功但缓存未更新的窗口期）
        appEmitters.put(appkey, emitter);
        try {
            emitter.send(SseEmitter.event().name("CONNECTED").data(JsonUtil.serialize("连接已建立")));
            logger.info("用户[{}]客户端[{}]的 SSE 连接建立成功", userId, appkey);
        } catch (IOException e) {
            logger.error("用户[{}]客户端[{}]的 SSE 推送初始事件出错", userId, appkey, e);
            appEmitters.remove(appkey);
            emitter.completeWithError(e);
        }
        return emitter;
    }

    /**
     * 定向推送消息.<br>
     * eventName 取自 {@link SseMessage#getEventName()}，为空时抛出异常。
     *
     * @param userId  接收用户 ID
     * @param appkey  目标客户端（为空时推送给该用户所有在线客户端）
     * @param message 消息体（必须设置 eventName）
     * @return 实际送达的连接数
     */
    public int publish(@NotNull String userId, @Nullable String appkey, @NotNull SseMessage message) {
        if (StringUtil.isNull(userId)) {
            return 0;
        }
        if (StringUtil.isNull(message.getEventName())) {
            throw new SystemErrorException("SSE 消息 eventName 不能为空");
        }
        SseEmitter.SseEventBuilder build = buildEvent(message.getEventName(), message);
        Map<String, SseEmitter> appEmitters = getEmitters(userId, appkey);
        List<String> deadAppkeys = this.doPublish(userId, appEmitters, List.of(build));
        // 从全局缓存清理死连接
        removeDeadEmitters(userId, deadAppkeys);
        // 送达数 = 本次参与推送的连接数 - 失败数
        return appEmitters.size() - deadAppkeys.size();
    }

    /**
     * 全员广播.<br>
     * eventName 取自 {@link SseMessage#getEventName()}，为空时抛出异常。
     *
     * @param message 消息体（必须设置 eventName）
     */
    public void broadcast(@NotNull SseMessage message) {
        if (StringUtil.isNull(message.getEventName())) {
            throw new SystemErrorException("SSE 消息 eventName 不能为空");
        }
        doBroadcast(message.getEventName(), message);
    }

    /**
     * 发送心跳包（内部使用，不对外暴露）.<br>
     * 调度线程只负责提交任务，实际 IO 由 broadcastExecutor 异步执行，避免阻塞调度线程。
     */
    private void sendHeartbeats() {
        broadcastExecutor.submit(() -> {
            doBroadcast(EVENT_HEARTBEAT, null);
            logger.debug("发送全局心跳，当前在线用户数: {}", emitters.size());
        });
    }

    /**
     * 广播内部实现，供 broadcast 公开方法和心跳复用
     */
    private void doBroadcast(@NotNull String eventName, @Nullable SseMessage message) {
        SseEmitter.SseEventBuilder build = buildEvent(eventName, message);
        for (String userId : emitters.keySet()) {
            Map<String, SseEmitter> appEmitters = emitters.get(userId);
            if (appEmitters == null) {
                continue;
            }
            List<String> deadAppkeys = this.doPublish(userId, appEmitters, List.of(build));
            removeDeadEmitters(userId, deadAppkeys);
        }
    }

    /**
     * 构建 SSE 事件
     */
    private SseEmitter.SseEventBuilder buildEvent(@NotNull String eventName, @Nullable SseMessage message) {
        SseEmitter.SseEventBuilder builder = SseEmitter.event().name(eventName);
        if (message != null) {
            builder.data(JsonUtil.serialize(message));
            if (StringUtil.notNull(message.getRecordId())) {
                builder.id(message.getRecordId());
            } else if (StringUtil.notNull(message.getMessageId())) {
                builder.id(message.getMessageId());
            }
        } else {
            // SSE 规范要求至少有 data 字段，否则部分客户端不触发事件
            builder.data("");
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
     * 实际发送逻辑.<br>
     * 推送失败时仅记录日志并标记为死连接，不主动 complete，避免触发回调形成双重 complete。
     *
     * @return 返回无效链接对应的 appkey 列表
     */
    private List<String> doPublish(String userId, Map<String, SseEmitter> appEmitters, List<SseEmitter.SseEventBuilder> list) {
        List<String> deadAppkeys = new ArrayList<>();
        for (Map.Entry<String, SseEmitter> entry : appEmitters.entrySet()) {
            String appkey = entry.getKey();
            SseEmitter emitter = entry.getValue();
            try {
                logger.debug("推送 SSE 消息给用户[{}]客户端[{}]开始.", userId, appkey);
                for (SseEmitter.SseEventBuilder item : list) {
                    emitter.send(item);
                }
                logger.debug("推送 SSE 消息给用户[{}]客户端[{}]成功.", userId, appkey);
            } catch (Exception e) {
                // 推送失败说明连接已失效，标记为死连接，由 removeDeadEmitters 统一清理缓存
                // 不调用 completeWithError：连接已坏时调用会抛 IllegalStateException，且会触发 onError 再次 complete
                logger.warn("推送 SSE 消息给用户[{}]客户端[{}]失败，标记为死连接. error={}", userId, appkey, e.getMessage());
                deadAppkeys.add(appkey);
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

}
