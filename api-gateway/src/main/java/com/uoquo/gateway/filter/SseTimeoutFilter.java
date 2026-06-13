/**
 * Copyright (c) 2025, www.uoquo.com All Rights Reserved.
 * 注意：本内容仅限于内部传阅，禁止外泄
 */
package com.uoquo.gateway.filter;

import com.uoquo.gateway.utils.GatewayUtil;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * 描述：长连接响应超时动态覆盖过滤器.<br>
 * 检测 SSE（{@code Accept: text/event-stream}）或 WebSocket（{@code Upgrade: websocket}）请求，
 * 将当前路由的 {@code response-timeout} 覆盖为 0（不超时），避免网关在长连接静默期主动断流。<br>
 * <p>
 * 执行时机：路由匹配完成之后、NettyRoutingFilter 发起 upstream 请求之前。
 */
@Component
public class SseTimeoutFilter implements GlobalFilter, Ordered {

    /**
     * 在 NettyRoutingFilter（{@code Integer.MAX_VALUE}）之前执行，
     * 同时晚于 LoggingFilter（{@code HIGHEST_PRECEDENCE + 110}），
     * 确保路由属性已写入 exchange attributes。
     */
    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 100;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (!GatewayUtil.isLongLivedRequest(exchange.getRequest())) {
            return chain.filter(exchange);
        }

        Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
        if (route != null) {
            Map<String, Object> metadata = new HashMap<>(route.getMetadata());
            // response-timeout=0 表示不超时，SSE/WebSocket 长连接必须
            metadata.put("response-timeout", 0);
            Route longLivedRoute = Route.async()
                    .asyncPredicate(route.getPredicate())
                    .filters(route.getFilters())
                    .id(route.getId())
                    .uri(route.getUri())
                    .order(route.getOrder())
                    .metadata(metadata)
                    .build();
            exchange.getAttributes().put(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR, longLivedRoute);
        }

        return chain.filter(exchange);
    }
}
