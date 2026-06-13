/**
 * Copyright (c) 2025, www.uoquo.com All Rights Reserved.
 * 注意：本内容仅限于内部传阅，禁止外泄
 */
package com.uoquo.gateway.utils;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;

import com.uoquo.utils.CurrentUser;
import com.uoquo.utils.IDGenerator;
import com.uoquo.utils.StringUtil;
import com.uoquo.utils.crypto.MD5;
import com.uoquo.utils.spring.RedisUtil;
import com.uoquo.web.BaseCacheKey;
import com.uoquo.web.exception.ParamErrorException;

import reactor.core.publisher.Mono;


public class GatewayUtil {
    private static final Logger log = LoggerFactory.getLogger(GatewayUtil.class);

    /**
     * 获取客户端的IP
     */
    public static String getClientIp(ServerHttpRequest request) {
        // 1.从请求头中获取
        String clientIp = getClientIp(request.getHeaders());
        // 2.获取代理地址
        if (StringUtil.isNull(clientIp) || "unknown".equalsIgnoreCase(clientIp)) {
            if (request.getRemoteAddress() != null) {
                clientIp = request.getRemoteAddress().getAddress().getHostAddress();
            }
        }
        // 返回内容
        if (StringUtil.isNull(clientIp) || "unknown".equalsIgnoreCase(clientIp)) {
            return null;
        } else {
            return clientIp;
        }
    }

    public static String getClientIp(HttpHeaders headers) {
        String clientIp = headers.getFirst("X-Forwarded-For");
        if (StringUtil.notNull(clientIp) && !"unknown".equalsIgnoreCase(clientIp)) {
            String[] adds = clientIp.split(",");
            clientIp = adds[0].trim();
        }
        if (StringUtil.isNull(clientIp) || "unknown".equalsIgnoreCase(clientIp)) {
            clientIp = headers.getFirst("Proxy-Client-IP");
        }
        if (StringUtil.isNull(clientIp) || "unknown".equalsIgnoreCase(clientIp)) {
            clientIp = headers.getFirst("WL-Proxy-Client-IP");
        }
        if (StringUtil.isNull(clientIp) || "unknown".equalsIgnoreCase(clientIp)) {
            clientIp = headers.getFirst("X-Real-IP");
        }
        return clientIp;
    }

    /**
     * 获取请求头参数.
     */
    public static String getHeader(HttpHeaders header, Map<String, String> queryParams, String key) throws ParamErrorException {
        List<String> list = header.get(key);
        if ((list == null) || list.isEmpty()) {
            // 从请求头中获取不到参数，则从URL参数中获取
            String value = queryParams.get(key);
            log.debug("header has no param [{}], query params has value [{}].", key, value);
            return value;
        } else if (list.size() == 1) {
            return list.get(0);
        } else {
            log.debug("header has too many param [{}={}].", key, list);
            throw new ParamErrorException(String.format("请求头中有多个参数[%s]", key));
        }
    }

    /**
     * 获取请求头参数.
     */
    public static String getHeaderFirst(HttpHeaders header, Map<String, String> queryParams, String key) {
        List<String> list = header.get(key);
        if ((list == null) || list.isEmpty()) {
            // 从请求头中获取不到参数，则从URL参数中获取
            String value = queryParams.get(key);
            log.debug("header has no param [{}], query params has value [{}].", key, value);
            return value;
        } else {
            return list.get(0);
        }
    }

    /**
     * 全局通信签名.<br>
     * 备注：主要用于网关到服务，服务到服务之间的参数签名.
     * @param signature 参数签名数据
     * @param secret    全局密钥
     */
    public static String sign(String signature, String secret) {
        try {
            return MD5.encrypt(signature + secret);
        } catch (Exception e) {
            log.warn("calc global signature error. {}", signature, e);
            return null;
        }
    }

    /**
     * 缓存用户信息到exchange（从而保证当前会话各个filter都可用）.
     */
    public static void putInfo2Attributes(Map<String, Object> attributes) {
        attributes.put("current-user",            CurrentUser.getInfo());
        if (StringUtil.notNull(CurrentUser.getToken())) {
            attributes.put(CurrentUser.TOKEN,         CurrentUser.getToken());
        }
        if (StringUtil.notNull(CurrentUser.getNonce())) {
            attributes.put(CurrentUser.NONCE,         CurrentUser.getNonce());
        }
        if (StringUtil.notNull(CurrentUser.getAppkey())) {
            attributes.put(CurrentUser.APPID,         CurrentUser.getAppkey());
        }
        if (StringUtil.notNull(CurrentUser.getAppVersion())) {
            attributes.put(CurrentUser.APP_VERSION,   CurrentUser.getAppVersion());
        }
        if (StringUtil.notNull(CurrentUser.getDeviceId())) {
            attributes.put(CurrentUser.DEVICE_ID,     CurrentUser.getDeviceId());
        }
        if (StringUtil.notNull(CurrentUser.getLanguage())) {
            attributes.put(CurrentUser.USER_LANGUAGE, CurrentUser.getLanguage());
        }
        if (StringUtil.notNull(CurrentUser.getClientIp())) {
            attributes.put(CurrentUser.CLIENT_IP,     CurrentUser.getClientIp());
        }
        if (StringUtil.notNull(CurrentUser.getTraceId())) {
            attributes.put(CurrentUser.TRACE_ID,      CurrentUser.getTraceId());
        }
    }

    /**
     * 解析AuthorizeFilter放入的CurrentUser信息
     */
    public static void parseInfo4Attributes(Map<String, Object> attributes) {
        CurrentUser.setToken((String)attributes.get(CurrentUser.TOKEN));
        CurrentUser.setNonce((String)attributes.get(CurrentUser.NONCE));
        CurrentUser.setAppkey((String)attributes.get(CurrentUser.APPID));
        CurrentUser.setAppVersion((String)attributes.get(CurrentUser.APP_VERSION));
        CurrentUser.setDeviceId((String)attributes.get(CurrentUser.DEVICE_ID));
        CurrentUser.setLanguage((String)attributes.get(CurrentUser.USER_LANGUAGE));
        CurrentUser.setClientIp((String)attributes.get(CurrentUser.CLIENT_IP));
        CurrentUser.setTraceId((String)attributes.get(CurrentUser.TRACE_ID));
        CurrentUser.UserInfo user = (CurrentUser.UserInfo)attributes.get("current-user");
        if (user != null) {
            CurrentUser.setInfo(user);
        }
        if (StringUtil.notNull(CurrentUser.getTraceId())) {
            MDC.put("requestId", CurrentUser.getTraceId());
        }
    }

    /**
     * 解析请求参数中的CurrentUser信息
     */
    public static void parseInfo4Request(ServerWebExchange exchange) {
        CurrentUser.clear();
        HttpHeaders header = exchange.getRequest().getHeaders();
        Map<String, String> queryParams = exchange.getRequest().getQueryParams().toSingleValueMap();
        // 请求ID（提前处理，方便日志打印，注：因为是内部ID，不再依赖前端传递）
        CurrentUser.setClientIp(GatewayUtil.getClientIp(exchange.getRequest()));
        CurrentUser.setTraceId(IDGenerator.getNextULID());
        MDC.put("requestId", CurrentUser.getTraceId());
        // 数据填充
        String token = GatewayUtil.getHeader(header, queryParams, CurrentUser.TOKEN);
        String appid = GatewayUtil.getHeader(header, queryParams, CurrentUser.APPID);
        String nonce = GatewayUtil.getHeader(header, queryParams, CurrentUser.NONCE);
        String udid  = GatewayUtil.getHeader(header, queryParams, CurrentUser.DEVICE_ID);
        CurrentUser.setToken(token);
        CurrentUser.setAppkey(appid);
        CurrentUser.setNonce(nonce);
        CurrentUser.setDeviceId(udid);
        // appinfo
        CurrentUser.AppInfo appInfo = RedisUtil.getLocalCache(BaseCacheKey.APPKEY_INFO_PREFIX + appid, CurrentUser.AppInfo.class);
        if (appInfo != null) {
            if (StringUtil.notNull(appInfo.getInstituteId())) {
                // 默认使用appid对应的机构ID
                CurrentUser.getInfo().setInstituteId(appInfo.getInstituteId());
            }
            if (StringUtil.notNull(appInfo.getType())) {
                CurrentUser.setAppType(appInfo.getType());
            }
        }
        // 用户信息
        CurrentUser.UserInfo user = CurrentUser.getInfo();
        if (StringUtil.notNull(token)) {
            CurrentUser.UserInfo temp = RedisUtil.get(BaseCacheKey.USER_INFO_PREFIX + token, CurrentUser.UserInfo.class);
            if (temp != null) {
                user = temp;
            }
        }
        // 放入当前线程
        CurrentUser.setInfo(user);
    }

    /**
     * 判断是否是长连接请求（SSE 或 WebSocket）.<br>
     * SSE 请求携带 {@code Accept: text/event-stream}，WebSocket 升级请求携带 {@code Upgrade: websocket}。<br>
     * 长连接请求不应设置响应超时，也不应缓冲响应体。
     */
    public static boolean isLongLivedRequest(ServerHttpRequest request) {
        // 1. Accept: text/event-stream → SSE（协议标准，优先判断）
        String accept = request.getHeaders().getFirst(HttpHeaders.ACCEPT);
        if (accept != null && accept.toLowerCase().contains(MediaType.TEXT_EVENT_STREAM_VALUE)) {
            return true;
        }
        // 2. Upgrade: websocket → WebSocket 升级请求
        String upgrade = request.getHeaders().getFirst(HttpHeaders.UPGRADE);
        return "websocket".equalsIgnoreCase(upgrade);
    }

    /**
     * 读取请求体并缓存，返回请求体字符串（支持所有类型，非JSON返回原始字符串或提示）
     * @param exchange ServerWebExchange
     * @return Mono<String> 请求体字符串（缓存后）
     */
    public static Mono<String> readAndCacheBody(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();
        // 尝试读取请求体（无论类型）
        return DataBufferUtils.join(request.getBody())
                .flatMap(dataBuffer -> {
                    // 1. 基础判断
                    MediaType contentType = request.getHeaders().getContentType();
                    if (contentType != null && contentType.includes(MediaType.MULTIPART_FORM_DATA)) {
                        // 文件上传不处理
                        return Mono.just("[Multipart form data, skip logging]");
                    } else if (contentType != null && contentType.includes(MediaType.APPLICATION_OCTET_STREAM)) {
                        // 二进制流不处理
                        return Mono.just("[Stream body, skip logging]");
                    } else if (dataBuffer.readableByteCount() > 1024 * 1024 * 10) {
                        // 请求体大于10M则不处理，防止OOM
                        return Mono.just("[too big Body, skip logging]");
                    }

                    // 2. 缓存原始请求体字节（无论是否JSON，确保后续可用）
                    byte[] bodyBytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bodyBytes);
                    // 释放缓冲区
                    DataBufferUtils.release(dataBuffer);
                    // 注意：ServerWebExchange 是不可变对象，mutate() 返回新实例，原 exchange 不变。
                    // 此方法目前仅在限流场景（doLimiter）中使用，限流时请求不会继续往下走，不需要替换 exchange。
                    // 若调用方需要在后续 filter 中重新读取请求体，应自行构建 ServerHttpRequestDecorator 并通过 exchange.mutate() 替换。
                    // 4. 转换为字符串（根据Content-Type处理）
                    String bodyStr;
                    if (contentType != null && contentType.isCompatibleWith(MediaType.APPLICATION_JSON)) {
                        bodyStr = new String(bodyBytes, StandardCharsets.UTF_8);
                    } else if (contentType != null && contentType.includes(MediaType.APPLICATION_FORM_URLENCODED)) {
                        bodyStr = new String(bodyBytes, StandardCharsets.UTF_8);
                    } else {
                        bodyStr = "[Non-JSON body, length: " + bodyBytes.length + "]";
                    }
                    return Mono.just(bodyStr);
                })
                .defaultIfEmpty("[Empty request body]")
                .onErrorResume(e -> {
                    // 读取异常时记录错误信息，不阻塞限流响应
                    exchange.getLogPrefix();
                    return Mono.just("[Failed to read body: " + e.getMessage() + "]");
                });
    }

}
