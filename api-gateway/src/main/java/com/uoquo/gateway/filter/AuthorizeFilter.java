/**
 * Copyright (c) 2025, www.uoquo.com All Rights Reserved.
 * 注意：本内容仅限于内部传阅，禁止外泄
 */
package com.uoquo.gateway.filter;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.*;

import com.uoquo.gateway.utils.GatewayUtil;
import com.uoquo.utils.IDGenerator;
import com.uoquo.utils.StringUtil;
import com.uoquo.utils.json.JsonUtil;
import com.uoquo.utils.CurrentUser;
import com.uoquo.web.BaseCacheKey;
import com.uoquo.web.exception.*;
import com.uoquo.utils.spring.RedisUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

/**
 * 描述：全局鉴权拦截器. <br>
 * 参考：https://segmentfault.com/a/1190000016227780<br>
 * 日期：2018-01-18 16:28 <br>
 * 变更：
 * <pre>
 * Version      Date           ModifiedBy       Content
 * --------     ----------     ------------     -----------------------
 * 1.0          2018-01-18     xuhz.           创建
 * </pre>
 * @since   JDK 1.8
 * @version 1.0
 * @author  uoquo team
 */
@Component // 可以用扫描的方式注入，也可以在配置中使用@bean注入
public class AuthorizeFilter implements GlobalFilter, Ordered {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Value("${app.language:zh-cn")
    private String DEFAULT_LANGUAGE;

    @Override
    public int getOrder() {
        // Ordered.HIGHEST_PRECEDENCE 所有请求进来时第一个处理
        // LoadBalancerClientFilter.LOAD_BALANCER_CLIENT_FILTER_ORDER - 10; // 优先于LB
        return Ordered.HIGHEST_PRECEDENCE + 100;
    }

    /**
     * 授权校验，全局签名
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 从请求头及redis中获取当前用户等信息
        GatewayUtil.parseInfo4Request(exchange);
        // 将用户信息缓存到exchange，方便后续filter使用
        GatewayUtil.putInfo2Attributes(exchange.getAttributes());
        // 1. 必传参数校验
        HttpHeaders header = exchange.getRequest().getHeaders();
        Map<String, String> queryParams = exchange.getRequest().getQueryParams().toSingleValueMap();
        // 常规信息
        String sign = GatewayUtil.getHeader(header, queryParams, CurrentUser.SIGN_APP);
        String lan  = GatewayUtil.getHeader(header, queryParams, CurrentUser.USER_LANGUAGE);
        String path = exchange.getRequest().getPath().toString();
        // 免校验处理（常用于文件下载）
        boolean unsigned = RedisUtil.existSetItem(BaseCacheKey.GLOBAL_UNSIGNED, path);
        if (unsigned) {
            // 免校验时，需补充部分参数用于后续判断
            if (StringUtil.isNull(sign)) {
                sign = "default-" + IDGenerator.getNextULID();
            }
            if (StringUtil.isNull(lan)) {
                lan = DEFAULT_LANGUAGE;
            }
        }
        // 2. 授权校验（仅做基础校验，核心校验挪到应用层）
        this.checkAuthorize(path);
        // 3. 补充信息到请求头
        Map<String, List<String>> headerMap = new HashMap<>();
        this.populateUserInfo2Headers(headerMap);
        this.populateOtherInfo2Headers(headerMap, sign, lan);
        if (log.isDebugEnabled()) {
            log.debug("[{}]现有请求头信息：{}", path, JsonUtil.serialize(header));
            log.debug("[{}]现有请求URL参数：{}", path, JsonUtil.serialize(queryParams));
            log.debug("[{}]新增请求头信息：{}", path, JsonUtil.serialize(headerMap));
        }
        // 4. 更新请求头内容
        ServerHttpRequest request = exchange.getRequest().mutate().headers(item -> {
            item.putAll(headerMap);
        }).build();
        return chain.filter(exchange.mutate().request(request).build()).doFinally(signal -> {
            MDC.remove("requestId");
        });
    }

    /**
     * 授权校验.
     */
    private void checkAuthorize(String path) {
        // 1. appid合法校验
        String appid = CurrentUser.getAppkey();
        if (StringUtil.isNull(appid)) {
            // 没传appid时，交由后端的具体应用做判断
            return;
        }
        String secret = RedisUtil.getLocalCache(BaseCacheKey.APPKEY_SECRET_PREFIX + appid, String.class);
        if (StringUtil.isNull(secret)) {
            throw new AppkeyInvalidException();
        }
        // 2. 权限判断（只做基础校验，具体判断挪到应用层）
        // 2.1 免登陆校验（如登录、获取验证码、下载等接口）
        if (path.endsWith("/login") || path.endsWith("/captcha")) {
            log.debug("[{}]登录接口.", path);
            return;
        }
        boolean accept = RedisUtil.existSetItem(BaseCacheKey.GLOBAL_PERMISSION, path);
        if (accept) {
            log.debug("[{}]全局免签.", path);
            return;
        }
        // 2.2 其他接口都需要登录后调用
        String token = CurrentUser.getToken();
        if (StringUtil.isNull(token)) {
            throw new TokenInvalidException();
        }
//        // 2.2 若无token
//        // 一是第三方发起的调用，此时跟用户无关，仅需判断应用的权限
//        // 二是token丢失，前端需要重新登录
//        if (StringUtil.isNull(token)) {
//            accept = RedisUtil.existSetItem(BaseCacheKey.APPKEY_PERMISSION_PREFIX + appid, path);
//            if (accept) {
//                log.debug("[{}]独立授权[{}].", path, appid);
//                return;
//            }
//            // 为了前端处理方便，此处统一返回token失效，而不是403 forbidden
//            throw new TokenInvalidException();
//        }
        // 2.3 有token，说明用户已经登录
        // 2025-03-05：此处不能用RedisUtil.getLocalCache，因为后续有setNonce等赋值操作，若从缓存获取，容易影响前一个请求
        CurrentUser.UserInfo user = CurrentUser.getInfo();
        if (StringUtil.notNull(user.getUserId())) {
            // token有对应的用户信息时，说明是用户登录的
            String loginTokenCacheKey = BaseCacheKey.USER_TOKEN_PREFIX + user.getUserId() +":"+ CurrentUser.getAppkey();
            String loginToken = RedisUtil.get(loginTokenCacheKey, String.class);
            if ((loginToken != null) && !token.equals(loginToken)) {
                RedisUtil.clearLocalCache(BaseCacheKey.USER_INFO_PREFIX + token);
                throw new AccountKickOutException();
            }
//            // 2025-11-08：不再自动刷新，改由请求方采用freshToken机制无感刷新，从而减少服务端对redis的操作，以及减少token泄露后的隐患
//            // 刷新用户的过期时间（默认30 * 60秒）
//            if (user.getExpires() != null) {
//                int timeout = (user.getExpires() <= 0) ? 1800 : user.getExpires();
//                RedisUtil.expire(BaseCacheKey.USER_INFO_PREFIX + token, timeout);
//                RedisUtil.expire(loginTokenCacheKey, timeout);
//            }
        }
        // 2.4 当调用退出、角色切换接口时，清理本地缓存的用户信息
        if (path.endsWith("/permission") || path.endsWith("/logout")) {
            // 清理本地缓存的用户信息（授权接口会重新设置缓存的用户信息）
            RedisUtil.clearLocalCache(BaseCacheKey.USER_INFO_PREFIX + token);
            // 清理本地所有缓存（从而减少定时清理的逻辑）
            RedisUtil.clearLocalCache();
        }
        // 2.5 具体权限判断（挪到应用层）
//        else if (RedisUtil.existSetItem(BaseCacheKey.GLOBAL_ALL_RESOURCE, path)) {
//            // 权限校验（登记到系统中的resource必须授权后才能访问）
//            accept = RedisUtil.existSetItem(BaseCacheKey.ROLE_PERMISSION_PREFIX + user.getCurrentRoleId(), path);
//            if (!accept) {
//                throw new ForbiddenException();
//            }
//        }
    }

    /**
     * 填充用户信息到请求头.
     * 注：有超过http请求头容量的风险，不建议使用
     */
    private void populateUserInfo2Headers(Map<String, List<String>> headerMap) {
        CurrentUser.UserInfo user = CurrentUser.getInfo();
        if (StringUtil.notNull(user.getUserId())) {
            headerMap.put(CurrentUser.USER_ID, Collections.singletonList(String.valueOf(user.getUserId())));
        }
        if (StringUtil.notNull(user.getUserName())) {
            headerMap.put(CurrentUser.USER_NAME, Collections.singletonList(URLEncoder.encode(user.getUserName(), StandardCharsets.UTF_8)));
        }
        if (StringUtil.notNull(user.getRealName())) {
            headerMap.put(CurrentUser.USER_REAL_NAME, Collections.singletonList(URLEncoder.encode(user.getRealName(), StandardCharsets.UTF_8)));
        }
        if (StringUtil.notNull(user.getNickName())) {
            headerMap.put(CurrentUser.USER_NICK_NAME, Collections.singletonList(URLEncoder.encode(user.getNickName(), StandardCharsets.UTF_8)));
        }
        if (StringUtil.notNull(user.getCurrentRoleId())) {
            headerMap.put(CurrentUser.USER_ROLE_ID, Collections.singletonList(String.valueOf(user.getCurrentRoleId())));
        }
        // 机构ID有可能是APPKEY的，所以挪到了populateOtherInfo2Headers中填充
//        if (StringUtil.notNull(user.getInstituteId())) {
//            headerMap.put(CurrentUser.USER_INSTITUTE_ID, Collections.singletonList(String.valueOf(user.getInstituteId())));
//        }
        if (StringUtil.notNull(user.getOfficeId())) {
            headerMap.put(CurrentUser.USER_OFFICE_ID, Collections.singletonList(String.valueOf(user.getOfficeId())));
        }
        if ((user.getOfficeList() != null) && !user.getOfficeList().isEmpty()) {
            headerMap.put(CurrentUser.USER_OFFICE_LIST, Collections.singletonList(JsonUtil.serialize(user.getOfficeList())));
        }
        if ((user.getGroupList() != null) && !user.getGroupList().isEmpty()) {
            headerMap.put(CurrentUser.USER_GROUP_LIST, Collections.singletonList(JsonUtil.serialize(user.getGroupList())));
        }
        if ((user.getRoleList() != null) && !user.getRoleList().isEmpty()) {
            headerMap.put(CurrentUser.USER_ROLE_LIST, Collections.singletonList(JsonUtil.serialize(user.getRoleList())));
        }
    }

    /**
     * 填充用户信息到请求头.
     */
    private void populateOtherInfo2Headers(Map<String, List<String>> headerMap, String sign, String lan) {
        // 补充请求头内容
        if (StringUtil.notNull(CurrentUser.getAppType())) {
            headerMap.put(CurrentUser.APP_TYPE, Collections.singletonList(CurrentUser.getAppType()));
        }
        if (StringUtil.notNull(CurrentUser.getInfo().getInstituteId())) {
            headerMap.put(CurrentUser.USER_INSTITUTE_ID, Collections.singletonList(String.valueOf(CurrentUser.getInfo().getInstituteId())));
        }
        // 补充其他信息（语言及签名)
        headerMap.put(CurrentUser.CLIENT_IP, Collections.singletonList(CurrentUser.getClientIp()));
        headerMap.put(CurrentUser.TRACE_ID,  Collections.singletonList(CurrentUser.getTraceId()));
        headerMap.put(CurrentUser.SIGN_APP,  Collections.singletonList(sign));
        headerMap.put(CurrentUser.USER_LANGUAGE, Collections.singletonList(lan));
        // 补充网关签名
        String globalSecret = getGlobalSecret();
        String timestamp    = String.valueOf(Clock.systemUTC().millis());
        String globalSign   = GatewayUtil.sign(sign + timestamp, globalSecret);
        headerMap.put(CurrentUser.GATEWAY_SIGN, Collections.singletonList(globalSign));
        headerMap.put(CurrentUser.GATEWAY_TIME, List.of(timestamp));
        if (log.isDebugEnabled()) {
            log.debug("网关签名原文[{}]，签名密文[{}]", sign + timestamp, globalSign);
        }
    }

    /**
     * 获取全局密钥.
     */
    private String getGlobalSecret() {
        String secret = RedisUtil.getLocalCache(BaseCacheKey.GLOBAL_SECRET, String.class);
        if (StringUtil.isNull(secret)) {
            throw new AppkeyInvalidException();
        }
        return secret;
    }

}
