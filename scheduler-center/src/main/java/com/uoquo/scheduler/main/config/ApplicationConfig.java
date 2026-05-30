package com.uoquo.scheduler.main.config;

import com.uoquo.cloud.feign.FeignHeaderInterceptor;
import com.uoquo.cloud.loadbalancer.IphashLoadBalancerConfiguration;
import com.uoquo.scheduler.common.feign.SchedulerFeignHeaderInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;

import jakarta.annotation.PostConstruct;

/**
 * 应用配置类
 * <ul>
 *   <li>同一个服务只需要注册一个LoadBalancerClient负载均衡器（所以提取到配置类中）</li>
 * </ul>
 */
@Configuration
@Import(ApplicationDeveloperConfig.class)
@Order(0)
@LoadBalancerClient(name = "service-platform", configuration = IphashLoadBalancerConfiguration.class)
public class ApplicationConfig {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @PostConstruct
    public void setProperties() {
        log.debug("ApplicationConfig init ...");
    }

    @Bean
    public FeignHeaderInterceptor schedulerFeignHeaderInterceptor() {
        log.debug("加载BEAN：SchedulerFeignHeaderInterceptor");
        return new SchedulerFeignHeaderInterceptor();
    }
}
