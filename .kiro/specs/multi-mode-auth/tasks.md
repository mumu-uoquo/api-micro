# Implementation Plan: multi-mode-auth

## Overview

本计划将多模式认证改造分解为若干渐进式编码步骤，涵盖：凭证表建立、删除废弃查询方法、账号登录重构、短信码登录、第三方凭证登录与凭证绑定，以及 `sendPhoneCaptcha` 签名更新。每一步均可独立验证，最终通过 Controller 层将全部链路串联。

## Tasks

- [x] 1. 建立凭证表 DDL 与 MyBatis 基础设施
  - [x] 1.1 编写 `bko_user_credential` 建表 SQL 脚本
    - 在 `service-platform` 的数据库迁移目录（或 `resources/sql/`）新建迁移脚本
    - 包含字段：`id`、`user_id`、`credential_type`、`credential_value`、`institute_id`、`create_time`、`update_time`、`delete_state`
    - 建立联合唯一索引 `uk_type_value_inst(credential_type, credential_value, institute_id)`
    - 建立普通索引 `idx_user_id(user_id)`
    - _Requirements: 1.1, 1.2, 1.3, 1.4_

  - [x] 1.2 新建 `UserCredential` POJO
    - 路径：`user/model/pojo/UserCredential.java`
    - 字段与建表 DDL 一一对应，包含 getter/setter
    - _Requirements: 1.1_

  - [x] 1.3 新建 `UserCredentialMapper` 接口与 XML
    - 接口路径：`user/mapper/UserCredentialMapper.java`，包含 `selectByCredentialType` 和 `upsertCredential` 两个方法
    - XML 路径：`resources/mapper/UserCredentialMapper.xml`
    - `upsertCredential` 使用 `INSERT ... ON DUPLICATE KEY UPDATE`
    - `selectByCredentialType` 需处理 `instituteId` 为 `null` 时的 `IS NULL` 判断
    - _Requirements: 1.1, 1.2, 1.3_

  - [ ]* 1.4 编写 `CredentialMapperIT` 集成测试
    - 测试 `selectByCredentialType`：全局类型（`instituteId=null`）与机构类型
    - 测试 `upsertCredential` 幂等性：重复 upsert 同一凭证仅更新不插入新行
    - _Requirements: 1.2, 1.3_

- [x] 2. 新建枚举、Param 类与缓存 Key 常量
  - [x] 2.1 新建 `CredentialTypeEnum`
    - 路径：`auth/model/enums/CredentialTypeEnum.java`
    - 枚举值：`WEIXIN("weixin")`、`WECOM("wecom")`
    - 提供静态方法 `contains(String code)`
    - _Requirements: 6.3_

  - [x] 2.2 新建三个 Param 类
    - `SmsLoginParam`（继承 `BasicLoginParam`）：`phone`（`@NotBlank + @Pattern`）、`smsCode`（`@NotBlank`）
    - `CredentialLoginParam`（继承 `BasicLoginParam`）：`credentialType`、`credentialValue`（均 `@NotBlank`）
    - `CredentialBindParam`（继承 `BasicLoginParam`）：`account`、`password`（`@Sensitive(CRYPT_RSA)`）、`tempToken`（均 `@NotBlank`）；`captcha`（非必填）
    - _Requirements: 5.2, 6.2, 7.2_

  - [x] 2.3 在 `PlatformCacheKey` 中追加 `BIND_TEMP_TOKEN` 常量
    - 常量值：`"UOQUO:BIND_TEMP:"`
    - _Requirements: 6.6_

- [x] 3. 删除 `selectByLogin` 并重构账号登录
  - [x] 3.1 删除 `UserInfoMapper.selectByLogin`
    - 从 `UserInfoMapper.java` 删除方法声明
    - 从 `UserInfoMapper.xml` 删除对应 `<select id="selectByLogin">` 元素
    - 确保整个代码库无任何 `selectByLogin` 调用引用
    - _Requirements: 2.1, 2.3_

  - [x] 3.2 在 `AuthServiceImpl` 中新增私有方法 `findUserByAccount`
    - 判断 `account` 是否匹配 `^1[3-9]\d{9}$`
    - 匹配则调用 `userInfoMapper.selectByPhone(null, account)`，否则调用 `userInfoMapper.selectByUserName(null, account)`
    - 无结果返回 `null`
    - _Requirements: 3.1_

  - [ ]* 3.3 编写 `findUserByAccount` 属性化测试（Property 1）
    - **Property 1：findUserByAccount 路由不变性**
    - 对所有匹配 `^1[3-9]\d{9}$` 的输入，验证只调用 `selectByPhone`，从不调用 `selectByUserName`
    - 对所有不匹配的输入，验证只调用 `selectByUserName`，从不调用 `selectByPhone`
    - 使用 jqwik，最少 100 次
    - **Validates: Requirements 3.1**

  - [x] 3.4 重构 `AuthServiceImpl.userLogin`
    - 将 `userInfoMapper.selectByLogin(null, param.getAccount())` 替换为 `findUserByAccount(param.getAccount())`
    - 若返回 `null` 抛出 `ACCOUNT_PASSWORD_ERROR`
    - 密码校验、连续失败锁定、MFA 双因子、Token 生成流程保持不变
    - _Requirements: 3.2, 3.3, 3.5_

  - [ ]* 3.5 编写 `UserLoginRefactorTest` 单元测试
    - 测试手机号账号走 `selectByPhone` 分支后完成完整登录
    - 测试用户名账号走 `selectByUserName` 分支后完成完整登录
    - 测试账号不存在时抛出 `ACCOUNT_PASSWORD_ERROR`
    - _Requirements: 3.2, 3.5_

- [x] 4. Checkpoint — 确保已有测试全部通过
  - 确保所有测试通过，如有问题请向用户询问。

- [-] 5. 实现短信码登录
  - [x] 5.1 更新 `UserInfoService.sendPhoneCaptcha` 签名
    - 接口由 `sendPhoneCaptcha(String phone)` 改为 `sendPhoneCaptcha(String phone, String userId)`
    - `UserInfoServiceImpl` 实现中将 TOTP 密钥从 `Base32.encode(phone)` 改为 `Base32.encode(userId)`
    - 同步更新 `UserInfoServiceImpl.updatePhone` 中验证码校验的密钥逻辑
    - _Requirements: 5.7, 5.8_

  - [x] 5.2 更新 `AuthServiceImpl.sendPhoneCaptcha`（未登录发码）
    - 先通过 `userInfoMapper.selectByPhone(null, phone)` 获取 userId
    - 若手机号未注册则静默返回空字符串，不抛出异常
    - 再调用 `userInfoService.sendPhoneCaptcha(phone, user.getId())`
    - _Requirements: 5.9_

  - [x] 5.3 更新 `UserProfileController.sendPhoneCaptcha`（已登录发码）
    - 直接取 `CurrentUser.getInfo().getUserId()` 作为 `userId`，无需再查库
    - 调用更新后的 `userInfoService.sendPhoneCaptcha(phone, userId)`
    - _Requirements: 5.8_

  - [ ]* 5.4 编写 TOTP round-trip 属性化测试（Property 2）
    - **Property 2：TOTP 短信码 round-trip 自洽性**
    - 对任意有效 `userId` 字符串，`verifyDynamicCode(Base32.encode(userId), generateDynamicCode(Base32.encode(userId)))` 必须返回 `true`
    - 使用 jqwik，最少 100 次
    - **Validates: Requirements 5.4, 5.7**

  - [x] 5.5 在 `AuthService` 接口新增 `smsLogin` 方法，在 `AuthServiceImpl` 中实现
    - 通过 `userInfoMapper.selectByPhone(null, phone)` 查找用户；不存在则抛出 `ACCOUNT_PASSWORD_ERROR`
    - 用 `Base32.encode(userId)` 作为密钥调用 `TotpAuthUtils.verifyDynamicCode`；失败则抛出 `CAPTCHA_ERROR`
    - 跳过密码校验，执行用户状态检查、MFA 判断及 Token 生成流程
    - _Requirements: 5.1, 5.3, 5.4, 5.5, 5.6_

  - [x] 5.6 在 `AuthController` 中新增 `POST /v1/auth/phone/login` 端点
    - 标注 `@IgnoreAuth(login = true)`，参数 `@RequestBody @Valid SmsLoginParam`
    - `userAgent` 为空时从请求头自动填充
    - _Requirements: 5.1, 5.2_

  - [ ]* 5.7 编写 `SmsLoginTest` 单元测试
    - 测试正常短信码登录流程
    - 测试用户不存在时抛出 `ACCOUNT_PASSWORD_ERROR`
    - 测试短信码错误时抛出 `CAPTCHA_ERROR`
    - 测试 MFA 已启用时返回 tempToken 而非完整 Token
    - 测试 `sendPhoneCaptcha` 手机号未注册静默返回
    - _Requirements: 5.1, 5.3, 5.4, 5.5, 5.6, 5.9_

- [ ] 6. 实现第三方凭证登录
  - [x] 6.1 在 `AuthService` 接口新增 `credentialLogin` 方法，在 `AuthServiceImpl` 中实现
    - 校验 `credentialType` 是否在 `CredentialTypeEnum` 范围，不合法则抛出 `PARAM_ERROR`
    - 调用 `credentialMapper.selectByCredentialType` 查询凭证表
    - 已绑定：加载用户主信息，执行状态检查，走 Token 生成流程，返回完整 `UserAuthDto`
    - 未绑定：生成 UUID 大写 tempToken，以 `BIND_TEMP:{token}` 写入 Redis（TTL=300s），返回仅含 `accessToken=tempToken` 的 `UserAuthDto`
    - _Requirements: 6.1, 6.3, 6.4, 6.5, 6.6_

  - [ ]* 6.2 编写 credentialType 枚举校验属性化测试（Property 3）
    - **Property 3：credentialType 枚举拒绝非法值**
    - 对任意不等于 `"weixin"` 且不等于 `"wecom"` 的字符串，`credentialLogin` 必须抛出 `ParamErrorException`，且 `credentialMapper` 无任何调用
    - 使用 jqwik，最少 100 次
    - **Validates: Requirements 6.3**

  - [ ]* 6.3 编写未绑定响应结构属性化测试（Property 4）
    - **Property 4：credentialLogin 未绑定响应结构不变性**
    - 对任意合法 `(credentialType, credentialValue)` 组合，当 `credentialMapper` 返回 `null` 时，返回的 `UserAuthDto` 的 `accessToken` 非空，`userId`、`userName`、`roleList`、`groupList` 均为 `null` 或空
    - 使用 jqwik，最少 100 次
    - **Validates: Requirements 6.6**

  - [x] 6.4 在 `AuthController` 中新增 `POST /v1/auth/credential/login` 端点
    - 标注 `@IgnoreAuth(login = true)`，参数 `@RequestBody @Valid CredentialLoginParam`
    - `userAgent` 为空时从请求头自动填充
    - _Requirements: 6.1, 6.2_

  - [ ]* 6.5 编写 `CredentialLoginTest` 单元测试
    - 测试已绑定流程（返回完整 UserAuthDto）
    - 测试未绑定流程（仅返回 accessToken）
    - 测试非法 `credentialType` 时抛出 `PARAM_ERROR`
    - _Requirements: 6.1, 6.3, 6.4, 6.5, 6.6_

- [x] 7. 实现凭证绑定
  - [x] 7.1 在 `AuthService` 接口新增 `credentialBind` 方法，在 `AuthServiceImpl` 中实现
    - 从 Redis 读取 `BIND_TEMP:{tempToken}`；key 不存在或已过期则抛出 `TokenEmptyException`
    - 调用 `findUserByAccount` 查询用户，复用与 `userLogin` 相同的密码哈希校验和连续失败锁定逻辑
    - 密码验证失败时：抛出 `ACCOUNT_PASSWORD_ERROR`，不调用 `upsertCredential`，不删除 `BIND_TEMP` key
    - 密码验证成功时：调用 `credentialMapper.upsertCredential`，删除 `BIND_TEMP` key，走完整 Token 生成流程返回完整 `UserAuthDto`
    - _Requirements: 7.1, 7.3, 7.4, 7.5, 7.6_

  - [ ]* 7.2 编写密码失败安全不变量属性化测试（Property 5）
    - **Property 5：credentialBind 密码失败的安全不变量**
    - 对任意有效 `tempToken`（Redis 中存在）和任意错误密码，验证失败时 `credentialMapper.upsertCredential` 从不调用，`BIND_TEMP` key 不被删除
    - 使用 jqwik，最少 100 次
    - **Validates: Requirements 7.5**

  - [x] 7.3 在 `AuthController` 中新增 `POST /v1/auth/credential/bind` 端点
    - 标注 `@IgnoreAuth(login = true)`，参数 `@RequestBody @Valid CredentialBindParam`
    - `userAgent` 为空时从请求头自动填充
    - _Requirements: 7.1, 7.2_

  - [ ]* 7.4 编写 `CredentialBindTest` 单元测试
    - 测试 tempToken 过期/不存在时抛出 `TokenEmptyException`
    - 测试密码错误时抛出 `ACCOUNT_PASSWORD_ERROR`，不写凭证，不删除 key
    - 测试成功绑定后返回完整 `UserAuthDto`
    - 测试 upsert 幂等性：同一 `(userId, credentialType)` 重复绑定只更新不插入
    - _Requirements: 7.3, 7.4, 7.5, 7.6_

- [x] 8. 工号和三方ID认证保持不变验证
  - [x] 8.1 确认 `checkByInstitute` 相关代码无改动
    - 检查 `userInfoMapper.checkByInstitute` 调用路径，确认 Controller/Service/Mapper 均未修改
    - 确认 `bko_user.user_code` 和 `bko_user.third_id` 字段未迁移
    - _Requirements: 4.1, 4.2, 4.3_

- [x] 9. Final Checkpoint — 确保所有测试通过
  - 确保所有测试通过，如有问题请向用户询问。

## Notes

- 标注 `*` 的子任务为可选项，可跳过以加快 MVP 交付
- 每个任务均引用了具体需求条目，便于追溯
- 属性化测试使用 **jqwik** 库，每个属性至少运行 100 次
- `findUserByAccount` 为私有方法，测试时需通过包级访问或反射暴露（或在测试包中添加 `@VisibleForTesting` 辅助方法）
- `upsertCredential` 中 `id` 字段由 Service 层调用前通过 `IDGenerator.getNextULID()` 生成并传入，保持 Mapper 层幂等性
- `institute_id` 为 NULL 的唯一约束行为：建议在应用层统一将 NULL 转为占位符 `'_GLOBAL_'`，以确保 MySQL 唯一索引正常工作（详见 design.md 数据模型说明）

## Task Dependency Graph

```json
{
  "waves": [
    { "id": 0, "tasks": ["1.1", "2.1", "2.2", "2.3"] },
    { "id": 1, "tasks": ["1.2", "1.3"] },
    { "id": 2, "tasks": ["1.4", "3.1", "3.2"] },
    { "id": 3, "tasks": ["3.3", "3.4"] },
    { "id": 4, "tasks": ["3.5", "5.1", "5.2", "5.3", "8.1"] },
    { "id": 5, "tasks": ["5.4", "5.5"] },
    { "id": 6, "tasks": ["5.6", "5.7", "6.1"] },
    { "id": 7, "tasks": ["6.2", "6.3", "6.4", "6.5"] },
    { "id": 8, "tasks": ["7.1"] },
    { "id": 9, "tasks": ["7.2", "7.3"] },
    { "id": 10, "tasks": ["7.4"] }
  ]
}
```
