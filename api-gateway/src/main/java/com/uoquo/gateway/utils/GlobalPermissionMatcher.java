/**
 * Copyright (c) 2025, www.uoquo.com All Rights Reserved.
 * 注意：本内容仅限于内部传阅，禁止外泄
 */
package com.uoquo.gateway.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.PathContainer;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

import com.uoquo.utils.StringUtil;
import com.uoquo.utils.spring.RedisUtil;
import com.uoquo.web.BaseCacheKey;

/**
 * 描述：全局免登录 URL 匹配器. <br>
 * 背景：各微服务启动后，会把 {@code @IgnoreAuth(all/login)} 标注的 URL 登记到 Redis 的
 *      {@link BaseCacheKey#GLOBAL_PERMISSION} 集合中，登记的 URL 可能是路径模式（如 {@code /user/{id}}），
 *      不能直接用集合的"是否存在"来判断，因此网关把集合缓存到本地：
 *      不含路径变量或通配符的 URL 精确匹配，含路径变量或通配符的按路径模式匹配.<br>
 * 说明：本地缓存每隔 {@value #REFRESH_INTERVAL_MS} 毫秒刷新一次，微服务新增免登录接口后，最多延迟这个时间生效；
 *      读取 Redis 失败时保留上一次的缓存（首次读取失败时，所有 URL 都需要登录）.
 * @author  uoquo team
 */
public class GlobalPermissionMatcher {
    private static final Logger log = LoggerFactory.getLogger(GlobalPermissionMatcher.class);

    /** 缓存刷新间隔（毫秒）. */
    private static final long REFRESH_INTERVAL_MS = 10_000L;

    private final PathPatternParser parser = new PathPatternParser();

    private final Object lock = new Object();

    private volatile Snapshot snapshot = new Snapshot(Set.of(), List.of());

    private volatile long loadedAt = 0L;

    public GlobalPermissionMatcher() {
        // 与应用层保持一致：末尾带斜杠的 URL 不等同于不带斜杠的
        parser.setMatchOptionalTrailingSeparator(false);
    }

    /**
     * 是否是免登录的 URL.
     * @param path 请求路径
     */
    public boolean matches(String path) {
        if (StringUtil.isNull(path)) {
            return false;
        }
        Snapshot current = current();
        if (current.exact.contains(path)) {
            return true;
        }
        if (current.patterns.isEmpty()) {
            return false;
        }
        PathContainer container = PathContainer.parsePath(path);
        for (PathPattern pattern : current.patterns) {
            if (pattern.matches(container)) {
                return true;
            }
        }
        return false;
    }

    private Snapshot current() {
        if (System.currentTimeMillis() - loadedAt > REFRESH_INTERVAL_MS) {
            synchronized (lock) {
                // 多个请求同时到达时，只有第一个负责刷新
                if (System.currentTimeMillis() - loadedAt > REFRESH_INTERVAL_MS) {
                    reload();
                }
            }
        }
        return snapshot;
    }

    private void reload() {
        // 无论成功与否都更新时间，失败时也要等待一个间隔再重试，避免 Redis 异常时每个请求都去重试
        loadedAt = System.currentTimeMillis();
        try {
            Set<String> urls = RedisUtil.getSets(BaseCacheKey.GLOBAL_PERMISSION, String.class);
            Set<String> exact = new HashSet<>();
            List<PathPattern> patterns = new ArrayList<>();
            for (String url : urls) {
                if (StringUtil.isNull(url)) {
                    continue;
                }
                if (url.indexOf('{') < 0 && url.indexOf('*') < 0 && url.indexOf('?') < 0) {
                    exact.add(url);
                    continue;
                }
                try {
                    patterns.add(parser.parse(url));
                } catch (Exception e) {
                    log.warn("免登录URL[{}]不是合法的路径模式，已忽略：{}", url, e.getMessage());
                }
            }
            snapshot = new Snapshot(exact, patterns);
            log.debug("刷新全局免登录URL：精确 {} 个，模式 {} 个.", exact.size(), patterns.size());
        } catch (Exception e) {
            log.error("读取全局免登录URL失败，继续使用上一次的缓存.", e);
        }
    }

    private record Snapshot(Set<String> exact, List<PathPattern> patterns) {
    }
}
