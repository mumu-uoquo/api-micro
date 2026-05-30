/**
 * Copyright (c) 2025, www.uoquo.com All Rights Reserved.
 * 注意：本内容仅限于内部传阅，禁止外泄
 */
package com.uoquo.gateway.main.config;

import com.uoquo.utils.spring.GenericJson2RedisSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import jakarta.annotation.PostConstruct;

/**
 * 自定义 Redis 配置
 * @author  uoquo team
 */
@Configuration
@ConditionalOnProperty(name = "app.redis.enabled", havingValue = "true", matchIfMissing = false)
public class RedisConfig {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @PostConstruct
    public void initConfiguration(){
        log.debug("RedisConfig init ...");
    }

    /**
     * REDIS配置处理
     */
    @Bean
    @Primary
    @ConditionalOnProperty(prefix = "spring.redis.default", name = "database", matchIfMissing = false)
//    @ConditionOnPropertyExists(name = "spring.redis.default.database")
    @ConfigurationProperties(prefix = "spring.redis.default")
    public RedisProperties redisProperties() {
        log.debug("Use RedisConfigProperties");
        return new RedisConfigProperties();
    }

    /**
     * Redis操作模板.
     * 与 {@link RedisAutoConfiguration}中的RedisConfiguration一致。<br>
     * 不一样的地方是，spring以名称注入@ConditionalOnMissingBean(name = "redisTemplate")，我们以class的形式注入
     */
    @Bean
    @ConditionalOnMissingBean(value = RedisTemplate.class)
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        log.debug("Use redisTemplate");
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        // 1. 设置key的序列化方法
        template.setKeySerializer(StringRedisSerializer.UTF_8);
        template.setHashKeySerializer(StringRedisSerializer.UTF_8);
        // 2. 设置val的序列化方法（默认：RedisSerializer.json()）
        template.setDefaultSerializer(new GenericJson2RedisSerializer());
        // 3. 开启事务
        template.setEnableTransactionSupport(true);
        template.afterPropertiesSet();
        return template;
    }
}
