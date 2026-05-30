/**
 * Copyright (c) 2025, www.uoquo.com All Rights Reserved.
 * 注意：本内容仅限于内部传阅，禁止外泄
 */
package com.uoquo.gateway.loadbalancer;

import com.uoquo.gateway.utils.GatewayUtil;
import com.uoquo.utils.CurrentUser;
import com.uoquo.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.*;
import org.springframework.cloud.loadbalancer.core.*;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * IP Hash负载均衡器
 * 该类不能继承 {@link org.springframework.cloud.loadbalancer.core.RoundRobinLoadBalancer RoundRobinLoadBalancer}，否则会影响 RoundRobinLoadBalancer 的注入
 */
public class IphashLoadBalancer  { // implements ReactiveLoadBalancer<ServiceInstance>
    private final Logger log = LoggerFactory.getLogger(getClass());

    private ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider;

    private Map<String, ObjectProvider<ServiceInstanceListSupplier>> serviceInstanceMap = new ConcurrentHashMap<>();

    private LoadBalancerClientFactory clientFactory;

    public static final String LOCAL_ADDRESS = "LocalAddress";
    public static final String REMOTE_ADDRESS = "RemoteAddress";

    public IphashLoadBalancer(LoadBalancerClientFactory clientFactory) {
//        this.serviceInstanceListSupplierProvider = serviceInstanceListSupplierProvider;
        this.clientFactory = clientFactory;
    }

    public Mono<Response<ServiceInstance>> choose(String serviceId, Request request) {
        // 此处还是在 IphashLoadBalancerFilter 的线程中，所以不需要重新获取 CurrentUser
        RequestData requestData = ((RequestDataContext)request.getContext()).getClientRequest();
        ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider =
                serviceInstanceMap.computeIfAbsent(serviceId,
                        k-> clientFactory.getProvider(serviceId, ServiceInstanceListSupplier.class));
        ServiceInstanceListSupplier supplier = serviceInstanceListSupplierProvider
                .getIfAvailable(NoopServiceInstanceListSupplier::new);
        return supplier.get(request).next()
                .map(serviceInstances -> {
                    // 拿到 IphashLoadBalancerFilter.createRequest 放入的用户信息
                    GatewayUtil.parseInfo4Attributes(requestData.getAttributes());
                    return getInstanceResponse(serviceId, requestData, serviceInstances);
                });
    }

    private Response<ServiceInstance> getInstanceResponse(String serviceId, RequestData requestData, List<ServiceInstance> instances) {
        if (instances.isEmpty()) {
            log.warn("No servers available for service: {}", serviceId);
            return new EmptyResponse();
        }
        String clientIp = CurrentUser.getClientIp();
        if (StringUtil.isNull(clientIp)) {
            clientIp = this.getClientIp(requestData);
        }
        int pos;
        if (StringUtil.isNull(clientIp)) {
            // 获取不到clientIp时，采用随机模式
            pos = ThreadLocalRandom.current().nextInt(instances.size());
        } else {
            // 获取到clientIp时，采用hash
            int hash = Math.abs(clientIp.hashCode());
            pos = hash % instances.size();
        }
        ServiceInstance instance = instances.get(pos);
        log.info("servers [{}], request ip [{}], instance [{}/{}] is [{}:{}]",
                serviceId, clientIp, pos, instances.size(), instance.getHost(), instance.getPort());
        return new DefaultResponse(instance);
    }

    private String getClientIp(RequestData requestData) {
        // 1. 优先获取请求头中的
        String clientIP = GatewayUtil.getClientIp(requestData.getHeaders());
        // 2. 再获取getRemoteAddr
        if (StringUtil.isNull(clientIP) || "unknown".equalsIgnoreCase(clientIP)) {
            InetSocketAddress remoteAddress = (InetSocketAddress)requestData.getAttributes().get(REMOTE_ADDRESS);
            clientIP = remoteAddress.getAddress().getHostAddress();
        }
        // 返回内容
        if (StringUtil.isNull(clientIP) || "unknown".equalsIgnoreCase(clientIP)) {
            return null;
        } else {
            return clientIP;
        }
    }

}
