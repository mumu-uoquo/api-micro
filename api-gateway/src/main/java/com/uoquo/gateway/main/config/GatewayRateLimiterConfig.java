/**
 * Copyright (c) 2025, www.uoquo.com All Rights Reserved.
 * 注意：本内容仅限于内部传阅，禁止外泄
 */
package com.uoquo.gateway.main.config;

import com.uoquo.gateway.utils.GatewayUtil;
import com.uoquo.utils.CurrentUser;
import com.uoquo.utils.StringUtil;
import com.uoquo.utils.crypto.SHA;
import com.uoquo.web.BaseCacheKey;
import com.uoquo.utils.spring.RedisUtil;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Mono;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

/**
 * 配置类：网关限流过滤器
 */
@Configuration
public class GatewayRateLimiterConfig {

    private final static String LIMIT_KEY_PREFIX = "LIMITER:";
    // 用于标准化路径：去除重复斜杠（如 "//" 转为 "/"）
    private static final Pattern DUPLICATE_SLASH_PATTERN = Pattern.compile("/+");

    /**
     * 限流过滤器的KEY：来源IP
     */
    @Bean("ipKeyResolver")
    public KeyResolver ipKeyResolver() {
        return exchange -> {
            String clientIp = GatewayUtil.getClientIp(exchange.getRequest());
            if (clientIp == null) {
                clientIp = "unknown";
            }
            return Mono.just(LIMIT_KEY_PREFIX + clientIp);
        };
    }

    /**
     * 限流过滤器的KEY：请求接口
     */
    @Bean("pathKeyResolver")
    public KeyResolver pathKeyResolver() {
        return exchange -> {
            // 获取原始路径（如 "/api//user/"）
            String path = exchange.getRequest().getPath().value();
//            // 替换重复斜杠为单个斜杠，确保路径格式统一（如 "/api//user/" → "/api/user/"）
//            path = DUPLICATE_SLASH_PATTERN.matcher(path).replaceAll("/");
            // 若路径为空（仅可能是根路径），返回 "/"
            path = StringUtil.isNull(path) ? "/" : path;
            String key = SHA.sha256(path);
            if (key == null) {
                key = URLEncoder.encode(path, StandardCharsets.UTF_8);
                if (key.length() > 64) {
                    key = key.substring(0, 64);
                }
            }
            return Mono.just(LIMIT_KEY_PREFIX + key);
        };
    }

    /**
     * 限流过滤器的KEY：指定用户
     */
    @Bean("userKeyResolver")
    public KeyResolver userKeyResolver() {
        // 根据参数进行限流
        return exchange -> {
            String userId = null;
            String token = exchange.getRequest().getHeaders().getFirst(CurrentUser.TOKEN);
            if (token != null) {
                CurrentUser.UserInfo user = RedisUtil.getLocalCache(BaseCacheKey.USER_INFO_PREFIX + token, CurrentUser.UserInfo.class);
                if (user != null) {
                    userId = user.getUserId();
                }
            }
            if (StringUtil.isNull(userId)) {
                userId = "unknown";
            }
            return Mono.just(LIMIT_KEY_PREFIX + userId);
        };
    }

    /**
     * 限流过滤器的KEY：按会话
     */
    @Primary
    @Bean("tokenKeyResolver")
    public KeyResolver tokenKeyResolver() {
        // 根据参数进行限流
        return exchange -> {
            // 来源APPID
            String appid = exchange.getRequest().getHeaders().getFirst(CurrentUser.APPID);
            if (StringUtil.isNull(appid)) {
                appid = "unknown";
            }
            // 来源用户
            String token = exchange.getRequest().getHeaders().getFirst(CurrentUser.TOKEN);
            if (StringUtil.isNull(token)) {
                // 未登录用户（或第三方请求）：尝试提取设备ID
                token = exchange.getRequest().getHeaders().getFirst(CurrentUser.DEVICE_ID);
                if (StringUtil.isNull(token)) {
                    // 无设备ID：提取客户端IP
                    token = GatewayUtil.getClientIp(exchange.getRequest());
                    if (StringUtil.isNull(token)) {
                        token = "unknown";
                    }
                }
            }
            String key = appid + ":" + token;
            if (key.length() > 100) {
                key = key.substring(0, 100);
            }
            return Mono.just(LIMIT_KEY_PREFIX + key);
        };
    }


}
