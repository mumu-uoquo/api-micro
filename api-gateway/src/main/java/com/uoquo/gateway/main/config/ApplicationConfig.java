/**
 * Copyright (c) 2025, www.uoquo.com All Rights Reserved.
 * 注意：本内容仅限于内部传阅，禁止外泄
 */
package com.uoquo.gateway.main.config;

import com.uoquo.gateway.handler.GlobalExceptionHandler;
import com.uoquo.gateway.loadbalancer.IphashLoadBalancer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.result.view.ViewResolver;

import jakarta.annotation.PostConstruct;
import java.util.stream.Collectors;

/**
 * 配置类：网关基础配置
 * @author uoquo
 */
@ComponentScan({
    "com.uoquo.**.handler",  // redis、spring等工具类（@Component）
    "com.uoquo.**.filter",   // 鉴权过滤器
})
@Configuration
public class ApplicationConfig {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @PostConstruct
    public void initConfiguration(){
        log.debug("ApplicationConfig init ...");
    }

//    /**
//     * 跨域配置<br>
//     * 备注：跨域可以交由nginx等中间层处理，或交由gateway的标准配置处理
//     * https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/#cors-configuration
//     */
//    @Bean
//    public WebFilter corsFilter() {
//        return (ServerWebExchange ctx, WebFilterChain chain) -> {
//            ServerHttpRequest request = ctx.getRequest();
//            if (CorsUtils.isCorsRequest(request)) {
//                ServerHttpResponse response = ctx.getResponse();
//                HttpHeaders headers = response.getHeaders();
//                headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN,  request.getHeaders().getOrigin());
//                headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "GET, POST, OPTIONS, PUT, DELETE, PATCH");
//                // 允许携带cookie
//                headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
//                // 允许携带的头信息（默认有：Accept, Accept-Language, Content-Language, Content-Type（仅限application/x-www-form-urlencoded, multipart/form-data, text/plain））
//                //headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "*");
//                headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "Origin, No-Cache, X-Requested-With, If-Modified-Since, Pragma, Last-Modified, Cache-Control, Expires, Content-Type, appid, token, nonce, signature-app, user-language");
//                // 允许前端读取的响应头（默认有： Cache-Control, Content-Language, Content-Type, Expires, Last-Modified, Pragma）
//                headers.add(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "*");
//                // 预检结果的缓存时长
//                headers.add(HttpHeaders.ACCESS_CONTROL_MAX_AGE, "18000L");
//
//                if (request.getMethod() == HttpMethod.OPTIONS) {
//                    response.setStatusCode(HttpStatus.OK);
//                    // return Mono.empty();
//                    return response.setComplete();
//                }
//            }
//            return chain.filter(ctx);
//        };
//    }

    /**
     * 自定义统一异常处理.
     * 参考：{@link org.springframework.boot.autoconfigure.web.reactive.error.ErrorWebFluxAutoConfiguration}
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public ErrorWebExceptionHandler errorWebExceptionHandler(ServerProperties serverProperties, ErrorAttributes errorAttributes,
                                                             WebProperties webProperties, ObjectProvider<ViewResolver> viewResolvers,
                                                             ServerCodecConfigurer serverCodecConfigurer, ApplicationContext applicationContext) {
        log.debug("Use GlobalExceptionHandler");
        GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler(errorAttributes,
                webProperties.getResources(), serverProperties.getError(), applicationContext);
        exceptionHandler.setViewResolvers(viewResolvers.orderedStream().collect(Collectors.toList()));
        exceptionHandler.setMessageWriters(serverCodecConfigurer.getWriters());
        exceptionHandler.setMessageReaders(serverCodecConfigurer.getReaders());
        return exceptionHandler;
    }

    /**
     * Iphash负载，此处无法参考 {@link org.springframework.cloud.loadbalancer.core.RoundRobinLoadBalancer RoundRobinLoadBalancer},
     * 总是无法获取到服务实例，原因待查
     */
    @Bean
    public IphashLoadBalancer iphashLoadBalancer(LoadBalancerClientFactory clientFactory) {
        log.debug("Use IphashLoadBalancer");
        return new IphashLoadBalancer(clientFactory);
    }
}
