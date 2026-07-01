package com.uoquo.platform.auth.service.impl;

import java.awt.image.BufferedImage;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.uoquo.cloud.events.RemoteEvent;
import com.uoquo.platform.auth.model.dto.CredentialConfigDto;
import com.uoquo.platform.auth.model.dto.CredentialStatusDto;
import com.uoquo.platform.auth.model.dto.OpsConfigDto;
import com.uoquo.platform.auth.model.dto.TokenDto;
import com.uoquo.platform.auth.model.dto.UserAuthDto;
import com.uoquo.platform.auth.model.enums.CredentialTypeEnum;
import com.uoquo.platform.auth.model.param.AccountLoginParam;
import com.uoquo.platform.auth.model.param.CaptchaParam;
import com.uoquo.platform.auth.model.param.CredentialBindParam;
import com.uoquo.platform.auth.model.param.CredentialLoginParam;
import com.uoquo.platform.auth.model.param.OpsConfigParam;
import com.uoquo.platform.auth.model.param.OpsLoginParam;
import com.uoquo.platform.auth.model.param.PhoneCaptchaParam;
import com.uoquo.platform.auth.model.param.RegisterParam;
import com.uoquo.platform.auth.model.param.ResetPasswordParam;
import com.uoquo.platform.auth.model.param.EmergencyLoginParam;
import com.uoquo.platform.auth.model.param.SmsLoginParam;
import com.uoquo.platform.auth.model.pojo.AuthInfo;
import com.uoquo.platform.auth.service.AuthService;
import com.uoquo.platform.auth.service.WechatService;
import com.uoquo.platform.common.BaseConstant;
import com.uoquo.platform.common.BusinessOperationEnum;
import com.uoquo.platform.common.BusinessTypeEnum;
import com.uoquo.platform.common.DictionaryCodeEnum;
import com.uoquo.platform.common.PlatformCacheKey;
import com.uoquo.platform.common.SettingsCode;
import com.uoquo.platform.common.exception.AccountReturnCode;
import com.uoquo.platform.common.exception.InstituteReturnCode;
import com.uoquo.platform.common.utils.TotpAuthUtils;
import com.uoquo.platform.common.utils.UserUtils;
import com.uoquo.platform.institute.mapper.InstituteInfoMapper;
import com.uoquo.platform.institute.model.pojo.InstituteInfo;
import com.uoquo.platform.role.model.dto.ModuleTreeDto;
import com.uoquo.platform.role.service.ModuleInfoService;
import com.uoquo.platform.system.mapper.AppInfoMapper;
import com.uoquo.platform.system.model.pojo.AppInfo;
import com.uoquo.platform.system.service.SysSettingService;
import com.uoquo.platform.user.mapper.UserCredentialMapper;
import com.uoquo.platform.user.mapper.UserInfoMapper;
import com.uoquo.platform.user.model.dto.GroupDto;
import com.uoquo.platform.user.model.dto.UserRoleDto;
import com.uoquo.platform.user.model.param.UserAddParam;
import com.uoquo.platform.user.model.pojo.UserCredential;
import com.uoquo.platform.user.model.pojo.UserInfo;
import com.uoquo.platform.user.service.UserInfoService;
import com.uoquo.platform.user.service.UserSettingService;
import com.uoquo.utils.CurrentUser;
import com.uoquo.utils.IDGenerator;
import com.uoquo.utils.StringUtil;
import com.uoquo.utils.crypto.Base32;
import com.uoquo.utils.crypto.TimeStepCryptoUtil;
import com.uoquo.utils.json.JsonUtil;
import com.uoquo.utils.spring.CaptchaUtil;
import com.uoquo.utils.spring.RedisUtil;
import com.uoquo.web.BaseCacheKey;
import com.uoquo.web.BaseReturnCode;
import com.uoquo.web.SystemReturnCode;
import com.uoquo.web.events.UoquoEventPublisher;
import com.uoquo.web.exception.AbstractBaseException;
import com.uoquo.web.exception.ForbiddenException;
import com.uoquo.web.exception.ParamEmtpyException;
import com.uoquo.web.exception.ParamErrorException;
import com.uoquo.web.exception.ResourceNotFoundException;
import com.uoquo.web.exception.TokenEmptyException;
import com.uoquo.web.exception.UoquoException;

@Service
public class AuthServiceImpl implements AuthService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    private UserSettingService userSettingService;

    @Autowired
    private ModuleInfoService moduleInfoService;

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Autowired
    private AppInfoMapper appInfoMapper;

    @Autowired
    private InstituteInfoMapper instituteInfoMapper;

    @Autowired
    private UserCredentialMapper credentialMapper;

    @Autowired
    private SysSettingService sysSettingService;

    @Autowired
    private WechatService wechatService;

    @Autowired
    private UoquoEventPublisher eventPublisher;

    @Autowired
    private CaptchaUtil captchaUtil;

    // 连续出错最大次数（默认5次）
    @Value("${app.auth.error.max-num:5}")
    private int passwordErrorMaxNum;
    // 连续出错后锁定时长（分钟，默认10分钟）
    @Value("${app.auth.error.lock-time:10}")
    private int passwordErrorLockTime;
    // APPKEY的token有效时长（分钟，默认2小时）
    @Value("${app.auth.appkey.timeout:120}")
    private int appTokenTimeout;
    // APPKEY的token有效时长（分钟，默认7天）
    @Value("${app.auth.refresh.timeout:10080}")
    private int refreshTimeout;

    /**
     * 静态资源前缀
     */
    @Value("${app.host.static:/}")
    private String staticHost;

    // 要更新连续出错信息，所以不能加事务控制！
    @Override
    public UserAuthDto userLogin(AccountLoginParam param, String clientIp) {
        // 请求端信息放入CurrentUser，方便后续记录日志
        CurrentUser.setClientIp(clientIp);
        CurrentUser.setAppVersion(param.getAppVersion());

        // 0. 验证码判断
        String captchaKey = CurrentUser.getDeviceId() + ":" + CurrentUser.getAppkey();
        String captchaFlag = RedisUtil.get(PlatformCacheKey.USER_CAPTCHA_FLAG + captchaKey, String.class);
        if (StringUtil.notNull(captchaFlag)) {
            if (StringUtil.isNull(param.getCaptcha())) {
                throw new ParamEmtpyException("验证码不能为空");
            }
            // 验证码只能用一次
            String captcha = RedisUtil.get(PlatformCacheKey.USER_CAPTCHA_CODE + captchaKey, String.class);
            RedisUtil.remove(PlatformCacheKey.USER_CAPTCHA_CODE + captchaKey);
            if (!param.getCaptcha().equalsIgnoreCase(captcha)) {
                logger.warn("用户[{}]输入的验证码[{}]与缓存的[{}]不一致", param.getAccount(), param.getCaptcha(), captcha);
                throw new UoquoException(AccountReturnCode.CAPTCHA_ERROR, "验证码不正确");
            }
        }
        // 1. 账号校验
        UserInfo info = this.findUserByAccount(param.getAccount());
        this.checkUserStatus(param.getAccount(), info);
        // 2. 密码校验（含错误计数与验证码锁定逻辑）
        this.checkAndVerifyPassword(info, param.getPassword(), param.getAccount(), clientIp);
        // TODO 应该采用“增强验证码流程”多维度风险评估（失败次数、时间密度、IP地址、设备指纹等）， 连续两次出错，则需要填验证码

        // 删除验证码标识
        RedisUtil.remove(PlatformCacheKey.USER_CAPTCHA_FLAG + captchaKey);
        // 3. 校验通过，MFA 判断 + 完成登录
        return this.completeLoginWithMfa(info, param.getAccount());
    }

    @Override
    public UserAuthDto mfaLogin(String tempToken, String totpCode) {
        // 1. 验证临时Token
        String userId = RedisUtil.get(PlatformCacheKey.TOTP_TEMP_TOKEN + tempToken, String.class);
        if (StringUtil.isNull(userId)) {
            throw new TokenEmptyException();
        }
        // 判断错误次数
        String errorKey = PlatformCacheKey.TOTP_VERIFY_ERROR + userId;
        Integer errors = RedisUtil.get(errorKey, Integer.class);
        if ((errors != null) && (errors >= 5)) {
            RedisUtil.remove(PlatformCacheKey.TOTP_TEMP_TOKEN + tempToken);
            RedisUtil.remove(PlatformCacheKey.TOTP_VERIFY_ERROR + userId);
            throw new UoquoException(AccountReturnCode.TOTP_ATTEMPT_EXCEED, "动态码错误次数过多，请重新获取二维码");
        }

        // 2. 获取用户信息
        UserInfo info = userInfoMapper.selectByPrimaryKey(userId);
        if (info == null) {
            throw new ResourceNotFoundException("用户不存在");
        }
        String secret = info.getTotpSecret();
        if (StringUtil.isNull(secret)) {
            throw new UoquoException(AccountReturnCode.ACCOUNT_UNBOUND_2FA, "用户未绑定双因子认证");
        }

        // 3. 验证动态码
        boolean verified = TotpAuthUtils.verifyAuthCode(secret, totpCode);
        if (!verified) {
            // 记录错误次数（防暴力破解）
            errors = (errors == null) ? 1 : errors + 1;
            RedisUtil.put(errorKey, errors, 600);
            throw new UoquoException(AccountReturnCode.TOTP_VALIDATION_ERROR, "动态码不正确");
        }

        // 4. 验证通过，清理临时Token和错误记录
        RedisUtil.remove(PlatformCacheKey.TOTP_TEMP_TOKEN + tempToken);
        RedisUtil.remove(PlatformCacheKey.TOTP_VERIFY_ERROR + userId);

        // 5. 补全用户信息并缓存（完成登录）
        CurrentUser.setToken(null);
        UserAuthDto dto = this.getUserAuthDto(info);
        dto.setTotpStatus("enabled");
        this.cacheUser2Redis(CurrentUser.getToken(), dto, false);

        // 6. 发布事件（登录）
        this.publishEvent(BusinessOperationEnum.LOGIN, SystemReturnCode.SUCCESS, "USER", info.getId(), info.getInstituteId(), info.getUserName(), dto.getAccessToken(), null);
        return dto;
    }

    @Override
    public UserAuthDto emergencyLogin(EmergencyLoginParam param, String clientIp) {
        CurrentUser.setClientIp(clientIp);
        CurrentUser.setAppVersion(param.getAppVersion());

        // 验证对应场景是否开启登录
        String enableSetting = sysSettingService.getValueByCode(SettingsCode.LOGIN_EMERG_ENABLE);
        if (!"true".equals(enableSetting)) {
            throw new ForbiddenException("系统未开启[紧急登录]的认证方式");
        }

        // 1. 检查紧急登录锁定状态
        String account = param.getAccount();
        String failKey = PlatformCacheKey.EMERGENCY_LOGIN_FAIL + account;
        String lockKey = PlatformCacheKey.EMERGENCY_LOGIN_LOCK + account;
        if (StringUtil.notNull(RedisUtil.get(lockKey, String.class))) {
            throw new UoquoException(AccountReturnCode.EMERGENCY_LOGIN_LOCKED);
        }

        // 2. 用户基本校验
        UserInfo info = this.findUserByAccount(param.getAccount());
        // 2.1 检查是否绑定了 MFA
        String secret = info.getTotpSecret();
        if (StringUtil.isNull(secret)) {
            throw new UoquoException(AccountReturnCode.ACCOUNT_UNBOUND_2FA, "用户未绑定双因子认证，无法使用紧急登录");
        }
        // 2.2 校验用户状态
        this.checkUserStatus(param.getAccount(), info);

        // 3. 验证 MFA 动态码
        try {
            boolean verified = TotpAuthUtils.verifyAuthCode(secret, param.getTotpCode());
            if (!verified) {
                throw new UoquoException(AccountReturnCode.TOTP_VALIDATION_ERROR, "动态码不正确");
            }
        } catch (AbstractBaseException e) {
            // 失败计数，连续 5 次锁定 24 小时
            long fails = this.incrEmergencyFail(failKey);
            logger.warn("用户[{}]第[{}]次紧急登录失败。", account, fails);
            if (fails >= 5) {
                RedisUtil.put(lockKey, "1", 24 * 60 * 60);
                logger.error("用户[{}]连续[{}]次紧急登录失败，将锁定24小时。", account, fails);
            }
            throw e;
        }

        // 4. 验证通过：清理失败计数，直接完成登录（跳过 MFA 二次验证）
        RedisUtil.remove(failKey);
        RedisUtil.remove(lockKey);

        CurrentUser.setToken(null);
        UserAuthDto dto = this.getUserAuthDto(info);
        dto.setTotpStatus("enabled");
        this.cacheUser2Redis(CurrentUser.getToken(), dto, false);

        this.publishEvent(BusinessOperationEnum.LOGIN, SystemReturnCode.SUCCESS,
                "USER", info.getId(), info.getInstituteId(), account, dto.getAccessToken(), null);
        return dto;
    }

    @Override
    public TokenDto userRefreshLogin(String refreshToken, String roleId, String clientIp) {
        Map<String, String> map = RedisUtil.get(PlatformCacheKey.USER_TOKEN_REFRESH + refreshToken, Map.class);
        if (map == null) {
            logger.info("设备[{}]的应用[{}]请求刷新码[{}]已失效", CurrentUser.getDeviceId(), CurrentUser.getAppkey(), refreshToken);
            throw new TokenEmptyException();
        }
        // 只能用一次
        RedisUtil.remove(PlatformCacheKey.USER_TOKEN_REFRESH + refreshToken);
        // 1. 仅同一个设备可以使用当前刷新码
        String deviceId = map.get("deviceId");
        if (!CurrentUser.getDeviceId().equals(deviceId)) {
            logger.info("设备[{}]的应用[{}]请求刷新码[{}]非法，原设备[{}]", CurrentUser.getDeviceId(), CurrentUser.getAppkey(), refreshToken, deviceId);
            throw new ForbiddenException();
        }
        String appkey = map.get("appkey");
        if (!CurrentUser.getAppkey().equals(appkey)) {
            logger.info("设备[{}]的应用[{}]请求刷新码[{}]非法，原应用[{}]", CurrentUser.getDeviceId(), CurrentUser.getAppkey(), refreshToken, appkey);
            throw new ForbiddenException();
        }
        // 2. 获取用户
        String userId = map.get("userId");
        UserInfo info = userInfoMapper.selectByPrimaryKey(userId);
        this.checkUserStatus(userId, info);
        // 3. 校验通过，返回用户dto信息
        UserAuthDto dto = this.getUserAuthDto(info);
        // 运维模式：恢复 opsMode 标识及手机号展示（防止刷新 token 时丢失运维模式）
        if ("true".equals(map.get("opsMode"))) {
            CurrentUser.getInfo().setOpsMode(true);
            dto.setUserName(map.get("userName"));
            dto.setRealName(map.get("realName"));
        }
        // 默认继续使用当前角色
        String currentRoleId = CurrentUser.getInfo().getCurrentRoleId();
        if (StringUtil.isNull(currentRoleId)) {
            currentRoleId = roleId;
        }
        if (StringUtil.notNull(currentRoleId)) {
            for (UserRoleDto role : dto.getRoleList()) {
                if (role.getId().equals(currentRoleId)) {
                    dto.setCurrentRoleId(currentRoleId);
                    break;
                }
            }
        }
        // 缓存用户信息
        this.cacheUser2Redis(CurrentUser.getToken(), dto, true);
        // 缓存授权菜单
        if (StringUtil.notNull(dto.getCurrentRoleId())) {
            getPermissionByRoleId(dto.getCurrentRoleId());
        }
        // 发布事件（登录）
        this.publishEvent(BusinessOperationEnum.LOGIN, SystemReturnCode.SUCCESS, "USER", info.getId(), info.getInstituteId(), info.getUserName(), dto.getAccessToken(), null);
        // 重新包装返回内容，仅返回token，其他信息不返回（防止用户信息泄露）
        TokenDto result = new TokenDto();
        result.setAccessToken(dto.getAccessToken());
        result.setRefreshToken(dto.getRefreshToken());
        result.setExpireTime(dto.getExpireTime());
        return result;
    }

    @Override
    public TokenDto appLogin(AccountLoginParam param, String clientIp) {
        AppInfo app = appInfoMapper.selectByAppkey(param.getAccount());
        // 1.1 校验状态
        this.checkAppStatus(param.getAccount(), app);
        // 1.2 校验密码
        String password = param.getPassword();
        if (!app.getSecret().equals(password)) {
            logger.warn("应用[{}]授权秘钥[{}]错误", param.getAccount(), password);
            this.publishEvent(BusinessOperationEnum.LOGIN, AccountReturnCode.PASSWORD_ERROR, "APP", app.getId(), app.getInstituteId(), param.getAccount(), null, password);
            throw new UoquoException(AccountReturnCode.ACCOUNT_PASSWORD_ERROR);
        }
        // 2. 生成token
        CurrentUser.setToken(null);
        TokenDto dto = this.getAppAuthDto(app);
        // 发布事件（登录）
        this.publishEvent(BusinessOperationEnum.LOGIN, SystemReturnCode.SUCCESS, "APP", app.getId(), app.getInstituteId(), param.getAccount(), dto.getAccessToken(), null);
        return dto;
    }

    @Override
    public TokenDto appRefreshLogin(String refreshToken, String clientIp) {
        String appkey = RedisUtil.get(BaseCacheKey.APPKEY_TOKEN_REFRESH + refreshToken, String.class);
        if (StringUtil.isNull(appkey)) {
            logger.info("应用[{}]请求刷新码[{}]已失效", CurrentUser.getAppkey(), refreshToken);
            throw new TokenEmptyException();
        }
        // 只能用一次
        RedisUtil.remove(PlatformCacheKey.USER_TOKEN_REFRESH + refreshToken);
        if (!appkey.equals(CurrentUser.getAppkey())) {
            logger.info("应用[{}]请求刷新码[{}]非法，原应用[{}]", CurrentUser.getAppkey(), refreshToken, appkey);
            throw new ForbiddenException();
        }
        AppInfo app = appInfoMapper.selectByAppkey(appkey);
        // 1.1 校验状态
        this.checkAppStatus(appkey, app);
        // 2. 生成token
        TokenDto dto = this.getAppAuthDto(app);
        // 发布事件（登录）
        this.publishEvent(BusinessOperationEnum.LOGIN, SystemReturnCode.SUCCESS, "APP", app.getId(), app.getInstituteId(), appkey, dto.getAccessToken(), null);
        return dto;
    }

    @Override
    public void logout(String token, String appkey, BaseReturnCode status) {
        CurrentUser.UserInfo user = RedisUtil.get(BaseCacheKey.USER_INFO_PREFIX + token, CurrentUser.UserInfo.class);
        if (user == null) {
            logger.warn("[{}]退出应用[{}]的请求token[{}]已失效", status, token, CurrentUser.getAppkey());
            return;
        }
        // 发布事件（登出）
        this.publishEvent(BusinessOperationEnum.LOGOUT, status, "USER", user.getUserId(), user.getInstituteId(), user.getUserName(), token, null);
        // 清理token
        this.clearToken2Redis(token);
        // 清理用户
        RedisUtil.remove(BaseCacheKey.USER_TOKEN_PREFIX + user.getUserId() + ":" + appkey);
    }

    @Override
    public UserAuthDto getUserInfo() {
        CurrentUser.UserInfo currentUser = CurrentUser.getInfo();
        // 1. 查询当前用户信息
        UserInfo info = userInfoMapper.selectByPrimaryKey(currentUser.getUserId());
        if (info == null) {
            throw new ResourceNotFoundException("用户信息不存在");
        }
        // 2. 补全用户信息
        UserAuthDto dto = this.getUserAuthDto(info);
        dto.setCurrentRoleId(currentUser.getCurrentRoleId());
        // 请求令牌、刷新令牌保持不变
        dto.setAccessToken(null);
        dto.setRefreshToken(null);
        dto.setExpireTime(null);
        return dto;
    }

    @Override
    public List<ModuleTreeDto> getPermissionByRoleId(String roleId) {
        // 1. 角色合法判断
        boolean flag = CurrentUser.getInfo().getRoleList().contains(roleId);
        if (!flag) {
            throw new ParamErrorException("非法的角色ID");
        }
        // 2. 获取菜单树
        AppInfo appInfo = appInfoMapper.selectByAppkey(CurrentUser.getAppkey());
        List<ModuleTreeDto> moduleTree = moduleInfoService.listModuleTreeByRoleId(roleId, appInfo.getModuleId());
        // 3. 更新当前用户信息
        CurrentUser.UserInfo currentUser = CurrentUser.getInfo();
        currentUser.setCurrentRoleId(roleId);
        RedisUtil.put(BaseCacheKey.USER_INFO_PREFIX + CurrentUser.getToken(), currentUser, currentUser.getExpires());
        // 更新缓存的用户信息时，需同步更新缓存的token有效时长（防止拦截器或其他地方获取不到用户最新token）
        String loginTokenCacheKey = BaseCacheKey.USER_TOKEN_PREFIX + currentUser.getUserId() +":"+ CurrentUser.getAppkey();
        RedisUtil.put(loginTokenCacheKey, CurrentUser.getToken(), currentUser.getExpires());
        return moduleTree;
    }

    @Override
    public String getCaptcha(CaptchaParam param, String clientIp) {
        String scene = param.getScene();
        String captchaKey = buildCaptchaKey(scene);

        // login 场景：仅在密码出错次数触发标识时才生成
        if (StringUtil.isNull(scene) || "login".equals(scene)) {
            String captchaFlag = RedisUtil.get(PlatformCacheKey.USER_CAPTCHA_FLAG + captchaKey, String.class);
            if (StringUtil.isNull(captchaFlag)) {
                return "";
            }
        }
        // register / phone 场景：直接生成，无需检查 FLAG

        // 生成验证码
        String captchaValue = captchaUtil.getCaptchaValue();
        BufferedImage image = captchaUtil.generateCaptchaImage(captchaValue);
        try {
            String base64Image = captchaUtil.convertToWebBase64(image, "png");
            RedisUtil.put(PlatformCacheKey.USER_CAPTCHA_CODE + captchaKey, captchaValue, passwordErrorLockTime * 60);
            return base64Image;
        } catch (Exception e) {
            logger.error("设备[{}]场景[{}]生成验证码图片失败.", CurrentUser.getDeviceId(), scene, e);
            return "";
        }
    }

    @Override
    public String sendPhoneCaptcha(PhoneCaptchaParam param, String clientIp) {
        // 先校验图形码
        String captchaKey = buildCaptchaKey(param.getScene());
        String cached = RedisUtil.get(PlatformCacheKey.USER_CAPTCHA_CODE + captchaKey, String.class);
        // 验证码只能用一次
        RedisUtil.remove(PlatformCacheKey.USER_CAPTCHA_CODE + captchaKey);
        if (!param.getCaptcha().equalsIgnoreCase(cached)) {
            logger.warn("设备[{}]场景[{}]输入的图形验证码[{}]与缓存[{}]不一致",
                    CurrentUser.getDeviceId(), param.getScene(), param.getCaptcha(), cached);
            throw new UoquoException(AccountReturnCode.CAPTCHA_ERROR, "验证码不正确");
        }

        String scene = param.getScene();
        UserInfo user = userInfoMapper.selectByPhone(null, param.getPhone());
        if ("register".equals(scene)) {
            // 注册场景：手机号已存在则拒绝，否则用手机号作为 TOTP 密钥
            if (user != null) {
                throw new UoquoException(AccountReturnCode.PHONE_EXIST);
            }
            return userInfoService.sendPhoneCaptcha(param.getPhone(), param.getPhone());
        } else {
            // 登录场景（sms_login 及其他）：先查 userId，未注册则静默返回防枚举
            if (user == null) {
                logger.info("sendPhoneCaptcha: phone={} 未注册，静默返回", param.getPhone());
                return "";
            }
            return userInfoService.sendPhoneCaptcha(param.getPhone(), user.getId());
        }
    }

    @Override
    public UserAuthDto smsLogin(SmsLoginParam param, String clientIp) {
        CurrentUser.setClientIp(clientIp);
        CurrentUser.setAppVersion(param.getAppVersion());

        // 验证系统是否开启短信码登录
        String enableSetting = sysSettingService.getValueByCode(SettingsCode.LOGIN_SMS_ENABLE);
        if (!"true".equals(enableSetting)) {
            throw new ForbiddenException("系统未开启[短信码]的认证方式");
        }

        // 1. 查找用户（按手机号）
        UserInfo info = userInfoMapper.selectByPhone(null, param.getPhone());
        this.checkUserStatus(param.getPhone(), info);

        // 2. 验证短信码（以 userId 为 TOTP 密钥）
        String secret = Base32.encode(info.getId());
        boolean valid = TotpAuthUtils.verifyDynamicCode(secret, param.getSmsCode());
        if (!valid) {
            throw new UoquoException(AccountReturnCode.CAPTCHA_ERROR, "短信验证码不正确");
        }

        // 3. 跳过密码校验，走 MFA + Token 流程
        return this.completeLoginWithMfa(info, info.getPhone());
    }

    @Override
    public CredentialConfigDto credentialConfig(String scene) {
        if (!CredentialTypeEnum.contains(scene)) {
            throw new ParamErrorException("不支持的凭证类型：" + scene);
        }
        String state = IDGenerator.getUUID().toUpperCase();

        String appid;
        String agentId = null;
        String redirectUri;
        if (CredentialTypeEnum.WECHAT.getCode().equals(scene)) {
            appid       = this.getSysConfig(SettingsCode.WECHAT_APPID);
            redirectUri = this.getSysConfig(SettingsCode.WECHAT_REDIRECT_URI);
        } else {
            appid       = this.getSysConfig(SettingsCode.WECOM_CORPID);
            agentId     = this.getSysConfig(SettingsCode.WECOM_AGENTID);
            redirectUri = this.getSysConfig(SettingsCode.WECOM_REDIRECT_URI);
        }

        // 缓存 state → {scene, appid, agentId, status, code}，供回调与轮询使用
        Map<String, String> cache = new HashMap<>();
        cache.put("scene",   scene);
        cache.put("appid",   appid);
        cache.put("agentId", agentId);
        cache.put("status",  "waiting");
        cache.put("code",    "");
        RedisUtil.put(PlatformCacheKey.CREDENTIAL_STATE + state, JsonUtil.serialize(cache), 600);

        CredentialConfigDto dto = new CredentialConfigDto();
        dto.setScene(scene);
        dto.setAppid(appid);
        dto.setAgentId(agentId);
        dto.setRedirectUri(redirectUri);
        dto.setState(state);
        return dto;
    }

    @Override
    public CredentialStatusDto credentialStatus(String scene, String state) {
        String key = PlatformCacheKey.CREDENTIAL_STATE + state;
        String json = RedisUtil.get(key, String.class);
        if (StringUtil.isNull(json)) {
            throw new UoquoException(AccountReturnCode.CREDENTIAL_STATE_INVALID);
        }
        Map<String, Object> cache = JsonUtil.deserialize(json);
        // 校验场景一致，避免串用
        if (StringUtil.notNull(scene) && !scene.equals(cache.get("scene"))) {
            throw new ParamErrorException("场景与授权请求不一致");
        }
        String status = (String) cache.get("status");
        String code   = (String) cache.get("code");
        return new CredentialStatusDto(status, code);
    }

    @Override
    public void credentialCallback(String code, String state) {
        String key = PlatformCacheKey.CREDENTIAL_STATE + state;
        String json = RedisUtil.get(key, String.class);
        if (StringUtil.isNull(json)) {
            throw new UoquoException(AccountReturnCode.CREDENTIAL_STATE_INVALID);
        }
        Map<String, Object> cache = JsonUtil.deserialize(json);
        cache.put("code", code);
        cache.put("status", "confirmed");
        // 写回缓存（沿用 600s TTL）
        RedisUtil.put(key, JsonUtil.serialize(cache), 600);
    }

    @Override
    public UserAuthDto credentialLogin(CredentialLoginParam param, String clientIp) {
        CurrentUser.setClientIp(clientIp);
        CurrentUser.setAppVersion(param.getAppVersion());

        // 1. 枚举校验
        String credentialType = param.getCredentialType();
        if (!CredentialTypeEnum.contains(credentialType)) {
            throw new ParamErrorException("不支持的凭证类型：" + credentialType);
        }
        // 校验 state 有效性（须与 /credential/config 下发并缓存的一致），并校验场景一致
        String stateKey = PlatformCacheKey.CREDENTIAL_STATE + param.getState();
        String stateJson = RedisUtil.get(stateKey, String.class);
        if (StringUtil.isNull(stateJson)) {
            throw new UoquoException(AccountReturnCode.CREDENTIAL_STATE_INVALID);
        }
        Map<String, Object> stateCache = JsonUtil.deserialize(stateJson);
        if (!credentialType.equals(stateCache.get("scene"))) {
            throw new ParamErrorException("场景与授权请求不一致");
        }
        // state 一次性使用，校验通过后立即失效，防止重放
        RedisUtil.remove(stateKey);
        // 验证对应场景是否开启登录
        String enableSetting = sysSettingService.getValueByCode("login." + credentialType + ".enabled");
        if (!"true".equals(enableSetting)) {
            throw new ForbiddenException(String.format("系统未开启[%s]的认证方式", credentialType));
        }

        // 2. 解析凭证标识：微信/企微传入的是授权 code，需先换取 openid/userid
        String credentialValue = param.getCredentialValue();
        if (CredentialTypeEnum.WECHAT.getCode().equals(credentialType)) {
            credentialValue = wechatService.exchangeWechatOpenId(credentialValue);
        } else if (CredentialTypeEnum.WECOM.getCode().equals(credentialType)) {
            credentialValue = wechatService.exchangeWecomUserId(credentialValue);
        }

        // 3. 查询凭证表（全局类型 instituteId=null）
        String instituteId = resolveInstituteId(credentialType);
        UserCredential credential = credentialMapper.selectByCredentialType(credentialType, credentialValue, instituteId);

        if (credential != null) {
            // 4a. 已绑定 → 正常登录（含 MFA 判断）
            UserInfo info = userInfoMapper.selectByPrimaryKey(credential.getUserId());
            this.checkUserStatus(credentialValue, info);
            return this.completeLoginWithMfa(info, info.getUserName());
        } else {
            // 4b. 未绑定 → 生成 tempToken，返回最小 UserAuthDto
            String tempToken = IDGenerator.getUUID().toUpperCase();
            Map<String, String> bindInfo = new HashMap<>();
            bindInfo.put("credentialType",  credentialType);
            bindInfo.put("credentialValue", credentialValue);
            RedisUtil.put(PlatformCacheKey.BIND_TEMP_TOKEN + tempToken, JsonUtil.serialize(bindInfo), 300);
            UserAuthDto dto = new UserAuthDto();
            dto.setAccessToken(tempToken);
            return dto;
        }
    }

    @Override
    public UserAuthDto credentialBind(CredentialBindParam param, String clientIp) {
        CurrentUser.setClientIp(clientIp);
        CurrentUser.setAppVersion(param.getAppVersion());

        // 1. 读取绑定上下文
        String bindJson = RedisUtil.get(PlatformCacheKey.BIND_TEMP_TOKEN + param.getTempToken(), String.class);
        if (StringUtil.isNull(bindJson)) {
            throw new TokenEmptyException();
        }
        Map<String, Object> bindInfo = JsonUtil.deserialize(bindJson);
        String credentialType  = (String) bindInfo.get("credentialType");
        String credentialValue = (String) bindInfo.get("credentialValue");

        // 2. 账号查询 + 状态校验（复用 findUserByAccount）
        // 2.1 验证码判断（与 userLogin 一致）
        String captchaKey = CurrentUser.getDeviceId() + ":" + CurrentUser.getAppkey();
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
        // 2.2 用户状态判断
        UserInfo info = findUserByAccount(param.getAccount());
        this.checkUserStatus(param.getAccount(), info);

        // 3. 密码校验（复用 checkAndVerifyPassword，含错误计数与验证码锁定）
        this.checkAndVerifyPassword(info, param.getPassword(), param.getAccount(), clientIp);

        // 4. 写入凭证（upsert） — id 由 IDGenerator.getNextULID() 生成
        String instituteId = resolveInstituteId(credentialType);
        credentialMapper.upsertCredential(IDGenerator.getNextULID(), info.getId(), credentialType, credentialValue, instituteId);

        // 5. 清理 tempToken 
        RedisUtil.remove(PlatformCacheKey.BIND_TEMP_TOKEN + param.getTempToken());
        RedisUtil.remove(PlatformCacheKey.USER_CAPTCHA_FLAG + captchaKey);

        // 6. 完成登录
        return this.completeLoginWithMfa(info, info.getUserName());
    }

    @Override
    public void resetPassword(ResetPasswordParam param, String clientIp) {
        CurrentUser.setClientIp(clientIp);

        // 1. 查找用户（按手机号）
        UserInfo info = userInfoMapper.selectByPhone(null, param.getPhone());
        // 防枚举：用户不存在与短信码错误返回同一错误码
        if (info == null) {
            logger.info("resetPassword: phone={} 未注册", param.getPhone());
            throw new UoquoException(AccountReturnCode.CAPTCHA_ERROR, "验证码错误或手机号未注册");
        }

        // 2. 校验短信码（以 userId 为 TOTP 密钥，与发码逻辑一致）
        String secret = Base32.encode(info.getId());
        boolean valid = TotpAuthUtils.verifyDynamicCode(secret, param.getSmsCode());
        if (!valid) {
            throw new UoquoException(AccountReturnCode.CAPTCHA_ERROR, "短信验证码不正确");
        }

        // 3. 重置密码（无需旧密码）
        userInfoService.resetPassword(info.getId(), param.getNewPassword());
    }

    @Override
    public void register(RegisterParam param, String clientIp) {
        CurrentUser.setClientIp(clientIp);

        // 1. 校验系统是否开启注册
        String registerSetting = sysSettingService.getValueByCode(SettingsCode.REGISTER_ENABLE);
        if (!"true".equals(registerSetting)) {
            throw new UoquoException(AccountReturnCode.REGISTER_DISABLED);
        }

        // 2. 校验短信码（注册场景：用户尚不存在，以 phone 为 TOTP 密钥，与发码逻辑一致）
        String secret = Base32.encode(param.getPhone());
        boolean valid = TotpAuthUtils.verifyDynamicCode(secret, param.getSmsCode());
        if (!valid) {
            throw new UoquoException(AccountReturnCode.CAPTCHA_ERROR, "短信验证码不正确");
        }

        // 3. 创建用户（唯一性校验由 addUserInfo 内部完成）
        UserAddParam addParam = new UserAddParam();
        addParam.setInstituteId(param.getInstituteId());
        addParam.setPhone(param.getPhone());
        addParam.setUserName(param.getUserName());
        addParam.setPassword(param.getPassword());
        addParam.setRealName(param.getRealName());
        userInfoService.addUserInfo(addParam);
    }

    /**
     * 用户认证：校验用户状态
     */
    private void checkUserStatus(String account, UserInfo info) {
        // 1.1 校验用户存在
        if (info == null) {
            logger.warn("用户[{}]不存在", account);
            this.publishEvent(BusinessOperationEnum.LOGIN, AccountReturnCode.ACCOUNT_NOT_EXIST, "USER", null, null, account, null, null);
            throw new UoquoException(AccountReturnCode.ACCOUNT_PASSWORD_ERROR);
        }
        // 1.2 校验用户状态
        if (!BaseConstant.NOT_DELETED.equals(info.getDeleteState())) {
            logger.warn("用户[{}][{}]已标记为删除，不允许登录", account, info.getId());
            this.publishEvent(BusinessOperationEnum.LOGIN, AccountReturnCode.ACCOUNT_DELETE, "USER", info.getId(), info.getInstituteId(), account, null, null);
            throw new UoquoException(AccountReturnCode.ACCOUNT_PASSWORD_ERROR, "账户不可用！");
        } else if (!DictionaryCodeEnum.STATE_NORMAL.getCode().equals(info.getStatus())) {
            logger.warn("用户[{}][{}]的状态为[{}]，不允许登录", account, info.getId(), info.getStatus());
            this.publishEvent(BusinessOperationEnum.LOGIN, AccountReturnCode.ACCOUNT_DISABLE, "USER", info.getId(), info.getInstituteId(), account, null, null);
            throw new UoquoException(AccountReturnCode.ACCOUNT_PASSWORD_ERROR, "账户不可用！");
        }
        // 1.3 校验用户锁定状态（连续出错5次提醒）
        int loginErrorCount = info.getLoginErrorCount() == null ? 0 : info.getLoginErrorCount();
        if (loginErrorCount >= passwordErrorMaxNum) {
            long ms = System.currentTimeMillis() - info.getLastedLoginTime().getTime() - passwordErrorLockTime * 60 * 1000L;
            int second = (int) Math.ceil((double) ms / 1_000);
            if (second < 0) {
                logger.warn("用户[{}][{}]被锁定，还剩[{}]秒，不允许登录", account, info.getId(), second);
                this.publishEvent(BusinessOperationEnum.LOGIN, AccountReturnCode.ACCOUNT_LOCK, "USER", info.getId(), info.getInstituteId(), account, null, null);
                throw new UoquoException(AccountReturnCode.ACCOUNT_PASSWORD_ERROR, "密码错误超过 %d 次，账号已锁定，请稍后重试！", passwordErrorMaxNum);
            } else {
                // 超过锁定时间，则重置登录错误次数
                UserInfo paramUser = new UserInfo();
                paramUser.setId(info.getId());
                paramUser.setLastedLoginIp(CurrentUser.getClientIp());
                paramUser.setLastedLoginTime(new Date());
                paramUser.setLoginErrorCount(0);
                userInfoMapper.updateLastLoginInfo(paramUser);
            }
        }
        // 1.4 校验机构状态
        InstituteInfo institute = instituteInfoMapper.selectByPrimaryKey(info.getInstituteId());
        if (!BaseConstant.NOT_DELETED.equals(institute.getDeleteState())) {
            logger.warn("用户[{}][{}]所属机构[{}]已标记为删除，不允许登录", account, info.getId(), info.getInstituteId());
            this.publishEvent(BusinessOperationEnum.LOGIN, InstituteReturnCode.INST_DELETE, "USER", info.getId(), info.getInstituteId(), account, null, null);
            throw new UoquoException(AccountReturnCode.ACCOUNT_PASSWORD_ERROR, "账户不可用！");
        } else if (DictionaryCodeEnum.INSTITUTE_STATUS_DISABLE.getCode().equals(institute.getStatus())) {
            logger.warn("用户[{}][{}]所属机构[{}]状态为[{}]，不允许登录", account, info.getId(), info.getInstituteId(), institute.getStatus());
            this.publishEvent(BusinessOperationEnum.LOGIN, InstituteReturnCode.INST_DISABLE, "USER", info.getId(), info.getInstituteId(), account, null, null);
            throw new UoquoException(AccountReturnCode.ACCOUNT_PASSWORD_ERROR, "账户不可用！");
        }
    }

    /**
     * 密码校验：验证密码哈希，并维护连续失败计数和验证码锁定。
     * 失败时抛出 ACCOUNT_PASSWORD_ERROR，成功时重置计数。
     *
     * @param info        已查到的用户信息
     * @param rawPassword 入参密码（解密后的明文）
     * @param account     登录账号（仅用于日志/事件）
     * @param clientIp    客户端 IP，写入最近登录信息
     */
    private void checkAndVerifyPassword(UserInfo info, String rawPassword, String account, String clientIp) {
        UserInfo paramUser = new UserInfo();
        paramUser.setId(info.getId());
        paramUser.setLastedLoginIp(clientIp);
        paramUser.setLastedLoginTime(new Date());
        boolean ok = UserUtils.checkPassword(rawPassword, info.getPassword());
        if (!ok) {
            int errorCount = info.getLoginErrorCount() == null ? 0 : info.getLoginErrorCount();
            if (errorCount >= 1) {
                String captchaKey = CurrentUser.getDeviceId() + ":" + CurrentUser.getAppkey();
                RedisUtil.put(PlatformCacheKey.USER_CAPTCHA_FLAG + captchaKey, "1", passwordErrorLockTime * 60);
            }
            paramUser.setLoginErrorCount(++errorCount);
            userInfoMapper.updateLastLoginInfo(paramUser);
            logger.warn("用户[{}][{}]密码连续输错[{}]次，不允许登录", account, info.getId(), errorCount);
            this.publishEvent(BusinessOperationEnum.LOGIN, AccountReturnCode.PASSWORD_ERROR,
                    "USER", info.getId(), info.getInstituteId(), account, null, rawPassword);
            throw new UoquoException(AccountReturnCode.ACCOUNT_PASSWORD_ERROR,
                    "密码错误,还可以输入 %d 次", (passwordErrorMaxNum - errorCount));
        }
        paramUser.setLoginErrorCount(0);
        userInfoMapper.updateLastLoginInfo(paramUser);
    }

    /**
     * MFA 判断 + 完成登录。
     * 若 MFA 已启用且已绑定：生成 TOTP 临时 Token，返回最小化 UserAuthDto；
     * 否则：缓存用户信息，发布登录事件，返回完整 UserAuthDto。
     *
     * @param info    已通过状态校验的用户信息
     * @param account 登录账号（用于事件日志，可传手机号/用户名/凭证值）
     */
    private UserAuthDto completeLoginWithMfa(UserInfo info, String account) {
        CurrentUser.setToken(null);
        // 获取 MFA 配置（用户 > 机构 > 系统）
        String setting = userSettingService.getValueByCode(info.getId(), SettingsCode.MFA_AUTH_ENABLED);
        String totpStatus = "unbound";
        if (!"true".equals(setting)) {
            totpStatus = "disabled";
        } else if (StringUtil.notNull(info.getTotpSecret())) {
            totpStatus = "enabled";
        }
        UserAuthDto dto;
        if ("enabled".equals(totpStatus)) {
            // MFA 已绑定：生成临时 Token，不返回和缓存用户信息
            String tempToken = this.generateToken();
            dto = new UserAuthDto();
            dto.setTotpStatus(totpStatus);
            dto.setAccessToken(tempToken);
            dto.setRefreshToken(null);
            RedisUtil.put(PlatformCacheKey.TOTP_TEMP_TOKEN + tempToken, info.getId(), 300);
            // 仅用于本次日志
            this.setCurrentUserInfo(this.getUserAuthDto(info));
        } else {
            // MFA 未开启或未绑定：正常登录流程
            dto = this.getUserAuthDto(info);
            dto.setTotpStatus(totpStatus);
            this.cacheUser2Redis(CurrentUser.getToken(), dto, false);
            // 发布事件（登录）
            this.publishEvent(BusinessOperationEnum.LOGIN, SystemReturnCode.SUCCESS,
                    "USER", info.getId(), info.getInstituteId(), account, dto.getAccessToken(), null);
        }
        return dto;
    }

    /**
     * 用户认证：补齐用户其他信息
     */
    private UserAuthDto getUserAuthDto(UserInfo info) {
        UserAuthDto dto = new UserAuthDto();
        BeanUtils.copyProperties(info, dto);
        // 头像补充前缀
        if (StringUtil.notNull(dto.getAvatar())) {
            dto.setAvatar(staticHost + dto.getAvatar());
        }
        // 补全机构信息
        InstituteInfo institute = instituteInfoMapper.selectByPrimaryKey(info.getInstituteId());
        if (institute != null) {
            dto.setInstituteName(institute.getInstituteName());
            dto.setRoleGroup(institute.getRoleGroup());
        }
        // 用户分组
        List<GroupDto> groupList = userInfoService.listGroupByUserId(info.getId());
        dto.setGroupList(groupList);
        // 用户角色
        List<UserRoleDto> roleList = userInfoService.listRoleInfoByUserId(info.getId());
        dto.setRoleList(roleList);
        // 用户token
        dto.setAccessToken(this.generateToken());
        // 用户token过期时间（默认30分钟）
        int timeout = 1800;
        try {
            Integer num = RedisUtil.get(SettingsCode.SESSION_TIMEOUT, Integer.class);
            if ((num != null) && (num > 0)) {
                timeout = num * 60;
            }
        } catch (Exception e) {
            // do nothing
        }
        dto.setExpireTime(timeout);
        // 刷新token
        dto.setRefreshToken(IDGenerator.getUUID().toUpperCase());
        return dto;
    }

    /**
     * 用户认证：缓存用户对象到Redis
     */
    private void cacheUser2Redis(String oldAccessToken, UserAuthDto userDto, boolean isRefresh) {
        // 补充用户信息
        this.setCurrentUserInfo(userDto);
        // 缓存用户信息
        String token  = CurrentUser.getToken();
        String userId = CurrentUser.getInfo().getUserId();
        // 用户单点登录控制（同一账号只允许一端登录，如果是刷新token，则不需要下发踢出事件）
        String loginTokenCacheKey = BaseCacheKey.USER_TOKEN_PREFIX + userId +":"+ CurrentUser.getAppkey();
        String cacheAccessToken = RedisUtil.get(loginTokenCacheKey, String.class);
        if (cacheAccessToken != null && !isRefresh) {
            // 删除缓存的token
            this.clearToken2Redis(cacheAccessToken);
            // 发布事件（被踢）
            this.publishEvent(BusinessOperationEnum.LOGOUT, SystemReturnCode.ACCOUNT_KICK_OUT, "USER", userDto.getId(), userDto.getInstituteId(), userDto.getUserName(), cacheAccessToken, null);
        }
        // 删除缓存的token
        if (StringUtil.notNull(oldAccessToken) && !oldAccessToken.equals(cacheAccessToken)) {
            this.clearToken2Redis(oldAccessToken);
        }
        // 放入新token
        RedisUtil.put(BaseCacheKey.USER_INFO_PREFIX + token, CurrentUser.getInfo(), userDto.getExpireTime());
        RedisUtil.put(loginTokenCacheKey, token, userDto.getExpireTime());
        // 刷新token仅当前设备可用（时间较长，默认7天）
        Map<String, String> map = new HashMap<>();
        map.put("userId",   userDto.getId());
        map.put("appkey",   CurrentUser.getAppkey());
        map.put("deviceId", CurrentUser.getDeviceId());
        // 用户名和真实姓名放入（运维模式时为前端传入的手机号，便于后续查找日志并在刷新时保留运维标识）
        map.put("userName", userDto.getUserName());
        map.put("realName", userDto.getRealName());
        map.put("opsMode",  String.valueOf(CurrentUser.getInfo().isOpsMode()));
        RedisUtil.put(BaseCacheKey.USER_TOKEN_REFRESH + userDto.getRefreshToken(), map, 60 * refreshTimeout);
        RedisUtil.put(BaseCacheKey.USER_TOKEN_REFRESH + userDto.getAccessToken(), userDto.getRefreshToken(), 60 * refreshTimeout);
    }

    /**
     * 设置当前用户信息
     */
    private void setCurrentUserInfo(UserAuthDto userDto) {
        CurrentUser.setToken(userDto.getAccessToken());
        // 补充用户信息
        CurrentUser.getInfo().setUserId(userDto.getId());
        CurrentUser.getInfo().setUserName(userDto.getUserName());
        CurrentUser.getInfo().setRealName(userDto.getRealName());
        CurrentUser.getInfo().setInstituteId(userDto.getInstituteId());
        if (userDto.getRoleList() != null) {
            List<String> roleIds = userDto.getRoleList().stream().map(UserRoleDto::getId).collect(Collectors.toList());
            CurrentUser.getInfo().setRoleList(roleIds);
        }
        if (userDto.getGroupList() != null) {
            List<String> groupIds = userDto.getGroupList().stream().map(GroupDto::getId).collect(Collectors.toList());
            CurrentUser.getInfo().setGroupList(groupIds);
        }
        CurrentUser.getInfo().setExpires(userDto.getExpireTime());
    }

    /**
     * 清除缓存的token相关信息
     */
    private void clearToken2Redis(String accessToken) {
        // 删除 refresh token
        String refreshToken = RedisUtil.get(BaseCacheKey.USER_TOKEN_REFRESH + accessToken, String.class);
        RedisUtil.remove(BaseCacheKey.USER_TOKEN_REFRESH + refreshToken);
        RedisUtil.remove(BaseCacheKey.USER_TOKEN_REFRESH + accessToken);
        // 删除 access token
        RedisUtil.remove(BaseCacheKey.USER_INFO_PREFIX + accessToken);
    }

    /**
     * 应用认证：校验状态
     */
    private void checkAppStatus(String account, AppInfo app) {
        // 1.1 校验账号存在
        if (app == null) {
            logger.warn("应用[{}]不存在", account);
            this.publishEvent(BusinessOperationEnum.LOGIN, AccountReturnCode.ACCOUNT_NOT_EXIST, "APP", null, null, account, null, null);
            throw new UoquoException(AccountReturnCode.ACCOUNT_PASSWORD_ERROR);
        }
        // 1.2 校验状态
        if (!BaseConstant.NOT_DELETED.equals(app.getDeleteState())) {
            logger.warn("应用[{}][{}]已标记为删除，不允许登录", app.getAppkey(), app.getId());
            this.publishEvent(BusinessOperationEnum.LOGIN, AccountReturnCode.ACCOUNT_DELETE, "APP", app.getId(), app.getInstituteId(), app.getAppkey(), null, null);
            throw new UoquoException(AccountReturnCode.ACCOUNT_PASSWORD_ERROR, "应用不可用！");
        } else if (!DictionaryCodeEnum.STATE_NORMAL.getCode().equals(app.getStatus())) {
            logger.warn("应用[{}][{}]的状态为[{}]，不允许登录", app.getAppkey(), app.getId(), app.getStatus());
            this.publishEvent(BusinessOperationEnum.LOGIN, AccountReturnCode.ACCOUNT_DISABLE, "APP", app.getId(), app.getInstituteId(), app.getAppkey(), null, null);
            throw new UoquoException(AccountReturnCode.ACCOUNT_PASSWORD_ERROR, "应用不可用！");
        }
        // 1.4 校验机构状态
        if (StringUtil.isNull(app.getInstituteId())) {
            // 内置APP无机构信息，不需要校验
            return;
        }
        InstituteInfo institute = instituteInfoMapper.selectByPrimaryKey(app.getInstituteId());
        if (!BaseConstant.NOT_DELETED.equals(institute.getDeleteState())) {
            logger.warn("应用[{}][{}]所属机构[{}]已标记为删除，不允许登录", app.getAppkey(), app.getId(), app.getInstituteId());
            this.publishEvent(BusinessOperationEnum.LOGIN, InstituteReturnCode.INST_DELETE, "APP", app.getId(), app.getInstituteId(), app.getAppkey(), null, null);
            throw new UoquoException(AccountReturnCode.ACCOUNT_PASSWORD_ERROR, "应用不可用！");
        } else if (DictionaryCodeEnum.INSTITUTE_STATUS_DISABLE.getCode().equals(institute.getStatus())) {
            logger.warn("应用[{}][{}]所属机构[{}]状态为[{}]，不允许登录", app.getAppkey(), app.getId(), app.getInstituteId(), institute.getStatus());
            this.publishEvent(BusinessOperationEnum.LOGIN, InstituteReturnCode.INST_DISABLE, "APP", app.getId(), app.getInstituteId(), app.getAppkey(), null, null);
            throw new UoquoException(AccountReturnCode.ACCOUNT_PASSWORD_ERROR, "应用不可用！");
        }
    }

    /**
     * 应用认证：补齐其他信息，并缓存
     */
    private TokenDto getAppAuthDto(AppInfo app) {
        // 1. 生成token
        TokenDto dto = new TokenDto();
        String appTokenKey = BaseCacheKey.APPKEY_TOKEN_PREFIX + app.getAppkey();
//        // 方法1：有效时长内重复使用：使用友好，但不安全
//        Long timeout = RedisUtil.expire(appTokenKey);
//        if (timeout == null || timeout <= 100) {
//            String token = generateToken();
//            dto.setToken(token);
//            RedisUtil.put(appTokenKey, token, 60 * appTokenTimeout);
//        } else {
//            String token = RedisUtil.get(appTokenKey, String.class);
//            dto.setToken(token);
//        }
        // 方法2：每次生成新的：可防止多平台使用同一个账号的情况
        dto.setAccessToken(generateToken());
        dto.setRefreshToken(IDGenerator.getUUID().toUpperCase());
        dto.setExpireTime(60 * appTokenTimeout);
        // 2. 缓存
        RedisUtil.put(appTokenKey, dto.getAccessToken(), 60 * appTokenTimeout);
        RedisUtil.put(BaseCacheKey.APPKEY_TOKEN_REFRESH + dto.getRefreshToken(), app.getAppkey(), 60 * refreshTimeout);
        return dto;
    }

    private String generateToken() {
        String str1 = IDGenerator.getUUID().toUpperCase();
        String str2 = IDGenerator.getNextULID();
        // 如果有旧token，则保持原有时间序列，方便日志追踪
        String oldToken = CurrentUser.getToken();
        if (StringUtil.notNull(oldToken)) {
            str2 = oldToken.substring(0, str2.length());
        }
        return str2 + str1.substring(str2.length());
    }

    /**
     * 发布账户相关事件
     */
    private void publishEvent(BusinessOperationEnum type, BaseReturnCode status, String subType, String businessId, String instituteId, String account, String token, String password) {
        RemoteEvent<AuthInfo> event = new RemoteEvent<>(BusinessTypeEnum.AUTH.getCode(), type.getCode(), status.getCode());
        event.setBusinessSubType(subType);
        event.setBusinessId(businessId);
        event.setBusinessInstituteId(instituteId);
        event.setRemarks(status.getText());
        // 登录、登出、被踢时，手动塞入token（此时从 CurrentUser 中获取不到）
        event.setToken(token);
        // 密码校验出错时，记录当时的错误密码
        if (StringUtil.notNull(password)) {
            event.addExtension("password", password);
        }
        // 登录的其他信息
        AuthInfo info = new AuthInfo();
        info.setAccount(account);
        info.setPassword(password);
        event.setNewData(info);

        eventPublisher.publishEvent(event);
    }

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

    /**
     * 读取系统级配置项，缺失则抛参数异常
     */
    private String getSysConfig(String code) {
        String setting = sysSettingService.getValueByCode(code);
        if (StringUtil.isNull(setting)) {
            throw new ParamErrorException("缺少配置项：" + code);
        }
        return setting;
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    /**
     * 构建图形验证码缓存 key。
     * 登录场景（login 或空）沿用 deviceId:appkey，与密码错误锁定 FLAG 保持一致；
     * 其他场景（register、phone 等）追加场景类型，避免不同场景的图形验证码相互覆盖。
     */
    private String buildCaptchaKey(String scene) {
        String base = CurrentUser.getDeviceId() + ":" + CurrentUser.getAppkey();
        if (StringUtil.isNull(scene) || "login".equals(scene)) {
            return base;
        }
        return base + ":" + scene;
    }

    /**
     * 解析 instituteId：当前版本 wechat 全局类型返回 null，wecom 返回当前 appkey 对应机构
     */
    private String resolveInstituteId(String credentialType) {
        if (CredentialTypeEnum.WECHAT.getCode().equals(credentialType)) {
            return null;
        }
        // TODO 根据请求域名、请求入参等信息来判断
        // wecom 等机构范围类型：从当前 AppInfo 中取机构ID
        AppInfo appInfo = appInfoMapper.selectByAppkey(CurrentUser.getAppkey());
        return appInfo != null ? appInfo.getInstituteId() : null;
    }

    // ============================ 运维登录 ============================
    @Override
    public OpsConfigDto opsConfig(OpsConfigParam param) {
        // account、phone 由 @Valid 保证非空，phone 在此仅作合法性/必填校验
        String serial  = this.getSysConfig(SettingsCode.SERIAL_NUMBER);
        String corpId  = this.getSysConfig(SettingsCode.OPS_WECOM_CORPID);
        String agentId = this.getSysConfig(SettingsCode.OPS_WECOM_AGENTID);
        String redirectUri = this.getSysConfig(SettingsCode.OPS_WECOM_REDIRECT_URI);

        // state = TAES(SERIAL_NUMBER + "|" + account)
        String state = TimeStepCryptoUtil.encryptTAES(serial + "|" + param.getAccount(), 30);

        // 按企业微信 OAuth2.0 构建授权地址
        String authUrl = "https://open.weixin.qq.com/connect/oauth2/authorize"
                + "?appid=" + this.urlEncode(corpId)
                + "&redirect_uri=" + this.urlEncode(redirectUri)
                + "&response_type=code"
                + "&scope=snsapi_privateinfo"
                + "&state=" + this.urlEncode(state)
                + "&agentid=" + this.urlEncode(agentId)
                + "#wechat_redirect";

        String qrCode;
        try {
            qrCode = TotpAuthUtils.generateQrcode(authUrl);
        } catch (Exception e) {
            logger.error("生成运维二维码失败", e);
            throw new UoquoException(AccountReturnCode.OPS_AUTH_FAILED, "生成二维码失败");
        }
        return new OpsConfigDto(qrCode);
    }

    @Override
    public UserAuthDto opsLogin(OpsLoginParam param, String clientIp) {
        CurrentUser.setClientIp(clientIp);
        CurrentUser.setAppVersion(param.getAppVersion());

        // 1. 锁定校验：连续失败 5 次后 24 小时内不可用
        String phone = param.getPhone();
        String failKey = PlatformCacheKey.OPS_LOGIN_FAIL + phone;
        String lockKey = PlatformCacheKey.OPS_LOGIN_LOCK + phone;
        if (StringUtil.notNull(RedisUtil.get(lockKey, String.class))) {
            throw new UoquoException(AccountReturnCode.OPS_LOGIN_LOCKED);
        }

        UserInfo info;
        try {
            // 2. 动态码校验
            // 以 Base32(ACTIVATE_CODE + phone) 为密钥校验动态口令
            // TODO 实际中从授权文件中读取激活码（ACTIVATE_CODE），此处临时用SERIAL_NUMBER代替
            String serial = this.getSysConfig(SettingsCode.SERIAL_NUMBER);
            String secret = Base32.encode(serial + phone);
            boolean valid = TotpAuthUtils.verifyDynamicCode(secret, param.getDynamicCode());
            if (!valid) {
                throw new UoquoException(AccountReturnCode.OPS_AUTH_FAILED);
            }
            // 3. 校验账号状态
            info = findUserByAccount(param.getAccount());
            this.checkUserStatus(param.getAccount(), info);
        } catch (AbstractBaseException e) {
            // 失败计数，连续 5 次锁定 24 小时
            long fails = this.incrOpsFail(failKey);
            logger.warn("运维人员[{}]第[{}]次登录账号[{}]失败。", phone, fails, param.getAccount(), e);
            if (fails >= 5) {
                RedisUtil.put(lockKey, "1", 24 * 60 * 60);
                logger.error("运维人员[{}]连续[{}]次登录失败，将锁定24小时。", phone, fails);
            }
            throw new UoquoException(AccountReturnCode.OPS_AUTH_FAILED);
        }
        // 4. 校验通过：清理失败计数，忽略 MFA 直接登录
        RedisUtil.remove(failKey);
        // 组装用户信息
        CurrentUser.setToken(null);
        // 标记运维模式（在缓存前设置，确保写入 USER_INFO 与刷新码）
        CurrentUser.getInfo().setOpsMode(true);
        UserAuthDto dto = this.getUserAuthDto(info);
        dto.setTotpStatus("disabled");
        // 运维模式下对外仅展示手机号
        dto.setUserName(phone);
        dto.setRealName(phone);
        this.cacheUser2Redis(CurrentUser.getToken(), dto, false);
        this.publishEvent(BusinessOperationEnum.LOGIN, SystemReturnCode.SUCCESS, "USER", info.getId(), info.getInstituteId(), phone, dto.getAccessToken(), null);
        return dto;
    }

    @Override
    public String opsMfa(String code, String state) {
        String serial  = null;
        String account = null;
        String phone   = null;
        try {
            // 1. 解码 state 得到序列号（与账号）
            String decoded = TimeStepCryptoUtil.decryptTAES(state, 30);
            String[] parts = decoded.split("\\|", 2);
            if (parts.length != 2) {
                logger.error("回传参数不合法：state={}, decoded={}, parts.length={}", state, decoded, parts.length);
                throw new RuntimeException("回传参数state不合法");
            }
            serial  = parts[0];
            account = parts[1];

            // 2. 通过 code 换取运维用户手机号（先 ticket 后 mobile）
            phone = wechatService.exchangeOpsWecomMobile(code);
            logger.info("运维人员[{}]对系统[{}]用账户[{}]进行运维，授权成功。", phone, serial, account);

            // 3. 以 Base32(ACTIVATE_CODE + phone) 生成动态口令
            // TODO 实际中用序列号（SERIAL_NUMBER）查出对应的激活码（ACTIVATE_CODE），此处临时用SERIAL_NUMBER代替
            String secret = Base32.encode(serial + phone);
            String dynamicCode = TotpAuthUtils.generateDynamicCode(secret);
            // TODO 目前仅作记录，实际使用中需配合权限校验

            return wechatService.opsMfaHtml(true, dynamicCode);
        } catch (Exception e) {
            logger.error("运维人员[{}]对系统[{}]用账户[{}]进行运维，授权失败{code={}, state={}}。", phone, serial, account, code, state, e);
            return wechatService.opsMfaHtml(false, "授权失败，请联系管理员。");
        }
    }

    /**
     * 运维登录失败计数自增（24 小时窗口）。
     */
    private long incrOpsFail(String failKey) {
        Integer fails = RedisUtil.get(failKey, Integer.class);
        int next = (fails == null ? 0 : fails) + 1;
        RedisUtil.put(failKey, next, 24 * 60 * 60);
        return next;
    }

    /**
     * 紧急登录失败计数自增（24 小时窗口）。
     */
    private long incrEmergencyFail(String failKey) {
        Integer fails = RedisUtil.get(failKey, Integer.class);
        int next = (fails == null ? 0 : fails) + 1;
        RedisUtil.put(failKey, next, 24 * 60 * 60);
        return next;
    }
}
