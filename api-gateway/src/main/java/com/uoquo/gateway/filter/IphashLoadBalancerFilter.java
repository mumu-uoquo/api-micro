/**
 * Copyright (c) 2025, www.uoquo.com All Rights Reserved.
 * 注意：本内容仅限于内部传阅，禁止外泄
 */
package com.uoquo.gateway.filter;

import com.uoquo.gateway.loadbalancer.IphashLoadBalancer;
import com.uoquo.gateway.utils.GatewayUtil;
import com.uoquo.web.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.*;
import org.springframework.cloud.gateway.config.GatewayLoadBalancerProperties;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.ReactiveLoadBalancerClientFilter;
import org.springframework.cloud.gateway.support.DelegatingServiceInstance;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.cloud.loadbalancer.core.ReactorLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ReactorServiceInstanceLoadBalancer;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Map;
import java.util.Set;

/**
 * IP Hash 过滤器（根据 IP Hash 的负载策略处理）
 * {@link org.springframework.cloud.gateway.filter.ReactiveLoadBalancerClientFilter ReactiveLoadBalancerClientFilter}
 */
@Component
public class IphashLoadBalancerFilter implements GlobalFilter, Ordered {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final LoadBalancerClientFactory clientFactory;

    private final GatewayLoadBalancerProperties properties;

    @Override
    public int getOrder() {
        // 滞后于 lb 的解析
        return ReactiveLoadBalancerClientFilter.LOAD_BALANCER_CLIENT_FILTER_ORDER + 1;
    }

    public IphashLoadBalancerFilter(LoadBalancerClientFactory clientFactory, GatewayLoadBalancerProperties properties) {
        this.clientFactory = clientFactory;
        this.properties = properties;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        GatewayUtil.parseInfo4Attributes(exchange.getAttributes());
        URI url = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR);
        String schemePrefix = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_SCHEME_PREFIX_ATTR);
        // 只处理 iphash 开头的配置
        if (url == null || (!"iphash".equals(url.getScheme()) && !"iphash".equals(schemePrefix))) {
            return chain.filter(exchange);
        }
        // preserve the original url
        ServerWebExchangeUtils.addOriginalRequestUrl(exchange, url);
        if (log.isTraceEnabled()) {
            log.trace(IphashLoadBalancerFilter.class.getSimpleName() + " url before: " + url);
        }
        // 构造请求内容
        String serviceId = url.getHost();
        DefaultRequest<RequestDataContext> lbRequest = createRequest(exchange.getRequest(), serviceId);
        // 获取LB生命周期管理bean，并在完成相关操作后，执行相关方法
        Set<LoadBalancerLifecycle> supportedLifecycleProcessors = LoadBalancerLifecycleValidator
                .getSupportedLifecycleProcessors(clientFactory.getInstances(serviceId, LoadBalancerLifecycle.class),
                        RequestDataContext.class, ResponseData.class, ServiceInstance.class);

        // 返回
        return choose(lbRequest, serviceId, supportedLifecycleProcessors).doOnNext(response -> {
                    GatewayUtil.parseInfo4Attributes(exchange.getAttributes());
                    if (!response.hasServer()) {
                        supportedLifecycleProcessors.forEach(lifecycle -> lifecycle
                                .onComplete(new CompletionContext<>(CompletionContext.Status.DISCARD, lbRequest, response)));
//                        throw NotFoundException.create(properties.isUse404(), "Unable to find instance for " + url.getHost());
                        throw new ResourceNotFoundException("Unable to find instance for " + url.getHost());
                    }
                    // 将实际请求协议
                    ServiceInstance retrievedInstance = response.getServer();
                    URI uri = exchange.getRequest().getURI();
                    // if the `lb:<scheme>` mechanism was used, use `<scheme>` as the default,
                    // if the loadbalancer doesn't provide one.
                    String overrideScheme = retrievedInstance.isSecure() ? "https" : "http";
                    if (schemePrefix != null) {
                        overrideScheme = url.getScheme();
                    }
                    DelegatingServiceInstance serviceInstance = new DelegatingServiceInstance(retrievedInstance, overrideScheme);
                    URI requestUrl = reconstructURI(serviceInstance, uri);
                    if (log.isTraceEnabled()) {
                        log.trace("LoadBalancerClientFilter url chosen: " + requestUrl);
                    }
                    // 将实际请求信息放入attributes
                    exchange.getAttributes().put(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR, requestUrl);
                    exchange.getAttributes().put(ServerWebExchangeUtils.GATEWAY_LOADBALANCER_RESPONSE_ATTR, response);
                    supportedLifecycleProcessors.forEach(lifecycle -> lifecycle.onStartRequest(lbRequest, response));
                }).then(chain.filter(exchange))
                .doOnError(throwable -> supportedLifecycleProcessors.forEach(lifecycle -> lifecycle
                        .onComplete(new CompletionContext<ResponseData, ServiceInstance, RequestDataContext>(
                                CompletionContext.Status.FAILED, throwable, lbRequest,
                                exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_LOADBALANCER_RESPONSE_ATTR)))))
                .doOnSuccess(aVoid -> supportedLifecycleProcessors.forEach(lifecycle -> lifecycle
                        .onComplete(new CompletionContext<ResponseData, ServiceInstance, RequestDataContext>(
                                CompletionContext.Status.SUCCESS, lbRequest,
                                exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_LOADBALANCER_RESPONSE_ATTR),
                                new ResponseData(exchange.getResponse(), new RequestData(exchange.getRequest()))))));
    }

    protected URI reconstructURI(ServiceInstance serviceInstance, URI original) {
        return LoadBalancerUriTools.reconstructURI(serviceInstance, original);
    }

    private Mono<Response<ServiceInstance>> choose(Request<RequestDataContext> lbRequest, String serviceId,
                                                   Set<LoadBalancerLifecycle> supportedLifecycleProcessors) {
        // 先使用 iphash 负载
        IphashLoadBalancer iphashLoadBalancer = this.clientFactory.getInstance(serviceId, IphashLoadBalancer.class);
        if (iphashLoadBalancer != null) {
            supportedLifecycleProcessors.forEach(lifecycle -> lifecycle.onStart(lbRequest));
            return iphashLoadBalancer.choose(serviceId, lbRequest);
        }
        // 没有 iphash 时，继续使用 lb 负载
        ReactorLoadBalancer<ServiceInstance> loadBalancer = this.clientFactory.getInstance(serviceId, ReactorServiceInstanceLoadBalancer.class);
        if (loadBalancer == null) {
//            throw new NotFoundException("No loadbalancer available for " + serviceId);
            throw new ResourceNotFoundException("No loadbalancer available for " + serviceId);
        }
        supportedLifecycleProcessors.forEach(lifecycle -> lifecycle.onStart(lbRequest));
        return loadBalancer.choose(lbRequest);
    }

    private DefaultRequest<RequestDataContext> createRequest(ServerHttpRequest request, String serviceId) {
        // 1. 灰度配置
        String hint = getHint(serviceId);
        // 2. 请求信息
        RequestData requestData = new RequestData(request);
        // 2.1 缓存来源地址信息
        requestData.getAttributes().put(IphashLoadBalancer.LOCAL_ADDRESS,  request.getLocalAddress());
        requestData.getAttributes().put(IphashLoadBalancer.REMOTE_ADDRESS, request.getRemoteAddress());
        // 2.2 缓存当前用户信息
        GatewayUtil.putInfo2Attributes(requestData.getAttributes());
        // 3. 构造请求
        return new DefaultRequest<>(new RequestDataContext(requestData, hint));
    }
    private String getHint(String serviceId) {
        LoadBalancerProperties loadBalancerProperties = clientFactory.getProperties(serviceId);
        Map<String, String> hints = loadBalancerProperties.getHint();
        String defaultHint = hints.getOrDefault("default", "default");
        String hintPropertyValue = hints.get(serviceId);
        return hintPropertyValue != null ? hintPropertyValue : defaultHint;
    }
}
