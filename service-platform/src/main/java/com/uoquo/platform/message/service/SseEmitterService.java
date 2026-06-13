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
    /** 主索引：userId → appkey → SseEmitter，用于按用户/appkey推送 */
    private final Map<String, Map<String, SseEmitter>> emitters = new ConcurrentHashMap<>();
    /** token反向索引：tokenKey(前16字符) → SseEmitter，用于按token精准推送（如踢人通知） */
    private final Map<String, SseEmitter> tokenIndex = new ConcurrentHashMap<>();
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
    public SseEmitter subscribe(@NotNull String userId, @NotNull String appkey, @NotNull String token) {
        SseEmitter emitter = new SseEmitter(10 * 60 * 1000L);
        return subscribe(userId, appkey, token, emitter);
    }

    /**
     * 订阅消息
     *
     * @param userId  用户ID
     * @param appkey  应用ID
     * @param token   当前会话 token，用于精准寻址（如踢人通知）；使用前26字符作为索引 key，兼容 token 刷新
     * @param emitter 外部传入的 SseEmitter（可自定义超时时长）
     */
    public SseEmitter subscribe(@NotNull String userId, @NotNull String appkey,
                                @NotNull String token, SseEmitter emitter) {
        if (StringUtil.isNull(userId) || StringUtil.isNull(appkey) || StringUtil.isNull(token)) {
            return null;
        }
        String tokenKey = UserUtils.formatToken(token);
        // 1. 注册相关事件（onTimeout 触发后 Spring 会自动调用 onCompletion，统一由 onCompletion 清理连接）
        String traceId = CurrentUser.getTraceId();
        emitter.onCompletion(() -> {
            try {
                MDC.put("requestId", traceId);
                logger.info("用户[{}]客户端[{}]的 SSE 连接已关闭", userId, appkey);
                removeEmitter(userId, appkey, tokenKey);
            } catch (Exception e) {
                logger.debug("用户[{}]客户端[{}]的 SSE 链接清理出错", userId, appkey, e);
            } finally {
                MDC.remove("requestId");
            }
        });
        emitter.onTimeout(() -> {
            logger.warn("用户[{}]客户端[{}]的 SSE 连接已超时", userId, appkey);
            emitter.complete();
        });
        emitter.onError(e -> {
            logger.error("用户[{}]客户端[{}]的 SSE 连接已出错", userId, appkey, e);
            emitter.completeWithError(e);
        });
        // 2. 替换主索引中 appkey 对应的连接为新连接，但不主动断开旧连接
        //    旧连接保留在 tokenIndex 中，等待 kickOut 消息推送后由客户端自行断开，
        //    或由 onCompletion / onTimeout / onError 回调自然清理
        Map<String, SseEmitter> appEmitters = emitters.computeIfAbsent(userId, k -> new ConcurrentHashMap<>());
        if (appEmitters.containsKey(appkey)) {
            logger.debug("用户[{}]客户端[{}]已有旧 SSE 连接，替换为新连接，旧连接保留至 kickOut 通知后断开", userId, appkey);
        }
        // 3. 先放入缓存，再发送初始事件
        appEmitters.put(appkey, emitter);
        if (StringUtil.notNull(tokenKey)) {
            tokenIndex.put(tokenKey, emitter);
        }
        try {
            emitter.send(SseEmitter.event().name("CONNECTED").data(JsonUtil.serialize("连接已建立")));
            logger.info("用户[{}]客户端[{}]的 SSE 连接建立成功", userId, appkey);
        } catch (IOException e) {
            logger.error("用户[{}]客户端[{}]的 SSE 推送初始事件出错", userId, appkey, e);
            appEmitters.remove(appkey);
            if (StringUtil.notNull(tokenKey)) {
                tokenIndex.remove(tokenKey);
            }
            emitter.completeWithError(e);
        }
        return emitter;
    }

    /**
     * 按 token 精准推送，用于踢人等需要定位特定会话的场景.<br>
     * 使用 token 前26字符匹配，兼容会话 token 刷新后仍能找到对应连接。
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
            logger.debug("token 为空，无法按 token 精准推送");
            return false;
        }
        SseEmitter emitter = tokenIndex.get(tokenKey);
        if (emitter == null) {
            logger.debug("token[{}]无对应的 SSE 连接，消息无需推送（连接已离线）", tokenKey);
            return false;
        }
        SseEmitter.SseEventBuilder build = buildEvent(message.getEventName(), message);
        try {
            emitter.send(build);
            logger.debug("按 token[{}]精准推送 SSE 消息成功.", tokenKey);
            return true;
        } catch (Exception e) {
            logger.warn("按 token[{}]精准推送 SSE 消息失败，连接已失效. error={}", tokenKey, e.getMessage());
            tokenIndex.remove(tokenKey);
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
     * 移除已关闭的SSE连接，同步清理 tokenIndex
     * @return 剩余的连接数量
     */
    private int removeDeadEmitters(String userId, List<String> deadAppkeys) {
        Map<String, SseEmitter> appEmitters = emitters.getOrDefault(userId, new ConcurrentHashMap<>());
        for (String appkey : deadAppkeys) {
            SseEmitter dead = appEmitters.remove(appkey);
            if (dead != null) {
                // 用 == 比较引用：目的是找到 tokenIndex 中与 dead 完全相同的实例并移除
                // SseEmitter 未重写 equals，但此处语义就是"同一个对象"，== 是正确选择
                tokenIndex.entrySet().removeIf(e -> e.getValue() == dead);
            }
        }
        if (appEmitters.isEmpty()) {
            emitters.remove(userId);
        }
        return appEmitters.size();
    }

    /**
     * 移除单个连接（onCompletion 回调使用），同步清理 tokenIndex.<br>
     * tokenKey 为空时跳过 tokenIndex 清理。
     */
    private void removeEmitter(String userId, String appkey, String tokenKey) {
        if (StringUtil.notNull(tokenKey)) {
            tokenIndex.remove(tokenKey);
        }
        Map<String, SseEmitter> appEmitters = emitters.getOrDefault(userId, new ConcurrentHashMap<>());
        appEmitters.remove(appkey);
        if (appEmitters.isEmpty()) {
            emitters.remove(userId);
        }
    }

}
