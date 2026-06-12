package com.uoquo.platform.auth.service.impl;

import com.uoquo.cloud.events.RemoteEvent;
import com.uoquo.platform.auth.model.dto.TokenDto;
import com.uoquo.platform.auth.model.dto.UserAuthDto;
import com.uoquo.platform.auth.model.pojo.AuthInfo;
import com.uoquo.platform.auth.model.param.UserLoginParam;
import com.uoquo.platform.common.utils.TotpAuthUtils;
import com.uoquo.platform.auth.service.AuthService;
import com.uoquo.platform.common.*;
import com.uoquo.platform.common.exception.AccountReturnCode;
import com.uoquo.platform.common.exception.InstituteReturnCode;
import com.uoquo.platform.common.utils.UserUtils;
import com.uoquo.platform.institute.mapper.InstituteInfoMapper;
import com.uoquo.platform.institute.model.pojo.InstituteInfo;
import com.uoquo.platform.role.model.dto.ModuleTreeDto;
import com.uoquo.platform.role.service.ModuleInfoService;
import com.uoquo.platform.system.mapper.AppInfoMapper;
import com.uoquo.platform.system.model.pojo.AppInfo;
import com.uoquo.platform.user.mapper.UserInfoMapper;
import com.uoquo.platform.user.model.dto.GroupDto;
import com.uoquo.platform.user.model.dto.UserRoleDto;
import com.uoquo.platform.user.model.pojo.UserInfo;
import com.uoquo.platform.user.service.UserInfoService;
import com.uoquo.platform.user.service.UserSettingService;
import com.uoquo.utils.CurrentUser;
import com.uoquo.utils.IDGenerator;
import com.uoquo.utils.StringUtil;
import com.uoquo.platform.common.BaseConstant;
import com.uoquo.web.BaseCacheKey;
import com.uoquo.web.BaseReturnCode;
import com.uoquo.web.SystemReturnCode;
import com.uoquo.web.events.UoquoEventPublisher;
import com.uoquo.web.exception.*;
import com.uoquo.utils.spring.CaptchaUtil;
import com.uoquo.utils.spring.RedisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.util.*;
import java.util.stream.Collectors;

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
    public UserAuthDto userLogin(UserLoginParam param, String clientIp) {
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
        UserInfo info = userInfoMapper.selectByLogin(null, param.getAccount());
        this.checkUserStatus(param.getAccount(), info);
        // 2. 密码校验
        UserInfo paramUser = new UserInfo();
        paramUser.setId(info.getId());
        paramUser.setLastedLoginIp(clientIp);
        paramUser.setLastedLoginTime(new Date());
        String password = param.getPassword();
        boolean checkPassword = UserUtils.checkPassword(password, info.getPassword());
        if (!checkPassword) {
            int loginErrorCount = info.getLoginErrorCount() == null ? 0 : info.getLoginErrorCount();
            if (loginErrorCount >= 1) {
                // TODO 应该采用“增强验证码流程”多维度风险评估（失败次数、时间密度、IP地址、设备指纹等）， 连续两次出错，则需要填验证码
                RedisUtil.put(PlatformCacheKey.USER_CAPTCHA_FLAG + captchaKey, "1", passwordErrorLockTime * 60);
            }
            paramUser.setLoginErrorCount(++loginErrorCount);
            userInfoMapper.updateLastLoginInfo(paramUser);
            logger.warn("用户[{}][{}]密码连续输错[{}]次，不允许登录", param.getAccount(), info.getId(), loginErrorCount);
            this.publishEvent(BusinessOperationEnum.LOGIN, AccountReturnCode.PASSWORD_ERROR, "USER", info.getId(), info.getInstituteId(), param.getAccount(), null, password);
            throw new UoquoException(AccountReturnCode.ACCOUNT_PASSWORD_ERROR, "密码错误,还可以输入 %d 次", (passwordErrorMaxNum - loginErrorCount));
        } else {
            paramUser.setLoginErrorCount(0);
            userInfoMapper.updateLastLoginInfo(paramUser);
        }
        // TODO 增加地理位置验证增强方案

        // 删除验证码标识
        RedisUtil.remove(PlatformCacheKey.USER_CAPTCHA_FLAG + captchaKey);

        // 3. 校验通过，判断MFA双因子状态
        CurrentUser.setToken(null);
        UserAuthDto dto = this.getUserAuthDto(info);
        // 获取MFA配置（用户 > 机构 > 系统)
        String setting = userSettingService.getValueByCode(info.getId(), SettingsCode.MFA_AUTH_ENABLED);
        if (!"true".equals(setting)) {
            dto.setTotpStatus("disabled");
        } else if (StringUtil.notNull(info.getTotpSecret())) {
            dto.setTotpStatus("enabled");
        } else {
            dto.setTotpStatus("unbound");
        }
        if ("enabled".equals(dto.getTotpStatus())) {
            // MFA已绑定：生成临时Token，不缓存用户信息
            // 生成临时Token（仅用于TOTP验证）
            String tempToken = this.generateToken();
            dto.setAccessToken(tempToken);
            dto.setRefreshToken(null);
            // 设置用户信息（主要用于日志记录）
            this.setCurrentUserInfo(dto);
            // 临时Token缓存用户ID（5分钟有效）
            RedisUtil.put(PlatformCacheKey.TOTP_TEMP_TOKEN + tempToken, info.getId(), 300);
        } else {
            // MFA未开启或未绑定：正常登录流程
            // 缓存用户信息
            this.cacheUser2Redis(CurrentUser.getToken(), dto, false);
            // 发布事件（登录）
            this.publishEvent(BusinessOperationEnum.LOGIN, SystemReturnCode.SUCCESS, "USER", info.getId(), info.getInstituteId(), param.getAccount(), dto.getAccessToken(), null);
        }
        return dto;
    }

    @Override
    public UserAuthDto totpLogin(String tempToken, String totpCode) {
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
        this.publishEvent(BusinessOperationEnum.LOGIN, SystemReturnCode.SUCCESS,
                "USER", info.getId(), info.getInstituteId(), info.getUserName(), dto.getAccessToken(), null);
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
    public TokenDto appLogin(UserLoginParam param, String clientIp) {
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
    public String getCaptcha(UserLoginParam param, String clientIp) {
        String captchaKey = CurrentUser.getDeviceId() + ":" + CurrentUser.getAppkey();
        String captchaFlag = RedisUtil.get(PlatformCacheKey.USER_CAPTCHA_FLAG + captchaKey, String.class);
        // 不需要验证码
        if (StringUtil.isNull(captchaFlag)) {
            return "";
        }
        // 生成验证码
        String captchaValue = captchaUtil.getCaptchaValue();
        BufferedImage image = captchaUtil.generateCaptchaImage(captchaValue);
        try {
            String base64Image = captchaUtil.convertToWebBase64(image, "png");
            RedisUtil.put(PlatformCacheKey.USER_CAPTCHA_CODE + captchaKey, captchaValue, passwordErrorLockTime * 60);
            return base64Image;
        } catch (Exception e) {
            logger.error("设备[{}]字符串[{}]生成验证码图片失败.", CurrentUser.getDeviceId(), captchaValue, e);
            return "";
        }
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
}
