package com.uoquo.platform.message.controller;

import com.uoquo.platform.common.PlatformCacheKey;
import com.uoquo.platform.message.service.SseEmitterService;
import com.uoquo.utils.CurrentUser;
import com.uoquo.utils.IDGenerator;
import com.uoquo.utils.StringUtil;
import com.uoquo.web.ReturnData;
import com.uoquo.annotation.web.IgnoreAuth;
import com.uoquo.web.exception.ForbiddenException;
import com.uoquo.web.exception.ParamEmtpyException;
import com.uoquo.utils.spring.RedisUtil;
import com.uoquo.web.utils.WebUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * 服务器简单推送事件（SSE）
 * @author xuhz
 */
@Tag(name = "sse", description = "消息订阅")
@RestController
@RequestMapping("/v1/message/sse")
public class ServerSentEventsController {
    final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private SseEmitterService sseEmitterService;

    /**
     * 方法1：step1-获取验证码
     */
    @Operation(summary = "用户消息：获取验证码", operationId = "getEndpointCode")
    @PostMapping("/endpoint")
    public ReturnData<String> getEndpointCode(HttpServletRequest request) {
        Map<String, String> endpoint = new HashMap<>();
        endpoint.put("userId", CurrentUser.getInfo().getUserId());
        endpoint.put("appkey", CurrentUser.getAppkey());
        endpoint.put("token", CurrentUser.getToken());
        String lastEventId = WebUtil.getHeader("Last-Event-ID", request);
        if (StringUtil.notNull(lastEventId)) {
            endpoint.put("lastEventId", lastEventId);
        }
        String code = IDGenerator.getNextULID();
        RedisUtil.put(PlatformCacheKey.SSE_CODE_PREFIX + code, endpoint, 60);
        return new ReturnData<>(code);
    }

    /**
     * 方法1：step2-标准SSE请求（需配合endpoint接口使用）
     */
    @IgnoreAuth(all = true)
    @Operation(summary = "用户消息：订阅消息", hidden = true)
    @GetMapping(value = "/subscribe/{code}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeByCode(@PathVariable String code, HttpServletResponse response) {
        Map<String, String> endpoint = RedisUtil.get(PlatformCacheKey.SSE_CODE_PREFIX + code, Map.class);
        if (endpoint == null) {
            throw new ForbiddenException("无效的订阅码");
        }
        RedisUtil.remove(PlatformCacheKey.SSE_CODE_PREFIX + code);
        String userId = endpoint.get("userId");
        String appkey = endpoint.get("appkey");
        if (StringUtil.isNull(userId) || StringUtil.isNull(appkey)) {
            throw new ParamEmtpyException("用户ID或APPKEY不能为空");
        }

        SseEmitter emitter = sseEmitterService.subscribe(userId, appkey, endpoint.getOrDefault("token", ""));
//        // 使用nginx做反向代理时需要将proxy_buffering关闭
//        // 或者加上响应头部x-accel-buffering，这样nginx就不会给后端响应数据加buffer
//        response.addHeader("x-accel-buffering", "no");
        return emitter;
    }

    /**
     * 方法2：标准SSE请求（需要通过URL传参的方式传入token、appid、nonce三个参数）
     */
    @IgnoreAuth(timestamp = true, params = true)
    @Operation(summary = "用户消息：订阅消息", hidden = true)
    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeByToken(HttpServletRequest request, HttpServletResponse response) {
        SseEmitter emitter = new SseEmitter(10 * 60 * 1000L);
        emitter = sseEmitterService.subscribe(CurrentUser.getInfo().getUserId(), CurrentUser.getAppkey(), CurrentUser.getToken(), emitter);
//        // 使用nginx做反向代理时需要将proxy_buffering关闭
//        // 或者加上响应头部x-accel-buffering，这样nginx就不会给后端响应数据加buffer
//        response.addHeader("x-accel-buffering", "no");
        return emitter;
    }

    /**
     * 方法3：自定义SSE请求（可实现登录拦截，请求体传参等）
     */
    @IgnoreAuth(timestamp = true)
    @Operation(summary = "用户消息：订阅消息", hidden = true)
    @PostMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeByPost(HttpServletRequest request, HttpServletResponse response) {
        SseEmitter emitter = new SseEmitter(10 * 60 * 1000L);
        emitter = sseEmitterService.subscribe(CurrentUser.getInfo().getUserId(), CurrentUser.getAppkey(), CurrentUser.getToken(), emitter);
//        // 使用nginx做反向代理时需要将proxy_buffering关闭
//        // 或者加上响应头部x-accel-buffering，这样nginx就不会给后端响应数据加buffer
//        response.addHeader("x-accel-buffering", "no");
        return emitter;
    }
}
