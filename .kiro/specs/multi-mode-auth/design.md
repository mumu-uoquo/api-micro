# Design Document: multi-mode-auth

## Overview

本设计文档描述对 `service-platform` 模块认证系统的多模式认证改造方案。改造目标是在不变更 `bko_user` 主表结构和现有前端页面的前提下：

1. 清理现有 `selectByLogin` OR 聚合查询，改为类型分支路由
2. 新增凭证表 `bko_user_credential` 作为第三方认证扩展基础设施
3. 新增短信码登录、第三方凭证登录、凭证绑定三条认证链路
4. 修正短信验证码密钥策略，以 `userId` 而非 `phone` 作为 TOTP 密钥

**设计原则：**
- 最小侵入：不迁移 `bko_user` 已有字段，不改动工号/三方ID认证路径
- 统一错误码：所有账号查询失败统一返回 `ACCOUNT_PASSWORD_ERROR`，不暴露账号类型
- Redis 临时令牌模式：`BIND_TEMP_TOKEN` 参照 `TOTP_TEMP_TOKEN` 模式实现，TTL=300s
- 无状态短信码：TOTP 动态码以 `userId` 为密钥，不依赖 Redis 存储验证码

---

## Architecture

### 改造后的认证链路全景

```mermaid
flowchart TD
    A[Client] -->|POST /v1/auth/account/login| B[AccountLogin]
    A -->|POST /v1/auth/phone/login| C[SmsLogin NEW]
    A -->|POST /v1/auth/credential/login| D[CredentialLogin NEW]
    A -->|POST /v1/auth/credential/bind| E[CredentialBind NEW]

    B --> F[findUserByAccount\nprivate method]
    C --> G[selectByPhone]
    E --> F

    F -->|matches ^1[3-9]\d{9}$| G[UserInfoMapper\nselectByPhone]
    F -->|no match| H[UserInfoMapper\nselectByUserName]

    D --> I[CredentialMapper\nselectByCredentialType]
    I -->|found| J[load user + checkStatus\n→ cacheUser2Redis]
    I -->|not found| K[generate tempToken\n→ Redis BIND_TEMP:token]

    K -->|client posts tempToken| E
    E --> L[Redis get BIND_TEMP:token]
    L --> F
    L --> M[upsertCredential\n→ cacheUser2Redis]

    J --> N[UserAuthDto full]
    M --> N
    B --> O[checkPassword\n→ cacheUser2Redis]
    O --> N
    C --> P[verifyDynamicCode\n→ cacheUser2Redis]
    P --> N
    K --> Q[UserAuthDto\naccessToken=tempToken only]
```

### 层次结构

```
AuthController          — HTTP 入口，@IgnoreAuth(login=true)
  └─ AuthService        — 认证服务接口
       └─ AuthServiceImpl — 认证逻辑实现
            ├─ findUserByAccount()    [private, shared]
            ├─ checkUserStatus()      [private, existing]
            ├─ getUserAuthDto()       [private, existing]
            ├─ cacheUser2Redis()      [private, existing]
            ├─ userLogin()            [refactored]
            ├─ smsLogin()             [new]
            ├─ credentialLogin()      [new]
            └─ credentialBind()       [new]

UserInfoMapper          — bko_user 查询（保留 selectByPhone/selectByUserName）
CredentialMapper        — bko_user_credential 查询/写入 [new]
UserInfoService         — sendPhoneCaptcha 签名更新
```

---

## Components and Interfaces

### 1. 删除：UserInfoMapper.selectByLogin

从 `UserInfoMapper.java` 和 `UserInfoMapper.xml` 中直接移除 `selectByLogin` 方法及对应 SQL。

**删除前（接口）：**
```java
UserInfo selectByLogin(@Param("instituteId") String instituteId,
                       @SensitiveField @Param("account") String account);
```

**删除前（XML）：**
```xml
<select id="selectByLogin" resultMap="BaseResultMap">
    ...phone/user_name/user_code OR 查询...
</select>
```

删除后 `UserInfoMapper` 保留 `selectByPhone(id, phone)` 和 `selectByUserName(id, userName)` 不变。

---

### 2. 新建：CredentialMapper

**路径：** `user/mapper/UserCredentialMapper.java`

```java
public interface UserCredentialMapper {

    /**
     * 按凭证类型+值+机构查询凭证记录
     * weixin 等全局类型传 instituteId=null
     */
    UserCredential selectByCredentialType(
            @Param("credentialType")  String credentialType,
            @Param("credentialValue") String credentialValue,
            @Param("instituteId")     String instituteId);

    /**
     * 凭证 upsert（ON DUPLICATE KEY UPDATE credential_value）
     * 唯一键：(credential_type, credential_value, institute_id)
     */
    int upsertCredential(
            @Param("userId")          String userId,
            @Param("credentialType")  String credentialType,
            @Param("credentialValue") String credentialValue,
            @Param("instituteId")     String instituteId);
}
```

**XML 路径：** `resources/mapper/UserCredentialMapper.xml`

关键 SQL：
```xml
<!-- upsert 使用 MySQL ON DUPLICATE KEY UPDATE -->
<insert id="upsertCredential">
    INSERT INTO bko_user_credential
        (id, user_id, credential_type, credential_value, institute_id,
         create_time, update_time, delete_state)
    VALUES
        (#{id,jdbcType=VARCHAR}, #{userId}, #{credentialType}, #{credentialValue},
         #{instituteId,jdbcType=VARCHAR}, NOW(), NOW(), 0)
    ON DUPLICATE KEY UPDATE
        user_id       = VALUES(user_id),
        credential_value = VALUES(credential_value),
        update_time   = NOW(),
        delete_state  = 0
</insert>
```

> 注：`id` 在 ServiceImpl 层调用前通过 `IDGenerator.getNextULID()` 生成并通过独立参数传入，或在 XML 中通过 `@SelectKey` 生成。推荐由 Service 层传入，保持 Mapper 层幂等性。

---

### 3. 新建：UserCredential POJO

**路径：** `user/model/pojo/UserCredential.java`

```java
package com.uoquo.platform.user.model.pojo;

import java.util.Date;

public class UserCredential {
    private String   id;
    private String   userId;
    private String   credentialType;
    private String   credentialValue;
    private String   instituteId;
    private Date     createTime;
    private Date     updateTime;
    private Integer  deleteState;
    // getters / setters omitted
}
```

---

### 4. 新建：CredentialTypeEnum

**路径：** `auth/model/enums/CredentialTypeEnum.java`（或 `common/enums/`）

```java
public enum CredentialTypeEnum {
    WEIXIN("weixin"),
    WECOM("wecom");

    private final String code;

    CredentialTypeEnum(String code) { this.code = code; }

    public String getCode() { return code; }

    public static boolean contains(String code) {
        for (CredentialTypeEnum e : values()) {
            if (e.code.equals(code)) return true;
        }
        return false;
    }
}
```

---

### 5. 新建：Param 类

#### SmsLoginParam

**路径：** `auth/model/param/SmsLoginParam.java`

```java
@Schema(description = "手机号短信码登录")
public class SmsLoginParam extends BasicLoginParam {

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "手机号")
    private String phone;

    @NotBlank(message = "短信验证码不能为空")
    @Schema(description = "短信验证码")
    private String smsCode;

    // getters / setters
}
```

#### CredentialLoginParam

**路径：** `auth/model/param/CredentialLoginParam.java`

```java
@Schema(description = "第三方凭证登录")
public class CredentialLoginParam extends BasicLoginParam {

    @NotBlank(message = "凭证类型不能为空")
    @Schema(description = "凭证类型（weixin/wecom）")
    private String credentialType;

    @NotBlank(message = "凭证标识不能为空")
    @Schema(description = "凭证标识值（如微信 openid）")
    private String credentialValue;

    // getters / setters
}
```

#### CredentialBindParam

**路径：** `auth/model/param/CredentialBindParam.java`

```java
@Schema(description = "凭证绑定（账号密码 + tempToken）")
public class CredentialBindParam extends BasicLoginParam {

    @NotBlank(message = "账号不能为空")
    @Schema(description = "登录账号（手机号或用户名）")
    private String account;

    @NotBlank(message = "密码不能为空")
    @Sensitive(type = SensitiveType.CRYPT_RSA)
    @Schema(description = "登录密码（RSA 加密）")
    private String password;

    @NotBlank(message = "临时Token不能为空")
    @Schema(description = "凭证登录返回的临时Token")
    private String tempToken;

    // getters / setters
}
```

---

### 6. 更新：PlatformCacheKey

在现有常量类中添加：

```java
/**
 * 第三方凭证绑定临时Token前缀（凭证未绑定时生成，TTL=300s）
 */
public static final String BIND_TEMP_TOKEN = "UOQUO:BIND_TEMP:";
```

---

### 7. 更新：AuthService 接口

新增三个方法：

```java
/**
 * 手机号短信码登录
 */
UserAuthDto smsLogin(SmsLoginParam param, String clientIp);

/**
 * 第三方凭证登录
 * 已绑定：返回完整 UserAuthDto
 * 未绑定：返回仅含 accessToken=tempToken 的 UserAuthDto
 */
UserAuthDto credentialLogin(CredentialLoginParam param, String clientIp);

/**
 * 凭证绑定（账号密码验证 + 写入凭证 + 完成登录）
 */
UserAuthDto credentialBind(CredentialBindParam param, String clientIp);
```

---

### 8. 更新：AuthServiceImpl

#### 8.1 新增私有方法 findUserByAccount

```java
/**
 * 按账号自动识别类型查询用户
 * 匹配手机号正则 ^1[3-9]\d{9}$ → selectByPhone
 * 其他 → selectByUserName
 */
private UserInfo findUserByAccount(String account) {
    if (account.matches("^1[3-9]\\d{9}$")) {
        return userInfoMapper.selectByPhone(null, account);
    } else {
        return userInfoMapper.selectByUserName(null, account);
    }
}
```

#### 8.2 重构 userLogin

将原有的 `userInfoMapper.selectByLogin(null, param.getAccount())` 替换为：

```java
UserInfo info = findUserByAccount(param.getAccount());
```

其余密码校验、MFA 判断、Token 生成流程保持不变。

#### 8.3 新增 smsLogin

```java
@Override
public UserAuthDto smsLogin(SmsLoginParam param, String clientIp) {
    CurrentUser.setClientIp(clientIp);
    CurrentUser.setAppVersion(param.getAppVersion());

    // 1. 查找用户
    UserInfo info = userInfoMapper.selectByPhone(null, param.getPhone());
    this.checkUserStatus(param.getPhone(), info);

    // 2. 验证短信码（以 userId 为 TOTP 密钥）
    String secret = Base32.encode(info.getId());
    boolean valid = TotpAuthUtils.verifyDynamicCode(secret, param.getSmsCode());
    if (!valid) {
        throw new UoquoException(AccountReturnCode.CAPTCHA_ERROR, "短信验证码不正确");
    }

    // 3. 跳过密码校验，走 MFA + Token 流程
    CurrentUser.setToken(null);
    UserAuthDto dto = this.getUserAuthDto(info);
    String setting = userSettingService.getValueByCode(info.getId(), SettingsCode.MFA_AUTH_ENABLED);
    if (!"true".equals(setting)) {
        dto.setTotpStatus("disabled");
    } else if (StringUtil.notNull(info.getTotpSecret())) {
        dto.setTotpStatus("enabled");
    } else {
        dto.setTotpStatus("unbound");
    }
    if ("enabled".equals(dto.getTotpStatus())) {
        String tempToken = this.generateToken();
        dto.setAccessToken(tempToken);
        dto.setRefreshToken(null);
        this.setCurrentUserInfo(dto);
        RedisUtil.put(PlatformCacheKey.TOTP_TEMP_TOKEN + tempToken, info.getId(), 300);
    } else {
        this.cacheUser2Redis(CurrentUser.getToken(), dto, false);
        this.publishEvent(BusinessOperationEnum.LOGIN, SystemReturnCode.SUCCESS,
                "USER", info.getId(), info.getInstituteId(), info.getPhone(), dto.getAccessToken(), null);
    }
    return dto;
}
```

#### 8.4 新增 credentialLogin

```java
@Override
public UserAuthDto credentialLogin(CredentialLoginParam param, String clientIp) {
    CurrentUser.setClientIp(clientIp);
    CurrentUser.setAppVersion(param.getAppVersion());

    // 1. 枚举校验
    if (!CredentialTypeEnum.contains(param.getCredentialType())) {
        throw new ParamErrorException("不支持的凭证类型：" + param.getCredentialType());
    }

    // 2. 查询凭证表（全局类型 instituteId=null）
    String instituteId = resolveInstituteId(param.getCredentialType());
    UserCredential credential = credentialMapper.selectByCredentialType(
            param.getCredentialType(), param.getCredentialValue(), instituteId);

    if (credential != null) {
        // 3a. 已绑定 → 正常登录
        UserInfo info = userInfoMapper.selectByPrimaryKey(credential.getUserId());
        this.checkUserStatus(param.getCredentialValue(), info);
        CurrentUser.setToken(null);
        UserAuthDto dto = this.getUserAuthDto(info);
        this.cacheUser2Redis(CurrentUser.getToken(), dto, false);
        this.publishEvent(BusinessOperationEnum.LOGIN, SystemReturnCode.SUCCESS,
                "USER", info.getId(), info.getInstituteId(), info.getUserName(), dto.getAccessToken(), null);
        return dto;
    } else {
        // 3b. 未绑定 → 生成 tempToken，返回最小 UserAuthDto
        String tempToken = IDGenerator.getUUID().toUpperCase();
        Map<String, String> bindInfo = new HashMap<>();
        bindInfo.put("credentialType",  param.getCredentialType());
        bindInfo.put("credentialValue", param.getCredentialValue());
        RedisUtil.put(PlatformCacheKey.BIND_TEMP_TOKEN + tempToken,
                JsonUtil.serialize(bindInfo), 300);
        UserAuthDto dto = new UserAuthDto();
        dto.setAccessToken(tempToken);
        return dto;
    }
}

/** 解析 instituteId：当前版本 weixin 全局类型返回 null，wecom 返回当前 appkey 对应机构 */
private String resolveInstituteId(String credentialType) {
    if (CredentialTypeEnum.WEIXIN.getCode().equals(credentialType)) {
        return null;
    }
    // wecom 等机构范围类型：从当前 AppInfo 中取机构ID
    AppInfo appInfo = appInfoMapper.selectByAppkey(CurrentUser.getAppkey());
    return appInfo != null ? appInfo.getInstituteId() : null;
}
```

#### 8.5 新增 credentialBind

```java
@Override
public UserAuthDto credentialBind(CredentialBindParam param, String clientIp) {
    CurrentUser.setClientIp(clientIp);
    CurrentUser.setAppVersion(param.getAppVersion());

    // 1. 读取绑定上下文
    String bindJson = RedisUtil.get(
            PlatformCacheKey.BIND_TEMP_TOKEN + param.getTempToken(), String.class);
    if (StringUtil.isNull(bindJson)) {
        throw new TokenEmptyException();
    }
    Map<String, String> bindInfo = JsonUtil.deserialize(bindJson, Map.class);
    String credentialType  = bindInfo.get("credentialType");
    String credentialValue = bindInfo.get("credentialValue");

    // 2. 账号查询 + 状态校验（复用 findUserByAccount）
    String captchaKey = CurrentUser.getDeviceId() + ":" + CurrentUser.getAppkey();
    // 验证码判断（与 userLogin 一致）
    String captchaFlag = RedisUtil.get(PlatformCacheKey.USER_CAPTCHA_FLAG + captchaKey, String.class);
    if (StringUtil.notNull(captchaFlag)) {
        if (StringUtil.isNull(param.getCaptcha())) {
            throw new ParamEmtpyException("验证码不能为空");
        }
        String captcha = RedisUtil.get(PlatformCacheKey.USER_CAPTCHA_CODE + captchaKey, String.class);
        RedisUtil.remove(PlatformCacheKey.USER_CAPTCHA_CODE + captchaKey);
        if (!param.getCaptcha().equalsIgnoreCase(captcha)) {
            throw new UoquoException(AccountReturnCode.CAPTCHA_ERROR, "验证码不正确");
        }
    }

    UserInfo info = findUserByAccount(param.getAccount());
    this.checkUserStatus(param.getAccount(), info);

    // 3. 密码校验（与 userLogin 一致）
    UserInfo paramUser = new UserInfo();
    paramUser.setId(info.getId());
    paramUser.setLastedLoginIp(clientIp);
    paramUser.setLastedLoginTime(new Date());
    boolean checkPassword = UserUtils.checkPassword(param.getPassword(), info.getPassword());
    if (!checkPassword) {
        int loginErrorCount = info.getLoginErrorCount() == null ? 0 : info.getLoginErrorCount();
        if (loginErrorCount >= 1) {
            RedisUtil.put(PlatformCacheKey.USER_CAPTCHA_FLAG + captchaKey, "1", passwordErrorLockTime * 60);
        }
        paramUser.setLoginErrorCount(++loginErrorCount);
        userInfoMapper.updateLastLoginInfo(paramUser);
        throw new UoquoException(AccountReturnCode.ACCOUNT_PASSWORD_ERROR,
                "密码错误,还可以输入 %d 次", (passwordErrorMaxNum - loginErrorCount));
    } else {
        paramUser.setLoginErrorCount(0);
        userInfoMapper.updateLastLoginInfo(paramUser);
    }

    // 4. 写入凭证（upsert）
    String instituteId = resolveInstituteId(credentialType);
    credentialMapper.upsertCredential(
            info.getId(), credentialType, credentialValue, instituteId);

    // 5. 清理 tempToken + 完成登录
    RedisUtil.remove(PlatformCacheKey.BIND_TEMP_TOKEN + param.getTempToken());
    RedisUtil.remove(PlatformCacheKey.USER_CAPTCHA_FLAG + captchaKey);

    CurrentUser.setToken(null);
    UserAuthDto dto = this.getUserAuthDto(info);
    this.cacheUser2Redis(CurrentUser.getToken(), dto, false);
    this.publishEvent(BusinessOperationEnum.LOGIN, SystemReturnCode.SUCCESS,
            "USER", info.getId(), info.getInstituteId(), param.getAccount(), dto.getAccessToken(), null);
    return dto;
}
```

> `CredentialBindParam` 需要添加 `captcha` 字段以支持验证码判断（复用 userLogin 验证码流程），或简化为直接不需要验证码（因为 bind 场景已通过 tempToken 验证了第一步的可信度），由实现者决定。当前设计建议与 userLogin 对齐，保留验证码字段但设为非必填。

---

### 9. 更新：AuthController

新增三个端点：

```java
@IgnoreAuth(login = true)
@Operation(summary = "手机号短信码登录", operationId = "smsLogin", method = "POST")
@PostMapping("/phone/login")
public ReturnData<UserAuthDto> smsLogin(HttpServletRequest request,
                                        @RequestBody @Valid SmsLoginParam param) {
    if (logger.isInfoEnabled()) {
        logger.info("smsLogin: phone={}", param.getPhone().replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2"));
    }
    if (StringUtil.isNull(param.getUserAgent())) {
        param.setUserAgent(request.getHeader("User-Agent"));
    }
    String clientIp = WebUtil.getClientIp(request);
    return new ReturnData<>(authService.smsLogin(param, clientIp));
}

@IgnoreAuth(login = true)
@Operation(summary = "第三方凭证登录", operationId = "credentialLogin", method = "POST")
@PostMapping("/credential/login")
public ReturnData<UserAuthDto> credentialLogin(HttpServletRequest request,
                                               @RequestBody @Valid CredentialLoginParam param) {
    if (logger.isInfoEnabled()) {
        logger.info("credentialLogin: type={}", param.getCredentialType());
    }
    if (StringUtil.isNull(param.getUserAgent())) {
        param.setUserAgent(request.getHeader("User-Agent"));
    }
    String clientIp = WebUtil.getClientIp(request);
    return new ReturnData<>(authService.credentialLogin(param, clientIp));
}

@IgnoreAuth(login = true)
@Operation(summary = "凭证绑定", operationId = "credentialBind", method = "POST")
@PostMapping("/credential/bind")
public ReturnData<UserAuthDto> credentialBind(HttpServletRequest request,
                                              @RequestBody @Valid CredentialBindParam param) {
    if (logger.isInfoEnabled()) {
        logger.info("credentialBind: account={}", param.getAccount());
    }
    if (StringUtil.isNull(param.getUserAgent())) {
        param.setUserAgent(request.getHeader("User-Agent"));
    }
    String clientIp = WebUtil.getClientIp(request);
    return new ReturnData<>(authService.credentialBind(param, clientIp));
}
```

---

### 10. 更新：UserInfoService / UserInfoServiceImpl / 调用方

#### 10.1 UserInfoService 接口签名变更

```java
// Before
String sendPhoneCaptcha(String phone);

// After
String sendPhoneCaptcha(String phone, String userId);
```

#### 10.2 UserInfoServiceImpl.sendPhoneCaptcha 实现变更

```java
@Override
public String sendPhoneCaptcha(String phone, String userId) {
    // 频率限制逻辑不变...
    // 2. 生成验证码（用 userId 做密钥，而非 phone）
    String secret = Base32.encode(userId);   // ← 关键变更
    String code = TotpAuthUtils.generateDynamicCode(secret);
    // 其余不变...
}
```

#### 10.3 UserInfoServiceImpl.updatePhone 验证变更

```java
// Before
String secret = Base32.encode(param.getPhone());

// After: 与 sendPhoneCaptcha 保持密钥一致
String secret = Base32.encode(userId);
```

#### 10.4 AuthServiceImpl.sendPhoneCaptcha 调用方变更

```java
@Override
public String sendPhoneCaptcha(PhoneCaptchaParam param, String clientIp) {
    // 图形验证码校验不变...

    // 未登录场景：先查 userId
    UserInfo user = userInfoMapper.selectByPhone(null, param.getPhone());
    if (user == null) {
        // 手机号未注册：静默返回，防枚举
        logger.info("sendPhoneCaptcha: phone={} 未注册，静默返回", param.getPhone());
        return "";
    }
    return userInfoService.sendPhoneCaptcha(param.getPhone(), user.getId());
}
```

#### 10.5 UserProfileController.sendPhoneCaptcha 调用方变更

```java
@PostMapping("/phone/captcha")
public ReturnData<String> sendPhoneCaptcha(@RequestBody @Valid SendPhoneCodeParam param) {
    // 已登录场景：直接取 userId，无需查库
    String userId = CurrentUser.getInfo().getUserId();
    String result = userInfoService.sendPhoneCaptcha(param.getPhone(), userId);
    return new ReturnData<>(result);
}
```

---

## Data Models

### bko_user_credential 建表 DDL

```sql
CREATE TABLE `bko_user_credential` (
    `id`               VARCHAR(26)  NOT NULL COMMENT '主键 ULID',
    `user_id`          VARCHAR(26)  NOT NULL COMMENT '用户ID，关联 bko_user.id',
    `credential_type`  VARCHAR(20)  NOT NULL COMMENT '凭证类型（weixin/wecom）',
    `credential_value` VARCHAR(200) NOT NULL COMMENT '凭证标识值（openid/userid）',
    `institute_id`     VARCHAR(26)      NULL COMMENT '机构ID；全局凭证为 NULL，机构凭证填机构ID',
    `create_time`      DATETIME         NULL COMMENT '创建时间',
    `update_time`      DATETIME         NULL COMMENT '更新时间',
    `delete_state`     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除标志（0=正常）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_type_value_inst` (`credential_type`, `credential_value`, `institute_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户第三方凭证表';
```

**字段说明：**

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | VARCHAR(26) | PK | ULID，由 IDGenerator.getNextULID() 生成 |
| user_id | VARCHAR(26) | NOT NULL | 关联 bko_user.id |
| credential_type | VARCHAR(20) | NOT NULL | 枚举值：weixin、wecom |
| credential_value | VARCHAR(200) | NOT NULL | 微信 openid 通常 28 字节，企业微信更长 |
| institute_id | VARCHAR(26) | NULL | NULL 表示全局唯一；非 NULL 表示机构范围唯一 |
| delete_state | TINYINT | DEFAULT 0 | 逻辑删除，0=正常，参照 bko_user 规范 |

**唯一约束设计说明：**
- `uk_type_value_inst(credential_type, credential_value, institute_id)` 中，`institute_id` 可为 NULL。MySQL 对 NULL 的唯一索引处理：NULL != NULL，因此同一 `credentialValue` 在 `instituteId=NULL` 时最多存一条。
- upsert 中若需精确匹配 NULL，XML 需使用 `<if test="instituteId == null">AND institute_id IS NULL</if>` 判断，或在应用层统一将 NULL 转为固定占位符（如 `'_GLOBAL_'`），二选一，建议使用占位符以确保唯一索引正常工作。

### Redis 数据模型

| Key 模式 | Value 类型 | TTL | 说明 |
|---------|-----------|-----|------|
| `UOQUO:BIND_TEMP:{token}` | JSON String | 300s | 凭证绑定上下文：`{"credentialType":"weixin","credentialValue":"oXXX"}` |
| `UOQUO:TOTP:TEMP_TOKEN:{token}` | String (userId) | 300s | MFA 临时 Token（现有，不变） |

### 关键数据流

```
credentialLogin (未绑定)
  → Redis: BIND_TEMP:{uuid} = {"credentialType":"weixin","credentialValue":"oXXX"}
  → 返回: UserAuthDto { accessToken = uuid, 其余字段 null }

credentialBind (成功)
  → Redis: DEL BIND_TEMP:{uuid}
  → DB:    UPSERT bko_user_credential(user_id, credential_type, credential_value, institute_id)
  → Redis: SET USER_INFO:{newToken} = CurrentUser.UserInfo
  → 返回: UserAuthDto { 完整用户信息 }
```

---

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system — essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property 1: findUserByAccount 路由不变性

*For any* 字符串 `account`，若其匹配正则 `^1[3-9]\d{9}$`，则 `findUserByAccount` 总是且仅调用 `userInfoMapper.selectByPhone`；否则总是且仅调用 `userInfoMapper.selectByUserName`。

**Validates: Requirements 3.1**

### Property 2: TOTP 短信码 round-trip 自洽性

*For any* 有效的 `userId` 字符串，用 `Base32.encode(userId)` 作为密钥生成的动态码，用同一密钥调用 `verifyDynamicCode` 必须返回 `true`。

即：`TotpAuthUtils.verifyDynamicCode(Base32.encode(userId), TotpAuthUtils.generateDynamicCode(Base32.encode(userId))) == true` 对所有 `userId` 成立。

**Validates: Requirements 5.4, 5.7**

### Property 3: credentialType 枚举拒绝非法值

*For any* 不属于 `CredentialTypeEnum`（即不等于 `"weixin"` 且不等于 `"wecom"`）的字符串作为 `credentialType` 入参，`credentialLogin` 必须抛出参数错误异常，且不查询凭证表、不访问数据库。

**Validates: Requirements 6.3**

### Property 4: credentialLogin 未绑定响应结构不变性

*For any* 有效的 `(credentialType, credentialValue)` 组合，当 `credentialMapper.selectByCredentialType` 返回 `null` 时，`credentialLogin` 返回的 `UserAuthDto` 满足：`accessToken` 非空（为 tempToken），且 `userId`、`userName`、`roleList`、`groupList` 等用户字段均为 `null` 或空集合。

**Validates: Requirements 6.6**

### Property 5: credentialBind 密码失败的安全不变量

*For any* 有效的 `tempToken`（在 Redis 中存在且未过期）和任意账号密码组合，若密码校验失败，则：
1. `credentialMapper.upsertCredential` 从不被调用
2. Redis 中 `BIND_TEMP:{tempToken}` key 依然存在（未被消耗）

**Validates: Requirements 7.5**

---

## Error Handling

### 错误码映射

| 场景 | 错误码 | 说明 |
|------|--------|------|
| 账号/手机号不存在 | `ACCOUNT_PASSWORD_ERROR` | 统一返回，不暴露具体原因 |
| 短信验证码错误 | `CAPTCHA_ERROR` | smsLogin 验证失败 |
| credentialType 非法 | `PARAM_ERROR` | 不在枚举范围内 |
| tempToken 不存在/过期 | `TokenEmptyException` | credentialBind 绑定上下文失效 |
| 密码错误 | `ACCOUNT_PASSWORD_ERROR` | credentialBind 密码校验失败，带剩余次数提示 |

### 防枚举攻击

`AuthServiceImpl.sendPhoneCaptcha`（未登录发码场景）：当手机号不存在时，静默返回空字符串，不抛出异常，不暴露手机号是否注册信息。

### 错误日志规范

- 所有认证失败事件通过 `publishEvent` 上报，与现有 `userLogin` 一致
- smsLogin 无需记录密码相关日志（无密码字段）
- credentialLogin 未绑定场景属于正常业务流，仅记录 INFO 级日志

---

## Testing Strategy

### 单元测试（Unit Tests）

针对纯逻辑层，使用 Mock 隔离数据库和 Redis：

| 测试类 | 覆盖范围 |
|--------|---------|
| `FindUserByAccountTest` | 手机号正则边界：130/139/190 开头（匹配），字母账号/10位数字（不匹配） |
| `SmsLoginTest` | 正常流程、用户不存在、短信码错误、MFA 分支 |
| `CredentialLoginTest` | 已绑定流程、未绑定流程、非法 credentialType |
| `CredentialBindTest` | tempToken 失效、密码错误、成功绑定、upsert 幂等 |
| `SendPhoneCaptchaTest` | 已登录/未登录场景、手机号未注册静默返回 |

### 属性化测试（Property-Based Tests）

使用 **jqwik**（Java 属性化测试库）实现，每条属性最少运行 **100 次**。

#### Property 1 实现思路（findUserByAccount 路由）

```java
// Feature: multi-mode-auth, Property 1: findUserByAccount routing
@Property(tries = 100)
void phonePatternAlwaysRoutesToSelectByPhone(
        @ForAll @StringLength(min = 11, max = 11) @NumericChars String phone) {
    // 只取首字符在 1[3-9] 范围的生成值
    assumeThat(phone).matches("^1[3-9]\\d{9}$");
    // mock userInfoMapper
    authService.findUserByAccountForTest(phone);
    verify(userInfoMapper).selectByPhone(null, phone);
    verify(userInfoMapper, never()).selectByUserName(any(), any());
}

@Property(tries = 100)
void nonPhoneAlwaysRoutesToSelectByUserName(@ForAll String account) {
    assumeThat(account).doesNotMatch("^1[3-9]\\d{9}$");
    authService.findUserByAccountForTest(account);
    verify(userInfoMapper).selectByUserName(null, account);
    verify(userInfoMapper, never()).selectByPhone(any(), any());
}
```

#### Property 2 实现思路（TOTP round-trip）

```java
// Feature: multi-mode-auth, Property 2: TOTP SMS code round-trip
@Property(tries = 100)
void totpRoundTripAlwaysVerifies(@ForAll @StringLength(min = 1, max = 50) String userId) {
    String secret = Base32.encode(userId);
    String code = TotpAuthUtils.generateDynamicCode(secret);
    assertThat(TotpAuthUtils.verifyDynamicCode(secret, code)).isTrue();
}
```

#### Property 3 实现思路（credentialType 枚举校验）

```java
// Feature: multi-mode-auth, Property 3: credential type enum validation
@Property(tries = 100)
void invalidCredentialTypeAlwaysThrows(@ForAll String type) {
    assumeThat(type).isNotIn("weixin", "wecom");
    CredentialLoginParam param = new CredentialLoginParam();
    param.setCredentialType(type);
    param.setCredentialValue("any");
    assertThatThrownBy(() -> authService.credentialLogin(param, "127.0.0.1"))
            .isInstanceOf(ParamErrorException.class);
    verifyNoInteractions(credentialMapper);
}
```

#### Property 4 实现思路（未绑定响应结构）

```java
// Feature: multi-mode-auth, Property 4: unbound credential response structure
@Property(tries = 100)
void unboundCredentialReturnsOnlyAccessToken(
        @ForAll @From("validCredentialType") String type,
        @ForAll @StringLength(min = 1, max = 100) String value) {
    when(credentialMapper.selectByCredentialType(any(), any(), any())).thenReturn(null);
    UserAuthDto result = authService.credentialLogin(buildParam(type, value), "127.0.0.1");
    assertThat(result.getAccessToken()).isNotBlank();
    assertThat(result.getUserId()).isNull();
    assertThat(result.getUserName()).isNull();
    assertThat(result.getRoleList()).isNullOrEmpty();
}
```

#### Property 5 实现思路（bind 密码失败安全不变量）

```java
// Feature: multi-mode-auth, Property 5: failed bind does not consume tempToken or write credential
@Property(tries = 100)
void failedPasswordDoesNotWriteCredentialOrConsumeToken(
        @ForAll @StringLength(min = 1) String account,
        @ForAll @StringLength(min = 1) String wrongPassword) {
    String tempToken = "test-token";
    when(redisUtil.get(BIND_TEMP_TOKEN + tempToken, String.class))
            .thenReturn("{\"credentialType\":\"weixin\",\"credentialValue\":\"oXXX\"}");
    when(userInfoMapper.selectByUserName(any(), any())).thenReturn(mockUser());
    when(UserUtils.checkPassword(wrongPassword, any())).thenReturn(false);

    CredentialBindParam param = buildBindParam(account, wrongPassword, tempToken);
    assertThatThrownBy(() -> authService.credentialBind(param, "127.0.0.1"))
            .isInstanceOf(UoquoException.class);
    verifyNoInteractions(credentialMapper);
    verify(redisUtil, never()).remove(BIND_TEMP_TOKEN + tempToken);
}
```

### 集成测试（Integration Tests）

| 测试 | 说明 |
|------|------|
| `CredentialMapperIT` | selectByCredentialType、upsertCredential（含幂等验证）的真实 DB 测试 |
| `SmsLoginIT` | 完整短信码登录链路（含 Redis 状态）1-2 个代表性例子 |
| `CredentialBindFlowIT` | credentialLogin → credentialBind 端到端流程 |
