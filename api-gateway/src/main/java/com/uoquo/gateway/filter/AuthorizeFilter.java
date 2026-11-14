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
import com.uoquo.gateway.utils.GlobalPermissionMatcher;
import com.uoquo.utils.SignParamUtil;
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
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

/**
 * 描述：全局鉴权拦截器. <br>
 * 职责：微服务模式下的登录和权限校验（应用层不再校验登录），并给转发的所有请求加上网关签名，
 *      应用层据此确认请求是由网关转发的；参数签名、时间戳、防重提交等校验仍在应用层完成. <br>
 * 参考：https://segmentfault.com/a/1190000016227780 <br>
 * 日期：2018-01-18 16:28 <br>
 * 变更：
 * <pre>
 * Version      Date           ModifiedBy       Content
 * --------     ----------     ------------     -----------------------
 * 1.0          2018-01-18     xuhz.           创建
 * 2.0          2026-11-13     xuhz.           网关负责完整的登录和权限校验；免登录URL完全由应用登记的
 *                                              {@code GLOBAL_PERMISSION} 决定；网关签名对所有转发的请求生效，
 *                                              并清理客户端伪造的内部请求头
 * </pre>
 * @since   JDK 1.8
 * @version 2.0
 * @author  uoquo team
 */
@Component // 可以用扫描的方式注入，也可以在配置中使用@bean注入
public class AuthorizeFilter implements GlobalFilter, Ordered {

    private final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * 只能由网关设置的请求头：客户端传入的这些请求头一律丢弃，防止伪造用户信息或网关签名.
     */
    private static final List<String> INTERNAL_HEADERS = List.of(
            CurrentUser.USER_ID, CurrentUser.USER_NAME, CurrentUser.USER_REAL_NAME, CurrentUser.USER_NICK_NAME,
            CurrentUser.USER_INSTITUTE_ID, CurrentUser.USER_OFFICE_ID, CurrentUser.USER_ROLE_ID,
            CurrentUser.USER_ROLE_LIST, CurrentUser.USER_OFFICE_LIST, CurrentUser.USER_GROUP_LIST,
            CurrentUser.APP_TYPE, CurrentUser.CLIENT_IP, CurrentUser.TRACE_ID,
            CurrentUser.GATEWAY_SIGN, CurrentUser.GATEWAY_TIME, CurrentUser.FEIGN_SIGN);

    @Value("${app.language:zh-cn}")
    private String DEFAULT_LANGUAGE;

    private final GlobalPermissionMatcher permissionMatcher;

    public AuthorizeFilter(GlobalPermissionMatcher permissionMatcher) {
        this.permissionMatcher = permissionMatcher;
    }

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
        // OPTIONS 请求（跨域预检）：网关直接返回成功，不再转发给应用层。
        // 跨域响应头已经由网关的 globalcors 配置在路由匹配时写入；预检请求不携带任何认证信息，因此不做鉴权，也不消耗限流令牌
        if (HttpMethod.OPTIONS.equals(exchange.getRequest().getMethod())) {
            exchange.getResponse().setStatusCode(HttpStatus.OK);
            return exchange.getResponse().setComplete();
        }
        // 从redis中获取当前用户信息
        GatewayUtil.parseInfo4Request(exchange);
        // 将用户信息缓存到exchange，方便后续filter使用
        GatewayUtil.putInfo2Attributes(exchange.getAttributes());
        // 1. 常规信息
        HttpHeaders header = exchange.getRequest().getHeaders();
        Map<String, String> queryParams = exchange.getRequest().getQueryParams().toSingleValueMap();
        String sign = GatewayUtil.getHeader(header, queryParams, CurrentUser.SIGN_APP);
        String lan  = GatewayUtil.getHeader(header, queryParams, CurrentUser.USER_LANGUAGE);
        String path = exchange.getRequest().getPath().toString();
        // 无签名的请求（免签的接口，如文件下载）：补充默认语言。
        // 有签名的请求，语言参与了签名计算，不可以替换，缺失时由应用层校验签名时报错
        if (StringUtil.isNull(sign) && StringUtil.isNull(lan)) {
            lan = DEFAULT_LANGUAGE;
        }
        // 2. 登录和权限校验
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
        // 4. 更新请求头内容（先清理客户端伪造的内部请求头）
        ServerHttpRequest request = exchange.getRequest().mutate().headers(item -> {
            INTERNAL_HEADERS.forEach(item::remove);
            item.putAll(headerMap);
        }).build();
        return chain.filter(exchange.mutate().request(request).build()).doFinally(signal -> {
            MDC.remove("requestId");
        });
    }

    /**
     * 登录和权限校验.<br>
     * 与单体应用的 CheckLoginInterceptor 保持一致，微服务的应用层不再校验登录。
     */
    private void checkAuthorize(String path) {
        // 1. appid合法校验
        // 对于传入了appid的请求，需优先校验合法性，防止透传增加应用负担
        String appid = CurrentUser.getAppkey();
        if (StringUtil.notNull(appid)) {
            String secret = RedisUtil.getLocalCache(BaseCacheKey.APPKEY_SECRET_PREFIX + appid, String.class);
            if (StringUtil.isNull(secret)) {
                throw new AppkeyInvalidException();
            }
        }
        // 2. 免登录校验（如登录、获取验证码、下载等接口）
        // 完全由应用中标注的 @IgnoreAuth(all/login) 决定，各应用启动后会登记到 GLOBAL_PERMISSION
        if (permissionMatcher.matches(path)) {
            log.debug("[{}]免登录接口.", path);
            return;
        }
        // 3. 其他接口都需要登录后调用
        if (StringUtil.isNull(appid)) {
            throw new AppkeyEmptyException();
        }
        String token = CurrentUser.getToken();
        if (StringUtil.isNull(token)) {
            throw new TokenEmptyException();
        }
        // 3.1 应用授权判断（第三方应用先获取token，再访问授权的URL）
        String appToken = RedisUtil.get(BaseCacheKey.APPKEY_TOKEN_PREFIX + appid, String.class);
        if (StringUtil.notNull(appToken) && token.equals(appToken)) {
            if (!RedisUtil.existSetItem(BaseCacheKey.APPKEY_PERMISSION_PREFIX + appid, path)) {
                throw new ForbiddenException();
            }
            log.debug("[{}]独立授权[{}].", path, appid);
            return;
        }
        // 3.2 用户授权判断
        // 2025-03-05：此处不能用RedisUtil.getLocalCache，因为后续有setNonce等赋值操作，若从缓存获取，容易影响前一个请求
        CurrentUser.UserInfo user = CurrentUser.getInfo();
        if (user == null || StringUtil.isNull(user.getUserId())) {
            // 有token，却无用户信息，说明token已失效
            throw new TokenInvalidException();
        }
        if (user.isOpsMode()) {
            // 运维模式时，需打印日志（userName一般为运维人员的手机号，不是userId的实际账号名）
            log.warn("运维人员[{}]以账户[{}]的角色[{}]访问[{}].", user.getUserName(), user.getUserId(), user.getCurrentRoleId(), path);
        } else {
            // 标准模式时，需要判断token的有效性（单点登录，是否被踢下线）
            String loginTokenCacheKey = BaseCacheKey.USER_TOKEN_PREFIX + user.getUserId() + ":" + appid;
            String loginToken = RedisUtil.get(loginTokenCacheKey, String.class);
            if (StringUtil.isNull(loginToken)) {
                log.warn("用户[{}]缓存的登录token为空，用当前token[{}]补充.", user.getUserId(), token);
                RedisUtil.put(loginTokenCacheKey, token, user.getExpires());
            } else if (!token.equals(loginToken)) {
                log.debug("用户[{}]的请求token[{}]不是最新的登录token[{}]，所以当前的客户端已经被踢下线.", user.getUserId(), token, loginToken);
                RedisUtil.clearLocalCache(BaseCacheKey.USER_INFO_PREFIX + token);
                throw new AccountKickOutException();
            }
        }
        // 3.3 用户权限判断
        if (path.endsWith("/permission") || path.endsWith("/logout")) {
            // 退出、角色切换（授权）接口：清理本地缓存的用户信息（授权接口会重新设置缓存的用户信息）
            RedisUtil.clearLocalCache(BaseCacheKey.USER_INFO_PREFIX + token);
            // 清理本地所有缓存（从而减少定时清理的逻辑）
            RedisUtil.clearLocalCache();
        } else if (RedisUtil.existSetItem(BaseCacheKey.GLOBAL_ALL_RESOURCE, path)) {
            // 登记到系统中的resource必须授权后才能访问
            boolean accept = RedisUtil.existSetItem(BaseCacheKey.ROLE_PERMISSION_PREFIX + user.getCurrentRoleId(), path);
            if (!accept) {
                throw new ForbiddenException();
            }
        }
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
        if (StringUtil.notNull(user.getInstituteId())) {
            headerMap.put(CurrentUser.USER_INSTITUTE_ID, Collections.singletonList(String.valueOf(user.getInstituteId())));
        }
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
     * 填充其他信息到请求头（含网关签名）.
     */
    private void populateOtherInfo2Headers(Map<String, List<String>> headerMap, String sign, String lan) {
        // 补充请求头内容
        if (StringUtil.notNull(CurrentUser.getAppType())) {
            headerMap.put(CurrentUser.APP_TYPE, Collections.singletonList(CurrentUser.getAppType()));
        }
        // 补充其他信息（语言及签名)，没有值的不传，避免下游收到字面量的 null
        if (StringUtil.notNull(CurrentUser.getClientIp())) {
            headerMap.put(CurrentUser.CLIENT_IP, Collections.singletonList(CurrentUser.getClientIp()));
        }
        if (StringUtil.notNull(CurrentUser.getTraceId())) {
            headerMap.put(CurrentUser.TRACE_ID, Collections.singletonList(CurrentUser.getTraceId()));
        }
        if (StringUtil.notNull(sign)) {
            headerMap.put(CurrentUser.SIGN_APP, Collections.singletonList(sign));
        }
        if (StringUtil.notNull(lan)) {
            headerMap.put(CurrentUser.USER_LANGUAGE, Collections.singletonList(lan));
        }
        // 补充网关签名：所有转发的请求都必须有，应用层据此确认请求是由网关转发的。
        // 签名 = sign(客户端签名 + 网关时间, 全局密钥)，没有客户端签名（免签的接口）时，客户端签名按空串计算
        String globalSecret = getGlobalSecret();
        String timestamp    = String.valueOf(Clock.systemUTC().millis());
        String signText     = StringUtil.isNull(sign) ? "" : sign;
        String globalSign   = SignParamUtil.sign(signText + timestamp, globalSecret);
        if (StringUtil.isNull(globalSign)) {
            // 签名失败时不能继续转发，否则应用层无法识别请求来源
            throw new AppkeyInvalidException();
        }
        headerMap.put(CurrentUser.GATEWAY_SIGN, Collections.singletonList(globalSign));
        headerMap.put(CurrentUser.GATEWAY_TIME, List.of(timestamp));
        if (log.isDebugEnabled()) {
            log.debug("网关签名原文[{}]，签名密文[{}]", signText + timestamp, globalSign);
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
