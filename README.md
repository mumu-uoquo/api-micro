# micro-service

uoquo 健康平台微服务工程，基于 Spring Boot 3.5 + Spring Cloud 2025 + Spring Cloud Alibaba 构建。工程采用 Maven 多模块聚合，统一由网关对外提供入口，通过 Nacos 做注册与配置中心，Kafka（Spring Cloud Bus）做事件总线，Redis 做缓存与限流。

## 技术栈

| 类别 | 选型 |
| --- | --- |
| JDK | Java 21 |
| 框架 | Spring Boot 3.5.x、Spring Cloud 2025.0.x、Spring Cloud Alibaba 2025.x |
| 注册 / 配置中心 | Nacos |
| 网关 | Spring Cloud Gateway（WebFlux） |
| 服务调用 | OpenFeign + LoadBalancer（含自定义 IP Hash 负载均衡） |
| 事件总线 | Kafka + Spring Cloud Bus |
| 缓存 / 限流 | Redis（单点 / 哨兵 / 集群三模式，优先级：哨兵 > 集群 > 单点） |
| 数据库 | MySQL + Druid 连接池 + MyBatis |
| 限流 / 熔断 | Sentinel（service-platform 可选开启） |
| 接口文档 | SpringDoc OpenAPI（Swagger UI） |
| 内部依赖 | `com.uoquo:dependencies` BOM 1.1.0（含 cloud-core、app-core、utils-basic 等） |

## 模块结构

```text
micro-service (pom)
├── api-gateway        微服务网关（鉴权、限流、负载均衡、全链路日志、统一异常）
├── service-platform   运营平台（用户、角色、机构、消息、系统设置、文件、鉴权、日志）
└── scheduler-center   调度中心（事件消息消费、定时任务调度）
```

各模块统一继承父 BOM `com.uoquo:dependencies:1.1.0`，由其统一管理依赖版本、打包插件与多环境 profile。

### api-gateway · 微服务网关

系统统一入口，端口默认 `7210`，服务名 `gateway`。核心能力：

- **全局鉴权**：`AuthorizeFilter` 作为全局过滤器，校验请求头必传参数、appid 合法性、登录态（token），并把解析出的用户信息透传到下游服务请求头；对登录、验证码、文件上传下载等接口做免签 / 免登放行。
- **请求限流**：基于 Redis 的令牌桶限流（`GatewayRequestRateLimiter`），默认每用户每秒平均 10 次、峰值 15 次，限流维度可按 token / IP / path 切换。
- **负载均衡**：支持标准 `lb://` 负载，以及自定义 `iphash://` IP Hash 负载（同一来源 IP 路由到固定实例）。
- **全链路日志**：`LoggingFilter` 记录请求 / 响应数据，结合 MDC 传递 requestId。
- **统一异常**：`GlobalExceptionHandler` 统一封装网关层异常响应。
- **跨域**：全局 CORS 配置，允许的来源、方法、头信息可通过环境变量调整。

路由在 `application.yml` 中配置，例如 `/health/api/platform/**` 转发到 `service-platform`。

### service-platform · 运营平台

核心业务服务，端口默认 `7220`，服务名 `service-platform`，上下文路径 `/health/api/platform`。按业务域划分模块：

| 业务域 | 说明 | 主要接口前缀 |
| --- | --- | --- |
| auth | 登录鉴权、TOTP 双因子、Token 管理 | `/v1/auth` |
| user | 用户管理、用户资料、用户配置、分组 | `/v1/user`、`/v1/user/profile`、`/v1/user/settings`、`/admin/v1/user` |
| role | 角色、模块、资源、权限 | `/v1/role`、`/v1/module`、`/admin/v1/role` |
| institute | 机构、部门、区域、机构设置 | `/v1/institute`、`/v1/department`、`/admin/v1/institute` |
| message | 站内消息、模板、推送、SSE 实时推送 | `/v1/message/*`、`/admin/v1/message` |
| system | 应用信息、字典、节假日、系统设置、返回码 | `/v1/system/*` |
| logs | 登录日志、业务日志、在线用户、事件记录 | `/v1/logs`、`/admin/v1/logs` |
| dfs | 文件上传 / 下载（分块、Base64） | `/v1/file/upload`、`/v1/file/download` |

技术要点：

- MyBatis + Druid 数据源，`MapperScan` 扫描 `com.uoquo.**.mapper`。
- 启动时预热缓存：应用信息、应用授权、角色授权、全量资源 URL 写入 Redis。
- 通过 Kafka / Spring Cloud Bus 发布与消费业务事件。
- SSE（Server-Sent Events）实现消息实时推送。
- 集成 SpringDoc OpenAPI，`app.swagger.enabled=true` 时开启文档。

### scheduler-center · 调度中心

非 Web 应用（`web-application-type: none`），服务名 `scheduler-center`。核心能力：

- **事件消费**：监听 Spring Cloud Bus（Kafka）上的远程事件，包含鉴权事件、消息事件等监听器（`AllEventListener`、`AuthEventListener`、`MessageInfoEventListener`）。
- **远程调用**：通过 OpenFeign 调用 `service-platform`（`UserRemoteService`、`MessageRemoteService`、`LogsRemoteService`），并使用自定义请求头拦截器与 IP Hash 负载均衡。
- **定时任务**：`DemoTask` 等定时任务骨架（如临时文件清理、过期消息清理）。
- 同样使用 MySQL + Druid + MyBatis 访问数据。

## 环境依赖

运行整套服务需要以下中间件（默认指向 `dev.xuziu.com`，可通过环境变量覆盖）：

- Nacos（注册 + 配置中心）
- Redis（哨兵 / 集群 / 单点）
- Kafka（事件总线）
- MySQL（`service-platform`、`scheduler-center` 需要）

## 配置说明

每个模块的配置分为两层：

- `bootstrap.yml`：个性化配置，优先加载，定义应用名、端口、Nacos、Redis、DB、Kafka、线程池等，全部支持环境变量覆盖（形如 `${ENV_KEY:default}`）。
- `application.yml`：通用配置，可提取到 Nacos 配置中心，包含 Web 容器、数据源、Kafka、Feign、MyBatis、Swagger 等。

常用环境变量（节选，均有默认值）：

| 变量 | 含义 |
| --- | --- |
| `SERVICE_NAME` / `SERVICE_PORT` | 服务名 / 端口 |
| `NACOS_HOST` / `NACOS_NAMESPACE` / `NACOS_USER` / `NACOS_PSWD` | Nacos 地址、命名空间、账号密码 |
| `REDIS_SENTINEL` / `REDIS_CLUSTER` / `REDIS_HOST` / `REDIS_PASSWORD` | Redis 哨兵 / 集群 / 单点地址与密码 |
| `KAFKA_HOST` / `KAFKA_GROUP` / `KAFKA_BUS_TOPIC` / `KAFKA_BUS_GROUP` | Kafka 地址与消费组、总线主题 |
| `DB_HOST` / `DB_PORT` / `DB_NAME` / `DB_USER` / `DB_PSWD` | 数据库连接信息 |
| `CORS_ORIGINS` | 网关允许的跨域来源 |
| `LIMITER_RATE` / `LIMITER_MAX` | 网关限流速率 / 峰值 |
| `SENTINEL_ENABLED` / `SENTINEL_HOST` | Sentinel 开关与控制台地址 |

> 注意（service-platform 多节点部署）：`app.kafka.default.bus-group` 每个节点需改为不同值，以保证 SSE 推送在各节点都能消费到事件。

## 构建与运行

### 环境要求

- JDK 21
- Maven 3.9+
- 可访问内部 Maven 仓库（`com.uoquo:*` 依赖）

### 编译打包

```bash
# 全部模块
mvn clean package

# 单个模块
mvn clean package -pl api-gateway
```

可通过 `-P` 指定环境 profile（`dev` / `test` / `demo` / `prod`），由 BOM 中的 profile 规则按 `src/main/resources/system-*.properties` 自动激活。

### 运行

```bash
# 直接运行（开发态）
mvn spring-boot:run -pl service-platform

# 打包后运行
java -jar service-platform/target/service-platform.jar
```

启动建议顺序：Nacos / Redis / Kafka / MySQL 就绪 → service-platform → scheduler-center → api-gateway。

## 构建注意事项

父 BOM 配置了 `git-commit-id-maven-plugin`，会在构建时读取 `.git` 的提交信息。若仓库尚无任何提交，构建会报 `Could not get HEAD Ref`。解决方式：先创建一次初始提交。

```bash
git add .
git commit -m "init"
```
