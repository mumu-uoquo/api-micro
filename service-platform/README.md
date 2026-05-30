# service-platform · 运营平台

运营平台核心业务服务，提供用户、角色、机构、消息、系统设置、文件、鉴权、日志等能力。

- 服务名：`service-platform`
- 默认端口：`7220`
- 上下文路径：`/health/api/platform`

## 业务模块

| 业务域 | 说明 | 主要接口前缀 |
| --- | --- | --- |
| auth | 登录鉴权、TOTP 双因子绑定 / 校验、Token 管理 | `/v1/auth` |
| user | 用户管理、用户资料、用户配置、用户分组 | `/v1/user`、`/v1/user/profile`、`/v1/user/settings`、`/admin/v1/user` |
| role | 角色、模块（菜单 / 按钮）、资源、权限 | `/v1/role`、`/v1/module`、`/admin/v1/role` |
| institute | 机构、部门、区域、机构设置 | `/v1/institute`、`/v1/department`、`/v1/institute/settings`、`/admin/v1/institute` |
| message | 站内消息、消息模板、推送日志、SSE 实时推送 | `/v1/message/manage`、`/v1/message/template`、`/v1/message/view`、`/v1/message/send`、`/v1/message/sse`、`/admin/v1/message` |
| system | 应用信息、字典、节假日、系统设置、返回码 | `/v1/system/appinfo`、`/v1/system/dictionary`、`/v1/system/holiday`、`/v1/system/settings`、`/v1/system/return-code` |
| logs | 登录日志、业务日志、在线用户、业务事件记录 | `/v1/logs`、`/admin/v1/logs` |
| dfs | 文件上传 / 下载（分块、Base64、断点续传） | `/v1/file/upload`、`/v1/file/download` |

> 接口前缀均基于上下文路径 `/health/api/platform`，经网关 `/health/api/platform/**` 路由转发。

## 技术要点

- **数据访问**：MySQL + Druid 连接池 + MyBatis，`@MapperScan` 扫描 `com.uoquo.**.mapper`，Mapper XML 与接口同包存放。
- **缓存预热**：启动时（`PlatformApplication.run`）异步将应用信息、应用授权、角色授权、全量资源 URL 写入 Redis，供网关鉴权使用。
- **事件总线**：通过 Kafka + Spring Cloud Bus 发布 / 消费业务事件（用户变更、消息推送等）。
- **实时推送**：基于 SSE（Server-Sent Events）的消息订阅与推送（`ServerSentEventsController`）。
- **鉴权安全**：TOTP 双因子（`TotpAuthUtils`）、敏感字段脱敏（`@SensitiveData` / `@SensitiveField`）。
- **接口文档**：集成 SpringDoc OpenAPI，`app.swagger.enabled=true` 时开启 Swagger UI。
- **限流熔断**：可选集成 Sentinel（`SENTINEL_ENABLED=true` 开启）。

## 依赖说明

- `com.uoquo:cloud-core`：内部微服务基础库（统一验参、统一事务、服务间调用、事件等）。
- `spring-cloud-starter-bus-kafka`：消息总线。
- `spring-cloud-starter-alibaba-nacos-discovery/config`：注册与配置中心。
- `spring-cloud-starter-alibaba-sentinel`：流控（可选）。
- `mysql-connector-j` + `druid-spring-boot-starter` + `mybatis-spring-boot-starter`：数据访问。
- `springdoc-openapi-starter-webmvc-ui`：接口文档。

## 配置与运行

配置分两层：`bootstrap.yml`（应用名、端口、Nacos、Redis、DB、Kafka、线程池、连接池等个性化配置，支持环境变量覆盖）与 `application.yml`（Web 容器、数据源、Kafka、Feign、MyBatis、Swagger 等通用配置）。

```bash
# 打包
mvn clean package -pl service-platform

# 运行
java -jar service-platform/target/service-platform.jar
```

依赖中间件：Nacos、Redis、Kafka、MySQL。

## 注意事项

### 多节点事件消费组

多节点部署时，`app.kafka.default.bus-group` 每个节点需由默认的相同值改为不一样的值，以便 SSE 推送在各节点都能消费到事件（默认值为 `服务名-bus`）。

```text
app.kafka.default.bus-group  →  每节点设置为不同值（如 service-platform-bus-1、service-platform-bus-2）
```

工程整体说明见根目录 [README](../README.md)。
