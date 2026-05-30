/**
 * Copyright (c) 2025, www.uoquo.com All Rights Reserved.
 * 注意：本内容仅限于内部传阅，禁止外泄
 */
package com.uoquo.gateway.filter;

import com.uoquo.gateway.utils.GatewayUtil;
import com.uoquo.utils.CurrentUser;
import com.uoquo.utils.json.JsonUtil;
import com.uoquo.web.ReturnData;
import com.uoquo.web.exception.TooManyRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.RequestRateLimiterGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RateLimiter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.HttpStatusHolder;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 自定义网关限流过滤器。
 * 实现：响应信息自定义格式输出
 * spring约定过滤器类名 "xxxGatewayFilterFactory"，其中"xxx"为配置中的name;
 */
@Component
public class GatewayRequestRateLimiterGatewayFilterFactory extends RequestRateLimiterGatewayFilterFactory {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private static final String EMPTY_KEY = "____EMPTY_KEY__";

    private final RateLimiter defaultRateLimiter;

    private final KeyResolver defaultKeyResolver;

    /**
     * Switch to deny requests if the Key Resolver returns an empty key, defaults to true.
     */
    private boolean denyEmptyKey = true;

    /** HttpStatus to return when denyEmptyKey is true, defaults to FORBIDDEN. */
    private final String emptyKeyStatusCode = HttpStatus.FORBIDDEN.name();

    public GatewayRequestRateLimiterGatewayFilterFactory(RateLimiter defaultRateLimiter, KeyResolver defaultKeyResolver) {
        super(defaultRateLimiter, defaultKeyResolver);
        this.defaultRateLimiter = defaultRateLimiter;
        this.defaultKeyResolver = defaultKeyResolver;
    }

    @Override
    public GatewayFilter apply(Config config) {
        KeyResolver resolver = getOrDefault(config.getKeyResolver(), defaultKeyResolver);
        RateLimiter<Object> limiter = getOrDefault(config.getRateLimiter(), defaultRateLimiter);
        boolean denyEmpty = getOrDefault(config.getDenyEmptyKey(), this.denyEmptyKey);
        HttpStatusHolder emptyKeyStatus = HttpStatusHolder
                .parse(getOrDefault(config.getEmptyKeyStatus(), this.emptyKeyStatusCode));

        return (exchange, chain) -> resolver.resolve(exchange).defaultIfEmpty(EMPTY_KEY).flatMap(key -> {
            if (EMPTY_KEY.equals(key)) {
                if (denyEmpty) {
                    ServerWebExchangeUtils.setResponseStatus(exchange, emptyKeyStatus);
                    return exchange.getResponse().setComplete();
                }
                return chain.filter(exchange);
            }
            String routeId = config.getRouteId();
            if (routeId == null) {
                Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
                routeId = (route == null) ? "" : route.getId();
            }
            String finalRouteId = routeId;
            // 限流判断逻辑
            return limiter.isAllowed(finalRouteId, key).flatMap(response -> {
                // flatMap将启动另一线程，因此先读取exchange中的CurrentUser信息
                GatewayUtil.parseInfo4Attributes(exchange.getAttributes());
                for (Map.Entry<String, String> header : response.getHeaders().entrySet()) {
                    exchange.getResponse().getHeaders().add(header.getKey(), header.getValue());
                }
                if (response.isAllowed()) {
                    return chain.filter(exchange);
                } else {
                    // 限流时需要单独记录日志，便于抓取并预警，因此不抛异常给统一处理器
//                    throw new TooManyRequestException();
                    return doLimiter(exchange, config, finalRouteId);
                }
            });
        });
    }

    private <T> T getOrDefault(T configValue, T defaultValue) {
        return (configValue != null) ? configValue : defaultValue;
    }

    /**
     * 限流处理
     */
    private Mono<Void> doLimiter(ServerWebExchange exchange, Config config, String finalRouteId) {
        // 1. 设置响应状态码和 headers
        ServerWebExchangeUtils.setResponseStatus(exchange, config.getStatusCode());
        TooManyRequestException ex = new TooManyRequestException();
        HttpHeaders headers = exchange.getResponse().getHeaders();
        headers.add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        headers.add("response-code", ex.getStatus());
        // 2. 读取并缓存请求体（确保后续流程可用），同时打印日志（无论是否读取成功）
        Mono<String> bodyMono = GatewayUtil.readAndCacheBody(exchange);
        return bodyMono.doOnNext(body -> {
            // 3. 打印日志
            ServerHttpRequest request = exchange.getRequest();
            String userInfo = JsonUtil.serialize(CurrentUser.getInfo());
            String clientIp = GatewayUtil.getClientIp(request);
            String method   = request.getMethod().name();
            String path     = request.getPath().toString();
            String reqType  = request.getHeaders().getContentType() == null ? "" : request.getHeaders().getContentType().toString();
            String params   = request.getQueryParams().toString();
            log.warn("request[{}] [{}] [{}] [{}] error. code={}, message={}, target={}, appkey={}, client_ip={}, device={}, token={}, user={}, header={}, cookie={}, params={}, body={}.",
                    CurrentUser.getNonce(), method, path, reqType, ex.getCode(), ex.getMesg(), finalRouteId, CurrentUser.getAppkey(),
                    clientIp, CurrentUser.getDeviceId(), CurrentUser.getToken(), userInfo, request.getHeaders(), request.getCookies(), params, body);
        }).then(Mono.defer(() -> {
            // 4. 组装限流响应体
            ReturnData<String> data = new ReturnData<>(ex);
            DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(
                    JsonUtil.serialize(data).getBytes(StandardCharsets.UTF_8)
            );
            return exchange.getResponse().writeWith(Mono.just(buffer));
        }));
    }

}


