# scheduler-center · 调度中心

调度中心，负责处理事件消息消费、服务间远程调用与定时任务调度。

- 服务名：`scheduler-center`
- 应用类型：非 Web 应用（`spring.main.web-application-type=none`）

## 核心功能

### 事件消息消费

监听 Spring Cloud Bus（Kafka）上的远程事件并处理：

- `AllEventListener`：通用事件监听。
- `AuthEventListener`：鉴权相关事件（如登录 / 登出日志）。
- `MessageInfoEventListener`：消息相关事件（接收人补全、推送日志等）。

消费失败时通过死信队列（DLQ）兜底，反序列化失败由 `DeserializationFailureHandler` 处理。

### 服务间远程调用（OpenFeign）

通过 OpenFeign 调用 `service-platform` 的内部接口（路径 `/health/api/platform`）：

- `UserRemoteService`：用户信息查询。
- `MessageRemoteService`：消息接收人新增、推送日志记录。
- `LogsRemoteService`：登录 / 登出日志、业务事件记录。

调用链使用自定义请求头拦截器（`SchedulerFeignHeaderInterceptor`，继承自 `FeignHeaderInterceptor`）补充内部调用所需的鉴权头，并对 `service-platform` 启用 IP Hash 负载均衡（`IphashLoadBalancerConfiguration`）。

### 定时任务

`com.uoquo.scheduler.platform.task` 下存放定时任务（如 `DemoTask`，预留临时文件清理、过期消息清理等）。

## 数据访问

使用 MySQL + Druid 连接池 + MyBatis（`MybatisDataSourcePrimaryConfig` 配置主数据源），`@MapperScan` 扫描 `com.uoquo.**.mapper`。

## 依赖说明

- `com.uoquo:cloud-core`：内部微服务基础库，传递引入 OpenFeign、LoadBalancer、事件等能力。
- `spring-boot-starter`：基础启动器（非 Web）。
- `spring-cloud-starter-bus-kafka`：消息总线。
- `spring-cloud-starter-alibaba-nacos-discovery/config`：注册与配置中心。
- `mysql-connector-j` + `druid-spring-boot-starter` + `mybatis-spring-boot-starter`：数据访问。

## 配置与运行

配置分两层：`bootstrap.yml`（应用名、Nacos、Redis、DB、Kafka、线程池、连接池等个性化配置，支持环境变量覆盖）与 `application.yml`（消息总线、数据源、Kafka、Feign、MyBatis 等通用配置）。

```bash
# 打包
mvn clean package -pl scheduler-center

# 运行
java -jar scheduler-center/target/scheduler-center.jar
```

依赖中间件：Nacos、Redis、Kafka、MySQL。需在 `service-platform` 启动后运行（Feign 调用依赖其接口）。

工程整体说明见根目录 [README](../README.md)。
