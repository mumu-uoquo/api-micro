package com.uoquo.platform.message.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

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
    /** 主索引：userId → tokenKey → 连接信息，用于按用户/appkey推送 */
    private final Map<String, Map<String, ConnectionRef>> emitters = new ConcurrentHashMap<>();
    /** token反向索引：tokenKey → 连接信息，用于按token精准推送（如踢人通知） */
    private final Map<String, ConnectionRef> tokenIndex = new ConcurrentHashMap<>();
    /** 调度线程：仅负责触发心跳任务，不执行实际 IO */
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    /**
     * 推送线程池：执行实际的 send IO（心跳、站内消息），避免慢客户端阻塞调用线程（如 Kafka 消费线程）。<br>
     * 有界队列 + CallerRunsPolicy：队列满时由提交线程自己执行，形成背压，不丢消息也不无限创建线程。
     */
    private final ThreadPoolExecutor broadcastExecutor = createPushExecutor();

    private static ThreadPoolExecutor createPushExecutor() {
        int size = Math.max(4, Runtime.getRuntime().availableProcessors());
        AtomicInteger seq = new AtomicInteger();
        ThreadPoolExecutor executor = new ThreadPoolExecutor(size, size, 60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(2048),
                r -> {
                    Thread t = new Thread(r, "sse-push-" + seq.incrementAndGet());
                    t.setDaemon(true);
                    return t;
                },
                new ThreadPoolExecutor.CallerRunsPolicy());
        executor.allowCoreThreadTimeOut(true);
        return executor;
    }

    /** 内置心跳事件名，不对外暴露 */
    private static final String EVENT_HEARTBEAT = "HEARTBEAT";

    @PostConstruct
    public void init() {
        // 全局心跳任务，每30秒执行一次
        scheduler.scheduleAtFixedRate(this::sendHeartbeats, 30, 30, TimeUnit.SECONDS);
    }

    @PreDestroy
    public void cleanup() {
        List<ConnectionRef> connections = new ArrayList<>(tokenIndex.values());
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

        // 2. 按 userId → tokenKey 保存连接；appkey 作为连接属性用于定向推送过滤
        Map<String, ConnectionRef> userEmitters = emitters.computeIfAbsent(userId, k -> new ConcurrentHashMap<>());
        ConnectionRef replacedForUser = userEmitters.put(tokenKey, connection);
        ConnectionRef replacedByToken = tokenIndex.put(tokenKey, connection);

        // 同一 token 重连时只保留最后一个连接，不影响该用户的其他 token
        if (replacedByToken != null && replacedByToken.emitter != emitter) {
            removeEmitter(replacedByToken);
            replacedByToken.emitter.complete();
        }
        if (replacedForUser != null && replacedForUser != replacedByToken && replacedForUser.emitter != emitter) {
            removeEmitter(replacedForUser);
            replacedForUser.emitter.complete();
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
     * 定向推送消息（单个用户）.<br>
     * eventName 取自 {@link SseMessage#getEventName()}，为空时抛出异常。<br>
     * 多节点部署时，用户的连接只存在于其中一个节点，其他节点未命中属于正常情况：直接返回 0，不抛异常。
     * 实际的网络写入由推送线程池异步完成，调用线程不会被慢客户端阻塞。
     *
     * @param userId  接收用户 ID
     * @param appkey  目标客户端（为空时推送给该用户所有在线客户端）
     * @param message 消息体（必须设置 eventName）
     * @return 本节点命中的连接数（已提交推送，不代表已送达）；0 表示本节点没有该用户的连接
     */
    public int publish(@NotNull String userId, @Nullable String appkey, @NotNull SseMessage message) {
        if (StringUtil.isNull(userId)) {
            return 0;
        }
        checkEventName(message);
        List<ConnectionRef> connections = collectEmitters(userId, appkey);
        if (connections.isEmpty()) {
            return 0;
        }
        List<Target> targets = List.of(new Target(connections, userId, message.getRecordId()));
        broadcastExecutor.execute(() -> pushToTargets(message, targets));
        return connections.size();
    }

    /**
     * 批量定向推送（一条消息推送给多个用户）.<br>
     * 接收人取自 {@link SseMessage#getReceiverRecords()}（receiverId → 接收记录ID），目标客户端取自
     * {@link SseMessage#getTargetAppKey()}。本节点没有连接的用户直接跳过；投递给每个客户端的消息只包含该用户自己的
     * receiverId、recordId，不会带上其他接收人的信息。
     *
     * @param message 消息模板（必须设置 eventName 和 receiverRecords）
     * @return 本节点命中的连接数（已提交推送，不代表已送达）；0 表示本节点没有任何接收人的连接
     */
    public int publishBatch(@NotNull SseMessage message) {
        checkEventName(message);
        Map<String, String> receiverRecords = message.getReceiverRecords();
        if (receiverRecords == null || receiverRecords.isEmpty()) {
            return 0;
        }
        String appkey = message.getTargetAppKey();
        List<Target> targets = new ArrayList<>();
        int matched = 0;
        // 只做内存索引查找，开销与接收人数成正比且很小；序列化、IO 都放到线程池里
        for (Map.Entry<String, String> entry : receiverRecords.entrySet()) {
            List<ConnectionRef> connections = collectEmitters(entry.getKey(), appkey);
            if (!connections.isEmpty()) {
                targets.add(new Target(connections, entry.getKey(), entry.getValue()));
                matched += connections.size();
            }
        }
        if (!targets.isEmpty()) {
            broadcastExecutor.execute(() -> pushToTargets(message, targets));
        }
        return matched;
    }

    /**
     * 在推送线程中，逐个目标生成消息并发送，失败的连接统一清理。
     */
    private void pushToTargets(SseMessage message, List<Target> targets) {
        try {
            for (Target target : targets) {
                SseMessage personal = message.forReceiver(target.userId, target.recordId);
                SseEmitter.SseEventBuilder build = buildEvent(message.getEventName(), personal);
                removeDeadEmitters(doPublish(target.connections, List.of(build)));
            }
        } catch (Exception e) {
            logger.warn("推送 SSE 消息[{}]出错：{}", message.getMessageId(), e.getMessage(), e);
        }
    }

    private void checkEventName(SseMessage message) {
        if (StringUtil.isNull(message.getEventName())) {
            throw new SystemErrorException("SSE 消息 eventName 不能为空");
        }
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
        for (Map<String, ConnectionRef> userEmitters : emitters.values()) {
            List<ConnectionRef> connections = new ArrayList<>(userEmitters.values());
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
     * 按条件收集连接快照。单个用户的活跃连接数通常很少，按 appkey 过滤无需额外索引。
     */
    private List<ConnectionRef> collectEmitters(String userId, String appkey) {
        List<ConnectionRef> connections = new ArrayList<>();
        Map<String, ConnectionRef> userEmitters = emitters.get(userId);
        if (userEmitters == null || userEmitters.isEmpty()) {
            return connections;
        }
        for (ConnectionRef connection : userEmitters.values()) {
            if (StringUtil.isNull(appkey) || appkey.equals(connection.appkey)) {
                connections.add(connection);
            }
        }
        return connections;
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
        tokenIndex.remove(connection.tokenKey, connection);

        Map<String, ConnectionRef> userEmitters = emitters.get(connection.userId);
        if (userEmitters == null) {
            return;
        }
        userEmitters.remove(connection.tokenKey, connection);
        if (userEmitters.isEmpty()) {
            emitters.remove(connection.userId, userEmitters);
        }
    }

    /** 一次推送的目标：某个用户在本节点的连接，以及该用户对应的接收记录ID。 */
    private static final class Target {
        private final List<ConnectionRef> connections;
        private final String userId;
        private final String recordId;

        private Target(List<ConnectionRef> connections, String userId, String recordId) {
            this.connections = connections;
            this.userId = userId;
            this.recordId = recordId;
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
