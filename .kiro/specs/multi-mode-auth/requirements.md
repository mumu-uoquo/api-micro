# Requirements Document

## Introduction

本功能对 `service-platform` 模块的认证系统进行多模式认证改造，目标是在不重构现有页面和主表结构的前提下，清理现有登录查询逻辑，并新增短信码登录、第三方凭证登录和凭证绑定能力，为后续扩展（微信、企业微信、APP 绑定等）预留基础设施。

**设计约束（不可变）：**

- `phone`、`user_name`、`user_code`、`third_id` 均保留在 `bko_user` 主表，不迁移到凭证表
- `bko_user_credential` 凭证表仅用于存储第三方扩展凭证（微信、企业微信等），不存储手机号、用户名、工号、三方ID
- 账号密码登录时，`account` 字段在 Service 层通过手机号正则自动识别类型：匹配 `^1[3-9]\d{9}$` 则走手机号查询，否则走用户名查询
- `UserInfoMapper.selectByLogin` 直接删除，不保留废弃标记

**改造范围：**

- 新建 `bko_user_credential` 表（含基础索引）
- 删除 `UserInfoMapper.selectByLogin` 及对应 XML
- 重构 `AuthServiceImpl.userLogin`，将账号查询逻辑改为手机号/用户名自动分支
- 新增短信码登录接口（`POST /v1/auth/phone/login`）
- 新增第三方凭证登录接口（`POST /v1/auth/credential/login`）
- 新增凭证绑定接口（`POST /v1/auth/credential/bind`）
- 更新 `UserInfoService.sendPhoneCaptcha` 方法签名，增加 `userId` 参数

---

## Glossary

- **Auth_Service**：现有认证服务（`AuthServiceImpl`），负责完整登录流程
- **UserInfo_Mapper**：用户信息 Mapper，对应 `bko_user`
- **Credential_Mapper**：凭证表 Mapper（新增），对应 `bko_user_credential`，提供 `selectByCredentialType` 和 `upsertCredential` 两个通用方法
- **bko_user**：现有用户主表，包含 `phone`、`user_name`、`user_code`、`third_id` 等字段
- **bko_user_credential**：新增凭证表，每条记录对应一种第三方认证标识
- **credential_type**：凭证类型，当前支持 `weixin`（微信 openid）和 `wecom`（企业微信 userid）
- **credential_value**：凭证标识值，如微信 openid
- **phone_pattern**：手机号正则 `^1[3-9]\d{9}$`，用于判断 account 入参类型
- **bind_temp_token**：绑定用临时 Token，Redis 中以 `BIND_TEMP:{token}` 为 key 存储 `{credentialType, credentialValue}`，有效期 300 秒，参考 MFA 的 `TOTP_TEMP_TOKEN` 模式

---

## Requirements

### Requirement 1: 凭证表预建

**User Story:** 作为系统架构师，我希望预先建立凭证表的基础结构，以便后续微信登录、企业微信登录等扩展认证方式可以直接写入，而无需再变更表结构。

#### Acceptance Criteria

1. `bko_user_credential` 表的字段结构为：`id`（VARCHAR 26，主键）、`user_id`（VARCHAR 26，非空）、`credential_type`（VARCHAR 20，非空）、`credential_value`（VARCHAR 200，非空）、`institute_id`（VARCHAR 26，可空，全局唯一类型填 NULL，机构范围类型填机构ID）、`create_time`（DATETIME）、`update_time`（DATETIME）、`delete_state`（TINYINT，默认 0）。
2. `bko_user_credential` 表需建立联合唯一索引 `uk_type_value_inst(credential_type, credential_value, institute_id)`，保证同一类型下凭证标识唯一。
3. `bko_user_credential` 表需建立索引 `idx_user_id(user_id)`，支持按用户反查凭证。
4. 建表完成后表中不包含任何数据行，仅作结构预建。

---

### Requirement 2: 删除 selectByLogin

**User Story:** 作为开发人员，我希望移除 `selectByLogin` 的 OR 聚合查询，以便每种认证方式都有清晰的专用查询方法，便于维护和索引优化。

#### Acceptance Criteria

1. `UserInfoMapper` 中的 `selectByLogin` 方法及对应 XML `<select>` 元素直接删除。
2. `UserInfoMapper` 保留现有的 `selectByPhone(id, phone)` 和 `selectByUserName(id, userName)` 方法，不做改动。
3. 删除后代码库中不存在任何对 `selectByLogin` 的调用引用。

---

### Requirement 3: 账号登录逻辑重构

**User Story:** 作为用户，我希望在账号密码登录页输入手机号时系统能自动识别并走手机号认证，以便不需要关心输入的是哪种账号类型。

#### Acceptance Criteria

1. 在 `AuthServiceImpl` 中抽取私有方法 `findUserByAccount(String account)`：判断 `account` 是否匹配手机号正则 `^1[3-9]\d{9}$`，若匹配则调用 `userInfoMapper.selectByPhone(null, account)`，否则调用 `userInfoMapper.selectByUserName(null, account)`；若无结果返回 `null`。
2. `userLogin` 调用 `findUserByAccount` 获取用户；若返回 `null`，抛出统一错误码 `ACCOUNT_PASSWORD_ERROR`，不暴露具体是手机号还是用户名未找到。
3. 查询到用户后，继续执行现有的密码校验、连续失败锁定、MFA 双因子、Token 生成流程，与改造前保持一致。
4. `findUserByAccount` 方法可被 `credentialBind` 复用，避免重复实现账号查询逻辑。
5. `userLogin` 方法改造后不再调用 `selectByLogin`。

---

### Requirement 4: 工号和三方ID登录保持不变

**User Story:** 作为企业内部员工，我希望工号登录和三方ID登录的逻辑维持现状，以便本次改造不影响已有的登录方式。

#### Acceptance Criteria

1. 工号（`user_code`）和三方ID（`third_id`）的登录查询继续使用现有的 `userInfoMapper.checkByInstitute`，不做修改。
2. `bko_user.user_code` 和 `bko_user.third_id` 字段不迁移到 `bko_user_credential`。
3. 与工号、三方ID认证相关的 Controller、Service 接口、Mapper 方法均不做任何修改。

---

### Requirement 5: 手机号短信码登录

**User Story:** 作为用户，我希望使用手机号和短信验证码登录，以便在不记得密码时也能完成认证。

#### Acceptance Criteria

1. 新增 `AuthService.smsLogin(SmsLoginParam param, String clientIp)` 方法，对应接口 `POST /v1/auth/phone/login`，添加 `@IgnoreAuth(login = true)` 注解。
2. `SmsLoginParam` 字段：`phone`（必填，手机号格式校验）、`smsCode`（必填）、`rememberMe`、`userAgent`、`appVersion`；Controller 层如 `userAgent` 为空则从 HTTP 请求头自动填充，与其他登录接口保持一致。
3. `smsLogin` 调用时，先通过 `userInfoMapper.selectByPhone(null, phone)` 查找用户；若无结果，抛出 `ACCOUNT_PASSWORD_ERROR`。
4. 查到用户后，使用 `TotpAuthUtils.verifyDynamicCode(Base32.encode(userId), smsCode)` 验证短信码；验证使用用户的 `user_id` 而非手机号作为 TOTP 密钥，无需 Redis 存储验证码。
5. 若短信码验证失败，抛出 `CAPTCHA_ERROR`。
6. 短信码验证通过后，跳过密码校验，直接执行用户状态检查、MFA 判断及 Token 生成流程。
7. `UserInfoService.sendPhoneCaptcha` 方法签名更新为 `sendPhoneCaptcha(String phone, String userId)`，短信码改为 `TotpAuthUtils.generateDynamicCode(Base32.encode(userId))` 生成。
8. 已登录场景调用 `sendPhoneCaptcha` 时（如用户资料页修改手机号），调用方直接传 `CurrentUser.getInfo().getUserId()` 作为 `userId`，无需再查库。
9. 未登录场景调用 `sendPhoneCaptcha` 时（如短信登录发码），`AuthServiceImpl.sendPhoneCaptcha` 先通过 `userInfoMapper.selectByPhone(null, phone)` 获取 `userId`，再调用 `UserInfoService.sendPhoneCaptcha`；若手机号未注册，静默返回空字符串，不抛出异常，避免手机号枚举攻击。

---

### Requirement 6: 第三方凭证登录

**User Story:** 作为第三方应用接入方，我希望通过认证类型和凭证标识（如微信 openid）发起登录，以便已绑定账号的用户可以直接完成登录，未绑定的用户可以进入绑定流程。

#### Acceptance Criteria

1. 新增 `AuthService.credentialLogin(CredentialLoginParam param, String clientIp)` 方法，对应接口 `POST /v1/auth/credential/login`，添加 `@IgnoreAuth(login = true)` 注解。
2. `CredentialLoginParam` 字段：`credentialType`（必填）、`credentialValue`（必填）、`rememberMe`、`userAgent`、`appVersion`。
3. 若 `credentialType` 不在支持的枚举范围（`weixin`、`wecom`），抛出 `PARAM_ERROR`。
4. 调用 `credentialMapper.selectByCredentialType(credentialType, credentialValue, instituteId)` 查询凭证表；全局类型（如 `weixin`）传 `instituteId = null`，机构范围类型（如 `wecom`）传当前应用对应的 `institute_id`。
5. 若查询到凭证记录，通过 `user_id` 加载用户主信息，执行用户状态检查，跳过密码校验，走 Token 生成流程，返回完整的 `UserAuthDto`。
6. 若未查询到凭证记录，执行以下步骤：
   - 生成 `tempToken`（UUID 大写），参考 MFA 的 `TOTP_TEMP_TOKEN` 模式
   - 以 `BIND_TEMP:{tempToken}` 为 key，将 `{credentialType, credentialValue}` 序列化为 JSON 存入 Redis，TTL = 300 秒
   - 返回一个仅填充了 `accessToken = tempToken` 的 `UserAuthDto`，其余字段为空，表示需要绑定
   - `credentialType` 不需要返回给前端（客户端自身知道）

---

### Requirement 7: 凭证绑定

**User Story:** 作为未绑定凭证的用户，我希望通过提供账号密码和 tempToken 完成凭证绑定，以便绑定后直接完成登录，下次使用凭证登录时无需再绑定。

#### Acceptance Criteria

1. 新增 `AuthService.credentialBind(CredentialBindParam param, String clientIp)` 方法，对应接口 `POST /v1/auth/credential/bind`，添加 `@IgnoreAuth(login = true)` 注解。
2. `CredentialBindParam` 字段：`account`（必填）、`password`（必填，RSA 加密，标注 `@Sensitive(type = SensitiveType.CRYPT_RSA)` 用于日志脱敏）、`tempToken`（必填）、`rememberMe`、`userAgent`、`appVersion`。
3. 首先从 Redis 读取 `BIND_TEMP:{tempToken}` 获取 `{credentialType, credentialValue}`；若 key 不存在或已过期，抛出 `TOKEN_EMPTY`（提示用户重新发起凭证登录）。
4. tempToken 有效时，调用 `findUserByAccount(account)` 获取用户，再执行密码哈希校验、连续失败锁定，复用与 `userLogin` 相同的账号查询和密码验证逻辑。
5. 账号密码验证失败时，抛出与 `userLogin` 相同的错误码，不写入任何凭证记录。
6. 账号密码验证成功时：
   - 调用 `credentialMapper.upsertCredential(userId, credentialType, credentialValue, instituteId)` 写入凭证表：若该 `(user_id, credential_type)` 已存在则更新 `credential_value`，否则插入新记录
   - 删除 Redis 中的 `BIND_TEMP:{tempToken}`
   - 走完整 Token 生成流程，返回完整的 `UserAuthDto`，绑定与登录一步完成
