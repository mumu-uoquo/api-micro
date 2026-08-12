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

import com.uoquo.platform.common.utils.UserUtils;
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
    /** 主索引：userId → appkey → tokenKey → SseEmitter，用于按用户/appkey推送 */
    private final Map<String, Map<String, Map<String, SseEmitter>>> emitters = new ConcurrentHashMap<>();
    /** token反向索引：tokenKey → 连接信息，用于按token精准推送（如踢人通知） */
    private final Map<String, ConnectionRef> tokenIndex = new ConcurrentHashMap<>();
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
        List<ConnectionRef> connections = new ArrayList<>();
        for (String userId : emitters.keySet()) {
            connections.addAll(collectEmitters(userId, null));
        }
        for (ConnectionRef connection : connections) {
            connection.emitter.complete();
        }
        emitters.clear();
        tokenIndex.clear();
        scheduler.shutdown();
        broadcastExecutor.shutdown();
    }

    /**
     * 订阅消息（默认10分钟超时）
     */
    public SseEmitter subscribe(@NotNull String userId, @NotNull String appkey, @NotNull String token) {
        SseEmitter emitter = new SseEmitter(10 * 60 * 1000L);
        return subscribe(userId, appkey, token, emitter);
    }

    /**
     * 订阅消息
     *
     * @param userId  用户ID
     * @param appkey  应用ID
     * @param token   当前会话 token，用于精准寻址（如踢人通知）；格式化后作为索引 key，兼容 token 刷新
     * @param emitter 外部传入的 SseEmitter（可自定义超时时长）
     */
    public SseEmitter subscribe(@NotNull String userId, @NotNull String appkey,
                                @NotNull String token, SseEmitter emitter) {
        if (StringUtil.isNull(userId) || StringUtil.isNull(appkey) || StringUtil.isNull(token)) {
            return null;
        }
        String tokenKey = UserUtils.formatToken(token);
        if (StringUtil.isNull(tokenKey)) {
            return null;
        }
        ConnectionRef connection = new ConnectionRef(userId, appkey, tokenKey, emitter);
        // 1. 注册相关事件（onTimeout 触发后 Spring 会自动调用 onCompletion，统一由 onCompletion 清理连接）
        String traceId = CurrentUser.getTraceId();
        emitter.onCompletion(() -> {
            try {
                MDC.put("requestId", traceId);
                logger.info("用户[{}]客户端[{}]会话[{}]的 SSE 连接已关闭", userId, appkey, tokenKey);
                removeEmitter(connection);
            } catch (Exception e) {
                logger.debug("用户[{}]客户端[{}]会话[{}]的 SSE 链接清理出错", userId, appkey, tokenKey, e);
            } finally {
                MDC.remove("requestId");
            }
        });
        emitter.onTimeout(() -> {
            logger.warn("用户[{}]客户端[{}]会话[{}]的 SSE 连接已超时", userId, appkey, tokenKey);
            emitter.complete();
        });
        emitter.onError(e -> {
            logger.error("用户[{}]客户端[{}]会话[{}]的 SSE 连接已出错", userId, appkey, tokenKey, e);
            emitter.completeWithError(e);
        });

        // 2. 按 userId → appkey → tokenKey 保存连接；同一 appkey 下不同 token 的连接可同时存在
        Map<String, Map<String, SseEmitter>> appEmitters = emitters.computeIfAbsent(
                userId, k -> new ConcurrentHashMap<>());
        Map<String, SseEmitter> tokenEmitters = appEmitters.computeIfAbsent(
                appkey, k -> new ConcurrentHashMap<>());
        SseEmitter replacedEmitter = tokenEmitters.put(tokenKey, emitter);
        ConnectionRef replacedConnection = tokenIndex.put(tokenKey, connection);

        // 同一 token 重连时只替换该 token 的旧连接，不影响同一 appkey 下的其他 token
        if (replacedConnection != null && replacedConnection.emitter != emitter) {
            removeEmitter(replacedConnection);
            replacedConnection.emitter.complete();
        } else if (replacedEmitter != null && replacedEmitter != emitter) {
            ConnectionRef replaced = new ConnectionRef(userId, appkey, tokenKey, replacedEmitter);
            removeEmitter(replaced);
            replacedEmitter.complete();
        }

        // 3. 先放入缓存，再发送初始事件
        try {
            emitter.send(SseEmitter.event().name("CONNECTED").data(JsonUtil.serialize("连接已建立")));
            logger.info("用户[{}]客户端[{}]会话[{}]的 SSE 连接建立成功", userId, appkey, tokenKey);
        } catch (IOException e) {
            logger.error("用户[{}]客户端[{}]会话[{}]的 SSE 推送初始事件出错", userId, appkey, tokenKey, e);
            removeEmitter(connection);
            emitter.completeWithError(e);
        }
        return emitter;
    }

    /**
     * 按 token 精准推送，用于踢人等需要定位特定会话的场景.<br>
     * 使用格式化后的 tokenKey 匹配，兼容会话 token 刷新后仍能找到对应连接。
     *
     * @param token   被踢会话的 token（完整或截断均可）
     * @param message 消息体（必须设置 eventName）
     * @return true 表示推送成功，false 表示目标连接不存在或已断开
     */
    public boolean publishByToken(@NotNull String token, @NotNull SseMessage message) {
        if (StringUtil.isNull(message.getEventName())) {
            throw new SystemErrorException("SSE 消息 eventName 不能为空");
        }
        String tokenKey = UserUtils.formatToken(token);
        if (StringUtil.isNull(tokenKey)) {
            logger.debug("token 为空，无法按 token 精准推送消息：{}", JsonUtil.serialize(message));
            return false;
        }
        ConnectionRef connection = tokenIndex.get(tokenKey);
        if (connection == null) {
            logger.debug("token[{}]无对应的 SSE 连接，消息无需推送（连接已离线）：{}", tokenKey, JsonUtil.serialize(message));
            return false;
        }
        SseEmitter.SseEventBuilder build = buildEvent(message.getEventName(), message);
        try {
            connection.emitter.send(build);
            logger.debug("按 token[{}]精准推送 SSE 消息成功.", tokenKey);
            return true;
        } catch (Exception e) {
            logger.warn("按 token[{}]精准推送 SSE 消息失败，连接已失效. error={}", tokenKey, e.getMessage());
            removeEmitter(connection);
            return false;
        }
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
        List<ConnectionRef> connections = getEmitters(userId, appkey);
        List<ConnectionRef> deadConnections = this.doPublish(connections, List.of(build));
        removeDeadEmitters(deadConnections);
        // 送达数 = 本次参与推送的连接数 - 失败数
        return connections.size() - deadConnections.size();
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
            int connectionCount = doBroadcast(EVENT_HEARTBEAT, null);
            logger.debug("发送全局心跳，当前在线用户数: {}，连接数: {}", emitters.size(), connectionCount);
        });
    }

    /**
     * 广播内部实现，供 broadcast 公开方法和心跳复用
     *
     * @return 本次参与广播的有效连接数
     */
    private int doBroadcast(@NotNull String eventName, @Nullable SseMessage message) {
        SseEmitter.SseEventBuilder build = buildEvent(eventName, message);
        int successCount = 0;
        for (String userId : emitters.keySet()) {
            List<ConnectionRef> connections = collectEmitters(userId, null);
            if (connections.isEmpty()) {
                continue;
            }
            List<ConnectionRef> deadConnections = this.doPublish(connections, List.of(build));
            removeDeadEmitters(deadConnections);
            successCount += connections.size() - deadConnections.size();
        }
        return successCount;
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
     * 获取用户的 SSE 连接。
     *
     * @param userId 用户ID
     * @param appkey 应用ID（为空时返回该用户所有连接）
     */
    private List<ConnectionRef> getEmitters(String userId, String appkey) {
        List<ConnectionRef> connections = collectEmitters(userId, appkey);
        if (!connections.isEmpty()) {
            return connections;
        }
        if (StringUtil.isNull(appkey)) {
            logger.debug("用户[{}]无 SSE 链接，无法推送消息.", userId);
            throw new SystemErrorException("用户[%s]无 SSE 链接，无法推送消息", userId);
        }
        logger.debug("用户[{}]无客户端[{}]的 SSE 链接，无法推送消息.", userId, appkey);
        throw new SystemErrorException("用户[%s]无[%s]SSE 链接，无法推送消息", userId, appkey);
    }

    /**
     * 按条件收集连接快照，避免推送期间嵌套索引发生并发变化。
     */
    private List<ConnectionRef> collectEmitters(String userId, String appkey) {
        List<ConnectionRef> connections = new ArrayList<>();
        Map<String, Map<String, SseEmitter>> appEmitters = emitters.get(userId);
        if (appEmitters == null || appEmitters.isEmpty()) {
            return connections;
        }
        if (StringUtil.notNull(appkey)) {
            addConnections(connections, userId, appkey, appEmitters.get(appkey));
            return connections;
        }
        for (Map.Entry<String, Map<String, SseEmitter>> appEntry : appEmitters.entrySet()) {
            addConnections(connections, userId, appEntry.getKey(), appEntry.getValue());
        }
        return connections;
    }

    private void addConnections(List<ConnectionRef> connections, String userId, String appkey,
                                Map<String, SseEmitter> tokenEmitters) {
        if (tokenEmitters == null) {
            return;
        }
        for (Map.Entry<String, SseEmitter> tokenEntry : tokenEmitters.entrySet()) {
            connections.add(new ConnectionRef(userId, appkey, tokenEntry.getKey(), tokenEntry.getValue()));
        }
    }

    /**
     * 实际发送逻辑.<br>
     * 推送失败时仅记录日志并标记为死连接，不主动 complete，避免触发回调形成双重 complete。
     *
     * @return 返回无效连接列表
     */
    private List<ConnectionRef> doPublish(List<ConnectionRef> connections,
                                          List<SseEmitter.SseEventBuilder> events) {
        List<ConnectionRef> deadConnections = new ArrayList<>();
        for (ConnectionRef connection : connections) {
            try {
                logger.debug("推送 SSE 消息给用户[{}]客户端[{}]会话[{}]开始.",
                        connection.userId, connection.appkey, connection.tokenKey);
                for (SseEmitter.SseEventBuilder event : events) {
                    connection.emitter.send(event);
                }
                logger.debug("推送 SSE 消息给用户[{}]客户端[{}]会话[{}]成功.",
                        connection.userId, connection.appkey, connection.tokenKey);
            } catch (Exception e) {
                // 推送失败说明连接已失效，标记为死连接，由 removeDeadEmitters 统一清理缓存
                // 不调用 completeWithError：连接已坏时调用会抛 IllegalStateException，且会触发 onError 再次 complete
                logger.warn("推送 SSE 消息给用户[{}]客户端[{}]会话[{}]失败，标记为死连接. error={}",
                        connection.userId, connection.appkey, connection.tokenKey, e.getMessage());
                deadConnections.add(connection);
            }
        }
        return deadConnections;
    }

    /**
     * 移除已关闭的 SSE 连接，同步清理 tokenIndex。
     */
    private void removeDeadEmitters(List<ConnectionRef> deadConnections) {
        for (ConnectionRef connection : deadConnections) {
            removeEmitter(connection);
        }
    }

    /**
     * 精确移除单个连接；只有 emitter 仍是索引中的同一实例时才删除，
     * 避免旧连接的回调误删同 token 的新连接。
     */
    private void removeEmitter(ConnectionRef connection) {
        ConnectionRef indexed = tokenIndex.get(connection.tokenKey);
        if (indexed != null && indexed.emitter == connection.emitter) {
            tokenIndex.remove(connection.tokenKey, indexed);
        }

        Map<String, Map<String, SseEmitter>> appEmitters = emitters.get(connection.userId);
        if (appEmitters == null) {
            return;
        }
        Map<String, SseEmitter> tokenEmitters = appEmitters.get(connection.appkey);
        if (tokenEmitters == null) {
            return;
        }
        tokenEmitters.remove(connection.tokenKey, connection.emitter);
        if (tokenEmitters.isEmpty()) {
            appEmitters.remove(connection.appkey, tokenEmitters);
        }
        if (appEmitters.isEmpty()) {
            emitters.remove(connection.userId, appEmitters);
        }
    }

    /** 单条 SSE 连接的索引信息。 */
    private static final class ConnectionRef {
        private final String userId;
        private final String appkey;
        private final String tokenKey;
        private final SseEmitter emitter;

        private ConnectionRef(String userId, String appkey, String tokenKey, SseEmitter emitter) {
            this.userId = userId;
            this.appkey = appkey;
            this.tokenKey = tokenKey;
            this.emitter = emitter;
        }
    }

}
