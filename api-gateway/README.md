# api-gateway · 微服务网关

基于 Spring Cloud Gateway（WebFlux）构建的微服务统一入口，提供请求鉴权、限流、IP Hash 负载均衡、全链路日志及统一异常处理能力。

- 服务名：`gateway`
- 默认端口：`7210`
- 应用类型：响应式（WebFlux）

## 核心功能

### 全局鉴权（AuthorizeFilter）

作为最高优先级的全局过滤器（`HIGHEST_PRECEDENCE + 100`），对所有请求统一处理：

- 从请求头 / URL 参数及 Redis 中解析当前用户信息（`GatewayUtil.parseInfo4Request`）。
- 校验必传参数、appid 合法性（`APPKEY_SECRET_PREFIX`）、登录态 token。
- 登录、验证码接口免登放行；通过 `GLOBAL_UNSIGNED` / `GLOBAL_PERMISSION` 集合配置免签、免登 URL（如文件上传下载）。
- 校验单点登录冲突（token 与缓存不一致时踢出，`AccountKickOutException`）。
- 退出 / 切换角色接口访问时清理本地缓存。
- 将解析出的用户信息（userId、userName、roleId、机构、语言、客户端 IP、traceId 等）透传到下游服务请求头，并补充网关签名（`GATEWAY_SIGN` / `GATEWAY_TIME`）。

### 请求限流（GatewayRequestRateLimiter）

基于 Redis 的令牌桶限流，自定义的 `GatewayRequestRateLimiterGatewayFilterFactory` 作为全局默认过滤器：

- 默认每用户每秒平均 `10` 次（`LIMITER_RATE`）、峰值 `15` 次（`LIMITER_MAX`）。
- 限流维度可在 token / IP / path 之间切换（默认 `tokenKeyResolver`）。
- 触发限流后返回码可配置（当前配置为 `200`）。

### 负载均衡

- 标准负载：`lb://service-name`，从 Nacos 注册中心拉取实例。
- 自定义 IP Hash 负载：`iphash://service-name`，由 `IphashLoadBalancer` + `IphashLoadBalancerFilter` 实现，将同一来源 IP 路由到固定实例（适合有状态 / 会话粘连场景）。

### 全链路日志与统一异常

- `LoggingFilter` 记录请求 / 响应数据，结合 MDC 传递 `requestId`。
- `GlobalExceptionHandler` 统一封装网关层异常为标准 `ReturnData` 响应。

### 跨域（CORS）

全局 CORS 配置，允许来源（`CORS_ORIGINS`）、方法、头信息可调；已放开 SSE 所需的 `Last-Event-ID` 等头信息。

## 路由配置

路由在 `application.yml` 的 `spring.cloud.gateway.server.webflux.routes` 中定义。示例：

| 路由 ID | 转发目标 | 匹配路径 |
| --- | --- | --- |
| service-platform | `lb://service-platform` | `/health/api/platform/**` |
| demo-cloud-book | `iphash://demo-cloud-book` | `/api/book/**` |

> Spring Cloud Gateway 2025.0+ 配置前缀已由 `spring.cloud.gateway` 变更为 `spring.cloud.gateway.server.webflux`。

## 依赖说明

- `spring-cloud-starter-gateway-server-webflux`：网关核心。
- `spring-cloud-starter-loadbalancer`：负载均衡。
- `spring-boot-starter-data-redis-reactive`：响应式 Redis（限流、鉴权缓存）。
- `spring-cloud-starter-alibaba-nacos-discovery/config`：注册与配置中心。
- `com.uoquo:utils-basic`：内部基础工具库。

## 配置与运行

配置分两层：`bootstrap.yml`（应用名、端口、Nacos、Redis 等个性化配置，支持环境变量覆盖）与 `application.yml`（网关路由、限流、CORS 等通用配置）。

```bash
# 打包
mvn clean package -pl api-gateway

# 运行
java -jar api-gateway/target/api-gateway.jar
```

依赖中间件：Nacos、Redis。需在 `service-platform` 等下游服务启动后再启动网关。

工程整体说明见根目录 [README](../README.md)。
