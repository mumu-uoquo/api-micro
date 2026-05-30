/**
 * Copyright (c) 2025, www.uoquo.com All Rights Reserved.
 * 注意：本内容仅限于内部传阅，禁止外泄
 */
package com.uoquo.gateway.filter;

import com.uoquo.gateway.utils.GatewayUtil;
import com.uoquo.utils.CompressUtil;
import com.uoquo.utils.CurrentUser;
import com.uoquo.utils.StringUtil;
import com.uoquo.utils.json.JsonUtil;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.factory.rewrite.CachedBodyOutputMessage;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.BodyInserterContext;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.*;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

/**
 * 描述：全局日志记录
 */
@Component
public class LoggingFilter implements GlobalFilter, Ordered {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 110;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 仅DEBUG模式才记录请求和响应日志
        // 若是SSE/WebSocket等流式请求，也不记录日志
        if (!logger.isDebugEnabled() || isLongLivedRequest(exchange.getRequest())) {
            return chain.filter(exchange);
        }
        // 基本信息
        GatewayUtil.parseInfo4Attributes(exchange.getAttributes());
        ServerHttpRequest request = exchange.getRequest();
        String path        = request.getPath().pathWithinApplication().value();
        String method      = request.getMethod().name();
        HttpHeaders header = request.getHeaders();
        Map<String, String> params = request.getQueryParams().toSingleValueMap();
        // 拼接日志对象
        LoggingData logData = new LoggingData();
        logData.setRequestTime(new Date());
        logData.setRequestMethod(method);
        logData.setRequestPath(path);
        logData.setRequestParams(params);
        logData.setHeaders(header);
        logData.setClientIp(GatewayUtil.getClientIp(request));
        logData.setOrigin(GatewayUtil.getHeaderFirst(header, params, HttpHeaders.ORIGIN));
        logData.setAppid(GatewayUtil.getHeader(header, params, CurrentUser.APPID));
        logData.setToken(GatewayUtil.getHeader(header, params, CurrentUser.TOKEN));
        logData.setNonce(GatewayUtil.getHeader(header, params, CurrentUser.NONCE));
        logData.setDeviceId(GatewayUtil.getHeader(header, params, CurrentUser.DEVICE_ID));
        // 路由信息
        Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
        if (route != null) {
            logData.setRoute(route);
            logData.setTargetServer(route.getUri().toString());
        }

        logData.setRequestId(StringUtil.isNull(CurrentUser.getTraceId()) ? request.getId() : CurrentUser.getTraceId());

        MediaType reqType = request.getHeaders().getContentType();
        logData.setContentType(reqType);
        // 记录请求体
        if (reqType != null && reqType.isCompatibleWith(MediaType.APPLICATION_JSON)){
            return writeBodyLog(exchange, chain, logData);
        } else {
            return writeBasicLog(exchange, chain, logData);
        }
    }

    /**
     * 普通请求：不解析请求体
     */
    private Mono<Void> writeBasicLog(ServerWebExchange exchange, GatewayFilterChain chain, LoggingData logData) {
        // 获取响应体
        ServerHttpResponseDecorator decoratedResponse = recordResponseLog(exchange, logData);
        return chain.filter(exchange.mutate().response(decoratedResponse).build())
                .then(Mono.fromRunnable(() -> {
                    // 打印日志
                    GatewayUtil.parseInfo4Attributes(exchange.getAttributes());
                    writeAccessLog(logData);
                }));
    }

    /**
     * JSON请求，解决 request body 只能读取一次问题，
     * 参考: org.springframework.cloud.gateway.filter.factory.rewrite.ModifyRequestBodyGatewayFilterFactory
     */
    private Mono<Void> writeBodyLog(ServerWebExchange exchange, GatewayFilterChain chain, LoggingData logData) {
        ServerRequest serverRequest = ServerRequest.create(exchange, HandlerStrategies.withDefaults().messageReaders());
        Mono<String> modifiedBody = serverRequest.bodyToMono(String.class)
                .flatMap(body ->{
                    logData.setRequestBody(body);
                    return Mono.just(body);
                });

        // 通过 BodyInserter 插入 body(支持修改body), 避免 request body 只能获取一次
        BodyInserter<Mono<String>, ReactiveHttpOutputMessage> bodyInserter = BodyInserters.fromPublisher(modifiedBody, String.class);
        HttpHeaders headers = new HttpHeaders();
        headers.putAll(exchange.getRequest().getHeaders());
        // the new content type will be computed by bodyInserter
        // and then set in the request decorator
        headers.remove(HttpHeaders.CONTENT_LENGTH);
        CachedBodyOutputMessage outputMessage = new CachedBodyOutputMessage(exchange, headers);

        return bodyInserter.insert(outputMessage, new BodyInserterContext())
                .then(Mono.defer(() -> {
                    // 重新封装请求
                    ServerHttpRequest decoratedRequest = new ServerHttpRequestDecorator(exchange.getRequest()) {
                        @Override
                        public HttpHeaders getHeaders() {
                            HttpHeaders httpHeaders = new HttpHeaders();
                            httpHeaders.putAll(super.getHeaders());
                            // 重新计算实际字节长度并设置 Content-Length，避免强制 chunked 导致下游解析异常
                            byte[] bodyBytes = logData.getRequestBody() != null
                                    ? logData.getRequestBody().getBytes(StandardCharsets.UTF_8)
                                    : new byte[0];
                            httpHeaders.setContentLength(bodyBytes.length);
                            httpHeaders.remove(HttpHeaders.TRANSFER_ENCODING);
                            return httpHeaders;
                        }

                        @Override
                        public Flux<DataBuffer> getBody() {
                            return outputMessage.getBody();
                        }
                    };

                    // 拼接响应日志
                    ServerHttpResponseDecorator decoratedResponse = recordResponseLog(exchange, logData);
                    return chain.filter(exchange.mutate().request(decoratedRequest).response(decoratedResponse).build())
                            .then(Mono.fromRunnable(() -> {
                                // 打印日志
                                GatewayUtil.parseInfo4Attributes(exchange.getAttributes());
                                writeAccessLog(logData);
                            }));
                }));
    }

    /**
     * 拼接响应日志<br>
     * 通过 DataBufferFactory 解决响应体分段传输问题。
     */
    private ServerHttpResponseDecorator recordResponseLog(ServerWebExchange exchange, LoggingData logData) {
        ServerHttpResponse response = exchange.getResponse();
        DataBufferFactory bufferFactory = response.bufferFactory();
        return new ServerHttpResponseDecorator(response) {
            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                if (body instanceof Flux) {
                    logData.setResponseTime(new Date());
                    logData.setResponseStatus(response.getStatusCode() != null ? response.getStatusCode().toString() : "");
                    MediaType resType = response.getHeaders().getContentType();
                    // SSE/流式响应直接透传，不走 .buffer()，避免阻塞永不结束的响应流
                    if (resType != null && resType.isCompatibleWith(MediaType.TEXT_EVENT_STREAM)) {
                        return super.writeWith(body);
                    }
                    // 仅拼接响应为JSON格式的内容
                    if ((resType != null) && resType.isCompatibleWith(MediaType.APPLICATION_JSON)) {
                        Flux<? extends DataBuffer> fluxBody = Flux.from(body);
                        return super.writeWith(fluxBody.buffer().map(dataBuffers -> {
                            // 合并多个流集合，解决返回体分段传输
                            // 使用 response 自身的 bufferFactory 而非 DefaultDataBufferFactory，
                            // 确保 join 后的 buffer 与响应上下文一致
                            DataBuffer join = bufferFactory.join(dataBuffers);
                            byte[] content = new byte[join.readableByteCount()];
                            join.read(content);
                            // 释放合并后的 buffer（原始 dataBuffers 在 join 时已被消费，无需单独 release）
                            DataBufferUtils.release(join);
                            // 若是压缩数据，则需要解压
                            String responseResult = null;
                            try {
                                if (CompressUtil.isGzip(content)) {
                                    responseResult = new String(CompressUtil.unGzip(content), StandardCharsets.UTF_8);
                                } else {
                                    responseResult = new String(content, StandardCharsets.UTF_8);
                                }
                            } catch (IOException e) {
                                responseResult = StringUtil.byte2hex(content);
                            }
                            logData.setResponseData(responseResult);
                            // 分配新 buffer 写出，避免 wrap 零拷贝引用在 release 后失效
                            DataBuffer outBuffer = bufferFactory.allocateBuffer(content.length);
                            outBuffer.write(content);
                            return outBuffer;
                        }));
                    }
                }
                // if body is not a flux. never got there.
                return super.writeWith(body);
            }
        };
    }

    /**
     * 打印日志
     */
    private void writeAccessLog(LoggingData logData) {
        double sec = 0;
        if (logData.getRequestTime() != null && logData.getResponseTime() != null) {
            long bgn = logData.getRequestTime().getTime();
            long end = logData.getResponseTime().getTime();
            sec = (end - bgn) / 1000d;
        }
        logger.debug("request [{}] [{}] [{}] [{}] [{}s]. target={}, appkey={}, client_ip={}, device={}, token={}, user={}, header={}, params={}, \nbody={} \nresponse={}.",
                logData.getNonce(), logData.getResponseStatus(), logData.getRequestMethod(), logData.getRequestPath(), String.format("%.3f", sec),
                logData.getTargetServer(), logData.getAppid(), logData.getClientIp(), logData.getDeviceId(), logData.getToken(), JsonUtil.serialize(CurrentUser.getInfo()),
                JsonUtil.serialize(logData.getHeaders()), logData.getRequestParams(), logData.getRequestBody(),logData.getResponseData());
    }

    /**
     * 判断是否是长连接请求（SSE 或 WebSocket）.<br>
     * 长连接请求不记录日志，避免 .buffer() 阻塞永不结束的响应流。
     */
    private boolean isLongLivedRequest(ServerHttpRequest request) {
        // 1. Accept: text/event-stream → SSE（协议标准，优先判断）
        String accept = request.getHeaders().getFirst(HttpHeaders.ACCEPT);
        if (accept != null && accept.contains(MediaType.TEXT_EVENT_STREAM_VALUE)) {
            return true;
        }
        // 2. Upgrade: websocket → WebSocket 升级请求
        String upgrade = request.getHeaders().getFirst(HttpHeaders.UPGRADE);
        if ("websocket".equalsIgnoreCase(upgrade)) {
            return true;
        }
        // 3. 路径约定 /sse/ → 直接跳过，不检查 Content-Type（text/event-stream 是响应头，请求阶段不存在）
        return request.getPath().toString().contains("/sse/");
    }

}
