package com.uoquo.platform.user.service.impl;

import com.uoquo.cloud.events.RemoteEvent;
import com.uoquo.platform.auth.model.dto.TotpDto;
import com.uoquo.platform.common.*;
import com.uoquo.platform.common.exception.AccountReturnCode;
import com.uoquo.platform.common.utils.TotpAuthUtils;
import com.uoquo.platform.common.utils.UserUtils;
import com.uoquo.platform.dfs.model.dto.UploadFileDto;
import com.uoquo.platform.dfs.model.param.UploadFileParam;
import com.uoquo.platform.dfs.service.FileUploadService;
import com.uoquo.platform.institute.mapper.InstituteInfoMapper;
import com.uoquo.platform.institute.model.pojo.InstituteInfo;
import com.uoquo.platform.role.mapper.RoleInfoMapper;
import com.uoquo.platform.role.model.pojo.RoleInfo;
import com.uoquo.platform.system.model.param.SettingSaveParam;
import com.uoquo.platform.user.mapper.GroupInfoMapper;
import com.uoquo.platform.user.mapper.UserGroupMapper;
import com.uoquo.platform.user.mapper.UserInfoMapper;
import com.uoquo.platform.user.mapper.UserRoleMapper;
import com.uoquo.platform.user.model.dto.GroupDto;
import com.uoquo.platform.user.model.dto.UserInfoDto;
import com.uoquo.platform.user.model.dto.UserRoleDto;
import com.uoquo.platform.user.model.param.*;
import com.uoquo.platform.user.model.pojo.GroupInfo;
import com.uoquo.platform.user.model.pojo.UserGroup;
import com.uoquo.platform.user.model.pojo.UserInfo;
import com.uoquo.platform.user.model.pojo.UserRole;
import com.uoquo.platform.user.service.UserInfoService;
import com.uoquo.platform.user.service.UserSettingService;
import com.uoquo.utils.CurrentUser;
import com.uoquo.utils.IDGenerator;
import com.uoquo.utils.PinYinUtil;
import com.uoquo.utils.StringUtil;
import com.uoquo.utils.crypto.Base32;
import com.uoquo.platform.common.BaseConstant;
import com.uoquo.web.BaseReturnCode;
import com.uoquo.web.SystemReturnCode;
import com.uoquo.web.events.UoquoEventPublisher;
import com.uoquo.web.exception.ParamEmtpyException;
import com.uoquo.web.exception.ResourceNotFoundException;
import com.uoquo.web.exception.UoquoException;
import com.uoquo.utils.spring.RedisUtil;
import com.uoquo.web.mybatis.page.PageHelper;
import com.uoquo.mybatis.page.PageList;
import com.uoquo.mybatis.page.PageResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserInfoServiceImpl implements UserInfoService {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Autowired
    private UserGroupMapper userGroupMapper;

    @Resource
    private RoleInfoMapper roleInfoMapper;

    @Autowired
    private GroupInfoMapper groupInfoMapper;

    @Autowired
    private InstituteInfoMapper instituteInfoMapper;

    @Autowired
    private FileUploadService fileUploadService;

    @Autowired
    private UserSettingService userSettingService;

    @Autowired
    private UoquoEventPublisher eventPublisher;

    @Value("${app.mfa.issuer:UOQUO}")
    private String issuer;

    /**
     * 静态资源前缀
     */
    @Value("${app.host.static:/}")
    private String staticHost;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addUserInfo(UserAddParam param) {
        // 1. 基础校验
        if (StringUtil.isNull(param.getInstituteId())) {
            throw new ParamEmtpyException("必须指定用户所在机构");
        }
        // 1.2 唯一校验
        // 全局唯一
        checkPhoneRepeat(null, param.getPhone());
        checkUserNameRepeat(null, param.getUserName());
        // 机构唯一
        checkUserCodeRepeat(null, param.getInstituteId(), param.getUserCode());
        checkThirdIdRepeat(null, param.getInstituteId(), param.getThirdId());
        // 2. 保存用户信息
        CurrentUser.UserInfo loginUser = CurrentUser.getInfo();
        UserInfo user = new UserInfo();
        BeanUtils.copyProperties(param, user);
        user.setId(IDGenerator.getNextULID());
        // 推荐码
        String referralCode = this.generateReferralCode(user, 20);
        user.setReferralCode(referralCode);
        // 姓名转拼音
        if (StringUtil.notNull(user.getRealName())) {
            user.setPinYin(PinYinUtil.getPinYin4FirstChar(user.getRealName()));
        }
        // 密码
        String password = param.getPassword();
        user.setPassword(UserUtils.hashPassword(password));
        // 状态
        user.setStatus(DictionaryCodeEnum.STATE_NORMAL.getCode());
        user.setStatusTime(new Date());
        // 其他信息
        user.setPwdExpired(false);
        user.setPwdEditTime(null);
        user.setLoginErrorCount(0);
        user.setCreateUser(loginUser.getUserId());
        user.setCreateTime(new Date());
        user.setUpdateUser(loginUser.getUserId());
        user.setUpdateTime(new Date());
        user.setDeleteState(BaseConstant.NOT_DELETED);
        userInfoMapper.insert(user);
        // 3. 发布事件（新增用户）
        this.publishEvent(BusinessOperationEnum.CREATE, SystemReturnCode.SUCCESS, null, user, null);
        // 4. 保存关联关系
        // 4.1 保存角色授权
        bindUserRole(user, param.getUserRoleIdList());
        // 4.2 保存用户组
        bindUserGroup(user, param.getUserGroupIdList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserInfo(UserUpdateParam param) {
        // 1. 基础校验
        // 1.1 用户存在校验
        UserInfo old = userInfoMapper.selectByPrimaryKey(param.getId());
        if (old == null) {
            throw new ResourceNotFoundException("信息不存在");
        }
        // 1.2 唯一校验
        // 全局唯一
        if (StringUtil.notNull(param.getPhone())) {
            checkPhoneRepeat(param.getId(), param.getPhone());
        }
        if (StringUtil.notNull(param.getUserName())) {
            checkUserNameRepeat(param.getId(), param.getUserName());
        }
        // 机构唯一
        checkUserCodeRepeat(param.getId(), old.getInstituteId(), param.getUserCode());
        checkThirdIdRepeat(param.getId(), old.getInstituteId(), param.getThirdId());
        // 2. 修改用户信息
        CurrentUser.UserInfo currentUser = CurrentUser.getInfo();
        UserInfo user = new UserInfo();
        BeanUtils.copyProperties(param, user);
        // 姓名转拼音（含有“*”则不更新）
        if (StringUtil.isNull(user.getRealName())) {
            // 更新为空字符串
            user.setRealName("");
            user.setPinYin("");
        } else if (user.getRealName().contains("*") || user.getRealName().equals(old.getRealName())) {
            // 不更新
            user.setRealName(null);
            user.setPinYin(null);
        } else {
            user.setPinYin(PinYinUtil.getPinYin4FirstChar(user.getRealName()));
        }
        // 密码
        if (StringUtil.notNull(param.getPassword())) {
            String password = param.getPassword();
            user.setPassword(UserUtils.hashPassword(password));
        }
        // 手机号含有“*”则不更新
        if (StringUtil.isNull(param.getPhone()) || param.getPhone().contains("*")) {
            user.setPhone(null);
        }
        // 邮箱含有“*”则不更新
        if (StringUtil.isNull(param.getEmail()) || param.getEmail().contains("*")) {
            user.setEmail(null);
        }
        // 头像不为空时，需保存
        if (StringUtil.notNull(param.getAvatar())) {
            String avatarPath = this.saveAvatar2File(old.getId(), old.getAvatar(), param.getAvatar());
            user.setAvatar(avatarPath);
        }
        // 更新基本信息时，不改状态
        user.setStatus(null);
        user.setUpdateUser(currentUser.getUserId());
        user.setUpdateTime(new Date());
        userInfoMapper.updateByPrimaryKey(user);
        // 3. 发布事件（修改用户）
        UserInfo newUser = userInfoMapper.selectByPrimaryKey(param.getId());
        if (StringUtil.notNull(user.getPhone())) {
            this.publishEvent(BusinessOperationEnum.UPDATE_PHONE, SystemReturnCode.SUCCESS, old, newUser, null);
        }
        if (StringUtil.notNull(user.getEmail())) {
            this.publishEvent(BusinessOperationEnum.UPDATE_EMAIL, SystemReturnCode.SUCCESS, old, newUser, null);
        }
        if (StringUtil.notNull(user.getPassword())) {
            this.publishEvent(BusinessOperationEnum.UPDATE_PASSWORD, SystemReturnCode.SUCCESS, old, newUser, null);
        }
        if (StringUtil.notNull(user.getAvatar())) {
            fileUploadService.deleteFileByPath(Collections.singletonList(old.getAvatar()));
            this.publishEvent(BusinessOperationEnum.UPDATE_AVATAR, SystemReturnCode.SUCCESS, old, newUser, null);
        }
        this.publishEvent(BusinessOperationEnum.UPDATE, SystemReturnCode.SUCCESS, old, newUser, null);
        // 4. 修改关联关系
        // 4.1 修改角色授权
        bindUserRole(newUser, param.getUserRoleIdList());
        // 4.2 修改用户组
        bindUserGroup(newUser, param.getUserGroupIdList());
    }

    @Override
    public void updateUserPassword(ChangePasswordParam param, boolean validateOldPassword) {
        // 1. 基础校验
        UserInfo old = userInfoMapper.selectByPrimaryKey(param.getId());
        if (old == null) {
            throw new ResourceNotFoundException("用户信息不存在");
        }
        // 旧密码校验
        String oldPassword = param.getOldPassword();
        if (validateOldPassword && !UserUtils.checkPassword(oldPassword, old.getPassword())) {
            throw new UoquoException(AccountReturnCode.OLD_PASSWORD_ERROR);
        }
        // 2. 修改密码
        CurrentUser.UserInfo currentUser = CurrentUser.getInfo();
        UserInfo user = new UserInfo();
        user.setId(param.getId());
        // 新密码
        String newPassword = param.getNewPassword();
        user.setPassword(UserUtils.hashPassword(newPassword));
        user.setPwdLevel(param.getNewPwdLevel());
        user.setPwdExpired(false);
        user.setPwdEditTime(new Date());
        user.setUpdateUser(currentUser.getUserId());
        user.setUpdateTime(new Date());
        userInfoMapper.updateByPrimaryKey(user);
        // 3. 发布事件（修改密码）
        user = userInfoMapper.selectByPrimaryKey(param.getId());
        if (validateOldPassword) {
            // 校验旧密码的场景：用户主动发起的修改
            this.publishEvent(BusinessOperationEnum.UPDATE_PASSWORD, SystemReturnCode.SUCCESS, old, user, null);
        } else {
            // 不校验旧密码：管理员发起的重置密码
            this.publishEvent(BusinessOperationEnum.RETRIEVE_PASSWORD, SystemReturnCode.SUCCESS, old, user, null);
        }
    }

    @Override
    public void updateState(UserStateParam param) {
        // 1. 基础校验
        UserInfo old = userInfoMapper.selectByPrimaryKey(param.getId());
        if (old == null) {
            throw new ResourceNotFoundException("用户信息不存在");
        }
        // 2. 修改状态
        CurrentUser.UserInfo currentUser = CurrentUser.getInfo();
        UserInfo user = new UserInfo();
        user.setId(param.getId());
        user.setStatus(param.getStatus());
        user.setStatusTime(new Date());
        user.setStatusMemo(param.getStatusMemo());
        user.setUpdateUser(currentUser.getUserId());
        user.setUpdateTime(new Date());
        // 如果是启用，则将 loginErrorCount 置 0
        if (DictionaryCodeEnum.STATE_NORMAL.getCode().equals(param.getStatus())) {
            user.setLoginErrorCount(0);
        }
        userInfoMapper.updateByPrimaryKey(user);
        // 3. 发布事件（修改状态）
        user = userInfoMapper.selectByPrimaryKey(param.getId());
        if (DictionaryCodeEnum.STATE_NORMAL.getCode().equals(param.getStatus())) {
            this.publishEvent(BusinessOperationEnum.ENABLE, SystemReturnCode.SUCCESS, old, user, null);
        } else if (DictionaryCodeEnum.STATE_DISABLE.getCode().equals(param.getStatus())) {
            this.publishEvent(BusinessOperationEnum.DISABLE, SystemReturnCode.SUCCESS, old, user, null);
        } else {
            this.publishEvent(BusinessOperationEnum.CHANGE_STATUS, SystemReturnCode.SUCCESS, old, user, null);
        }
    }

    @Override
    public void updateUserAvatar(String userId, String avatar) {
        if (StringUtil.isNull(avatar)) {
            throw new ParamEmtpyException("头像内容为空");
        }
        // 1.1 用户存在校验
        UserInfo old = userInfoMapper.selectByPrimaryKey(userId);
        if (old == null) {
            throw new ResourceNotFoundException("信息不存在");
        }
        // 保存头像
        String avatarPath = this.saveAvatar2File(old.getId(), old.getAvatar(), avatar);
        if (avatarPath == null) {
            throw new ParamEmtpyException("头像信息保存失败");
        }
        // 更新库
        CurrentUser.UserInfo currentUser = CurrentUser.getInfo();
        UserInfo user = new UserInfo();
        user.setId(old.getId());
        user.setAvatar(avatarPath);
        user.setUpdateUser(currentUser.getUserId());
        user.setUpdateTime(new Date());
        userInfoMapper.updateByPrimaryKey(user);
        // 删除旧文件
        fileUploadService.deleteFileByPath(Collections.singletonList(old.getAvatar()));
        // 3. 发布事件（修改头像）
        user = userInfoMapper.selectByPrimaryKey(userId);
        this.publishEvent(BusinessOperationEnum.UPDATE_AVATAR, SystemReturnCode.SUCCESS, old, user, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(String id) {
        // 1. 基础校验
        UserInfo info = userInfoMapper.selectByPrimaryKey(id);
        if (info == null) {
            throw new ResourceNotFoundException("用户信息不存在");
        }
        // 2. 删除用户（仅逻辑删除，防止有其他关联数据查询报错）
        userInfoMapper.deleteByPrimaryKey(id, System.currentTimeMillis());
        // 3. 删除授权（防止用户绕过登录）
        userRoleMapper.deleteByUserId(id);
        userGroupMapper.deleteByUserId(id);
        // 4. 发布事件（删除用户）
        this.publishEvent(BusinessOperationEnum.DELETE, SystemReturnCode.SUCCESS, info, null, null);
    }

    @Override
    public UserInfoDto getUserInfo(String userId) {
        UserInfo info = userInfoMapper.selectByPrimaryKey(userId);
        if (info == null) {
            throw new ResourceNotFoundException("用户信息不存在");
        }
        return convertInfo2Dto(info);
    }

    @Override
    public PageResult<UserInfoDto> listUserInfo(UserListParam param) {
        // 分页查询
        Map<String, Object> paramMap = new HashMap<>();
        if (StringUtil.notNull(param.getUserName())) {
            paramMap.put("userName", param.getUserName());
        }
        if (StringUtil.notNull(param.getInstituteId())) {
            paramMap.put("instituteId", param.getInstituteId());
        }
        if (StringUtil.notNull(param.getInstituteParentId())) {
            paramMap.put("instituteParentId", param.getInstituteParentId());
        }
        if (param.getCreateTimeStart() != null) {
            paramMap.put("createTimeStart", param.getCreateTimeStart());
            paramMap.put("createTimeEnd", param.getCreateTimeEnd() == null ? new Date() : param.getCreateTimeEnd());
        }
        PageHelper.startPage(param.getPageNum(), param.getPageSize());
        PageList<UserInfoDto> userList = (PageList<UserInfoDto>) userInfoMapper.selectBySearch(paramMap);

        // 对象转换
        for (UserInfoDto item : userList.getResult()) {
            this.perfectDto(item);
        }
        // 封装返回数据
        return PageResult.of(userList);
    }

    @Override
    public PageResult<UserInfoDto> listUserByRange(UserListByRangeParam param) {
        Map<String, Object> paramMap = new HashMap<>();
        if (DictionaryCodeEnum.PUBLISH_RANGE_INSTITUTE.getCode().equals(param.getReceiverRange())) {
            if (param.getReceiverIds() == null || param.getReceiverIds().isEmpty()) {
                paramMap.put("instituteParentId", param.getReceiverInstituteId());
            } else {
                paramMap.put("instituteList", param.getReceiverIds());
            }
        } else if (DictionaryCodeEnum.PUBLISH_RANGE_ROLE.getCode().equals(param.getReceiverRange())) {
            if (param.getReceiverIds() == null || param.getReceiverIds().isEmpty()) {
                paramMap.put("instituteId", param.getReceiverInstituteId());
            } else {
                paramMap.put("roleList", param.getReceiverIds());
            }
        } else if (DictionaryCodeEnum.PUBLISH_RANGE_USER.getCode().equals(param.getReceiverRange())) {
            if (param.getReceiverIds() == null || param.getReceiverIds().isEmpty()) {
                paramMap.put("instituteId", param.getReceiverInstituteId());
            } else {
                paramMap.put("userList", param.getReceiverIds());
            }
        } else if (DictionaryCodeEnum.PUBLISH_RANGE_ALL.getCode().equals(param.getReceiverRange())) {
            // do nothing;
        }  else {
            return PageResult.empty();
        }
        PageHelper.startPage(param.getPageNum(), param.getPageSize());
        PageList<UserInfoDto>userList = (PageList<UserInfoDto>) userInfoMapper.selectBySimple(paramMap);
        return PageResult.of(userList);
    }

    @Override
    public List<UserRoleDto> listRoleInfoByUserId(String userId) {
        List<RoleInfo> roleList = roleInfoMapper.listByUserId(userId);
        return roleList.stream().map(item -> {
            UserRoleDto role = new UserRoleDto();
            role.setId(item.getId());
            role.setRoleName(item.getRoleName());
            BeanUtils.copyProperties(item, role);
            return role;
        }).collect(Collectors.toList());
    }

    @Override
    public List<GroupDto> listGroupByInstituteId(String instituteId) {
        List<GroupInfo> list = groupInfoMapper.listByInstituteId(instituteId);
        // 对象转换
        List<GroupDto> result = new ArrayList<>();
        for (GroupInfo info : list) {
            GroupDto dto = new GroupDto();
            BeanUtils.copyProperties(info, dto);
            result.add(dto);
        }
        return result;
    }

    @Override
    public List<GroupDto> listGroupByUserId(String userId) {
        List<GroupInfo> list = groupInfoMapper.listByUserId(userId);
        // 对象转换
        List<GroupDto> result = new ArrayList<>();
        for (GroupInfo info : list) {
            GroupDto dto = new GroupDto();
            BeanUtils.copyProperties(info, dto);
            result.add(dto);
        }
        return result;
    }

    @Override
    public TotpDto getTotpQrCode(String userId) {
        // 1. 获取用户信息
        UserInfo user = userInfoMapper.selectByPrimaryKey(userId);
        if (user == null) {
            throw new ResourceNotFoundException("用户不存在");
        }

        // 2. 生成TOTP密钥
        String secret = TotpAuthUtils.generateSecret();

        // 3. 生成otpauth URI
        String account = StringUtil.notNull(user.getUserName()) ? user.getUserName() : user.getPhone();
        String otpAuthUri = TotpAuthUtils.generateOtpAuthUri(secret, account, issuer);

        // 4. 临时存储密钥到Redis（5分钟，等待绑定确认）
        RedisUtil.put(PlatformCacheKey.TOTP_BIND_SECRET + user.getId(), secret, 300);

        // 5. 生成二维码（Base64）
        String qrCodeBase64 = null;
        try {
            qrCodeBase64 = TotpAuthUtils.generateQrcode(otpAuthUri);
        } catch (Exception e) {
            logger.warn("生成TOTP二维码失败，仅返回URI", e);
        }

        // 6. 组装返回结果
        TotpDto dto = new TotpDto();
        dto.setOtpAuthUri(otpAuthUri);
        dto.setQrCodeBase64(qrCodeBase64);
        return dto;
    }

    @Override
    public void bindTotp(String mfaCode, String userId) {
        // 1. 从Redis获取临时密钥
        String secret = RedisUtil.get(PlatformCacheKey.TOTP_BIND_SECRET + userId, String.class);
        if (StringUtil.isNull(secret)) {
            throw new UoquoException(AccountReturnCode.TOTP_QRCODE_EXPIRED, "绑定已过期，请重新获取二维码");
        }
        // 判断错误次数
        String errorKey = PlatformCacheKey.TOTP_VERIFY_ERROR + userId;
        Integer errors = RedisUtil.get(errorKey, Integer.class);
        if ((errors != null) && (errors >= 5)) {
            RedisUtil.remove(PlatformCacheKey.TOTP_BIND_SECRET + userId);
            throw new UoquoException(AccountReturnCode.TOTP_ATTEMPT_EXCEED, "动态码错误次数过多，请重新获取二维码");
        }

        // 2. 验证动态码
        boolean verified = TotpAuthUtils.verifyAuthCode(secret, mfaCode);
        if (!verified) {
            // 记录错误次数
            errors = (errors == null) ? 1 : errors + 1;
            RedisUtil.put(errorKey, errors, 600);
            throw new UoquoException(AccountReturnCode.TOTP_VALIDATION_ERROR, "动态码不正确");
        }

        // 3. 验证成功，更新数据库
        UserInfo user = new UserInfo();
        user.setId(userId);
        user.setTotpSecret(secret);
        userInfoMapper.updateByPrimaryKey(user);
        // 同时开启用户的双因子认证
        SettingSaveParam setting = new SettingSaveParam();
        setting.setConfigCode(SettingsCode.MFA_AUTH_ENABLED);
        setting.setConfigValue("true");
        userSettingService.saveSetting(userId, List.of(setting));

        // 4. 清理临时密钥
        RedisUtil.remove(PlatformCacheKey.TOTP_BIND_SECRET + userId);
        RedisUtil.remove(PlatformCacheKey.TOTP_VERIFY_ERROR + userId);
    }

    @Override
    public void updateRealName(String userId, String realName) {
        // 1. 基础校验
        UserInfo old = userInfoMapper.selectByPrimaryKey(userId);
        if (old == null) {
            throw new ResourceNotFoundException("用户信息不存在");
        }
        // 2. 更新真实姓名和拼音
        CurrentUser.UserInfo currentUser = CurrentUser.getInfo();
        UserInfo user = new UserInfo();
        user.setId(userId);
        user.setRealName(realName);
        user.setPinYin(PinYinUtil.getPinYin4FirstChar(realName));
        user.setUpdateUser(currentUser.getUserId());
        user.setUpdateTime(new Date());
        userInfoMapper.updateByPrimaryKey(user);
        // 3. 发布事件
        user = userInfoMapper.selectByPrimaryKey(userId);
        this.publishEvent(BusinessOperationEnum.UPDATE, SystemReturnCode.SUCCESS, old, user, null);
    }

    @Override
    public String sendPhoneCaptcha(String phone) {
        // 1. 校验发送频率限制
        // 1.1 同一手机号（60秒内只能发送一次）
        String limitKey = PlatformCacheKey.PHONE_CAPTCHA_LIMIT + phone;
        String limitValue = RedisUtil.get(limitKey, String.class);
        if (limitValue != null) {
            throw new UoquoException(AccountReturnCode.CAPTCHA_SEND_TOO_FREQUENT);
        }
        // 1.2 同一客户地址（60秒内只能发送一次）
        String limitIpKey = PlatformCacheKey.PHONE_CAPTCHA_LIMIT + CurrentUser.getClientIp();
        limitValue = RedisUtil.get(limitIpKey, String.class);
        if (limitValue != null) {
            throw new UoquoException(AccountReturnCode.CAPTCHA_SEND_TOO_FREQUENT);
        }
        // 2. 生成验证码（用phone做秘钥）
        String secret = Base32.encode(phone);
        String code = TotpAuthUtils.generateDynamicCode(secret);
        // 3. 设置发送频率限制（60秒）
        RedisUtil.put(limitKey, "1", 60);
        RedisUtil.put(limitIpKey, "1", 60);
        // 4. 发送验证码（TODO: 调用短信服务发送，先临时返回前端，便于调试）
        logger.info("发送手机验证码: phone={}, code={}", phone, code);
        return String.format("发送成功。本次验证码：%s", code);
    }

    @Override
    public void updatePhone(String userId, UpdatePhoneParam param) {
        // 1. 基础校验
        // 1.1 错误次数校验
        String errorKey = PlatformCacheKey.PHONE_CAPTCHA_ERROR + param.getPhone();
        Integer errors = RedisUtil.get(errorKey, Integer.class);
        if (errors != null && errors >= 5) {
            throw new UoquoException(AccountReturnCode.CAPTCHA_CODE_ATTEMPT_EXCEED);
        }
        // 1.2 账号校验
        UserInfo old = userInfoMapper.selectByPrimaryKey(userId);
        if (old == null) {
            throw new ResourceNotFoundException("用户信息不存在");
        }
        // 1.3 校验手机号是否已被使用
        checkPhoneRepeat(userId, param.getPhone());
        // 5. 验证验证码
        String secret = Base32.encode(param.getPhone());
        boolean verified = TotpAuthUtils.verifyDynamicCode(secret, param.getCaptcha());
        if (!verified) {
            errors = (errors == null) ? 1 : errors + 1;
            RedisUtil.put(errorKey, errors, 300);
            throw new UoquoException(AccountReturnCode.CAPTCHA_CODE_ERROR);
        }
        // 6. 更新手机号
        CurrentUser.UserInfo currentUser = CurrentUser.getInfo();
        UserInfo user = new UserInfo();
        user.setId(userId);
        user.setPhone(param.getPhone());
        user.setUpdateUser(currentUser.getUserId());
        user.setUpdateTime(new Date());
        userInfoMapper.updateByPrimaryKey(user);
        // 7. 清理验证码相关缓存
        RedisUtil.remove(errorKey);
        // 8. 发布事件
        user = userInfoMapper.selectByPrimaryKey(userId);
        this.publishEvent(BusinessOperationEnum.UPDATE_PHONE, SystemReturnCode.SUCCESS, old, user, null);
    }

    @Override
    public String sendEmailCaptcha(String email) {
        // 1. 校验发送频率限制
        // 1.1 同一邮箱（60秒内只能发送一次）
        String limitKey = PlatformCacheKey.EMAIL_CAPTCHA_LIMIT + email;
        String limitValue = RedisUtil.get(limitKey, String.class);
        if (limitValue != null) {
            throw new UoquoException(AccountReturnCode.CAPTCHA_SEND_TOO_FREQUENT);
        }
        // 1.2 同一客户地址（60秒内只能发送一次）
        String limitIpKey = PlatformCacheKey.EMAIL_CAPTCHA_LIMIT + CurrentUser.getClientIp();
        limitValue = RedisUtil.get(limitIpKey, String.class);
        if (limitValue != null) {
            throw new UoquoException(AccountReturnCode.CAPTCHA_SEND_TOO_FREQUENT);
        }
        // 2. 生成验证码
        String secret = Base32.encode(email);
        String code = TotpAuthUtils.generateDynamicCode(secret);
        // 3. 设置发送频率限制（60秒）
        RedisUtil.put(limitKey, "1", 60);
        RedisUtil.put(limitIpKey, "1", 60);
        // 4. 发送验证码（TODO: 调用邮件服务发送，先临时返回前端，便于调试）
        logger.info("发送邮箱验证码: email={}, code={}", email, code);
        return String.format("发送成功。本次验证码：%s", code);
    }

    @Override
    public void updateEmail(String userId, UpdateEmailParam param) {
        // 1. 基础校验
        // 1.1 错误次数校验
        String errorKey = PlatformCacheKey.EMAIL_CAPTCHA_ERROR + param.getEmail();
        Integer errors = RedisUtil.get(errorKey, Integer.class);
        if (errors != null && errors >= 5) {
            throw new UoquoException(AccountReturnCode.CAPTCHA_CODE_ATTEMPT_EXCEED);
        }
        // 1.2 账号校验
        UserInfo old = userInfoMapper.selectByPrimaryKey(userId);
        if (old == null) {
            throw new ResourceNotFoundException("用户信息不存在");
        }
        
        // 4. 验证验证码
        String secret = Base32.encode(param.getEmail());
        boolean verified = TotpAuthUtils.verifyDynamicCode(secret, param.getCaptcha());
        if (!verified) {
            errors = (errors == null) ? 1 : errors + 1;
            RedisUtil.put(errorKey, errors, 300);
            throw new UoquoException(AccountReturnCode.CAPTCHA_CODE_ERROR);
        }
        // 5. 更新邮箱
        CurrentUser.UserInfo currentUser = CurrentUser.getInfo();
        UserInfo user = new UserInfo();
        user.setId(userId);
        user.setEmail(param.getEmail());
        user.setUpdateUser(currentUser.getUserId());
        user.setUpdateTime(new Date());
        userInfoMapper.updateByPrimaryKey(user);
        // 6. 清理验证码相关缓存
        RedisUtil.remove(errorKey);
        // 7. 发布事件
        user = userInfoMapper.selectByPrimaryKey(userId);
        this.publishEvent(BusinessOperationEnum.UPDATE_EMAIL, SystemReturnCode.SUCCESS, old, user, null);
    }

    private UserInfoDto convertInfo2Dto(UserInfo info) {
        UserInfoDto dto = new UserInfoDto();
        BeanUtils.copyProperties(info, dto);
        // 补充机构信息
        InstituteInfo institute = instituteInfoMapper.selectByPrimaryKey(info.getInstituteId());
        if (institute != null) {
            dto.setInstituteName(institute.getInstituteName());
        }
        this.perfectDto(dto);
        return dto;
    }

    /**
     * 完善DTO信息
     */
    private void perfectDto(UserInfoDto dto) {
        // 获取用户角色信息
        List<UserRoleDto> roleDtos = listRoleInfoByUserId(dto.getId());
        dto.setUserRoleList(roleDtos);
        // 获取用户分组信息
        List<GroupDto> groupDtos = listGroupByUserId(dto.getId());
        dto.setUserGroupList(groupDtos);
        // 头像补充前缀
        if (StringUtil.notNull(dto.getAvatar())) {
            dto.setAvatar(staticHost + dto.getAvatar());
        }
    }


    /**
     * 根据用户ID生成推荐码
     */
    private String generateReferralCode(UserInfo user, int maxAttempts) {
        if (maxAttempts <= 0) {
            logger.error("生成推荐码失败，尝试次数过多");
            throw new UoquoException(AccountReturnCode.REFERRAL_CODE_ERROR);
        }
        String referralCode = UserUtils.generateReferralCode(user.getId(), user.getPhone());
        boolean flag = checkReferralCodeRepeat(referralCode);
        if (flag) {
            user.setId(IDGenerator.getNextULID());
            referralCode = generateReferralCode(user, maxAttempts - 1);
        }
        return referralCode;
    }

    /**
     * 用户角色绑定（先删后加）
     */
    private void bindUserRole(UserInfo user, List<String> userRoleList) {
        // 1. 查询已有角色
        List<UserRole> userRoles = userRoleMapper.selectByUserId(user.getId());
        List<String> roleIds = userRoles.stream().map(UserRole::getRoleId).toList();
        // 2 保存（先删后加）
        userRoleMapper.deleteByUserId(user.getId());
        List<UserRole> list = new ArrayList<>();
        for (String roleId : userRoleList) {
            UserRole userRole = new UserRole();
            userRole.setId(IDGenerator.getNextULID());
            userRole.setUserId(user.getId());
            userRole.setRoleId(roleId);
            list.add(userRole);
        }
        userRoleMapper.batchInsert(list);
        // 3. 发布事件
        // 3.1 删除的角色
        List<String> delRoleIds = roleIds.stream().filter(item -> !userRoleList.contains(item)).collect(Collectors.toList());
        if (!delRoleIds.isEmpty()) {
            Map<String, List<String>> map = new HashMap<>();
            map.put("delRole", delRoleIds);
            this.publishEvent(BusinessOperationEnum.DEL_RELATION, SystemReturnCode.SUCCESS, user, user, map);
        }
        // 3.2 新增的角色
        List<String> addRoleIds = userRoleList.stream().filter(item -> !roleIds.contains(item)).collect(Collectors.toList());
        if (!addRoleIds.isEmpty()) {
            Map<String, List<String>> map = new HashMap<>();
            map.put("addRole", addRoleIds);
            this.publishEvent(BusinessOperationEnum.ADD_RELATION, SystemReturnCode.SUCCESS, user, user, map);
        }
    }

    /**
     * 用户分组绑定（先删后加）
     */
    private void bindUserGroup(UserInfo user, List<String> userGroupList) {
        // 1. 查询已有分组
        List<UserGroup> userGroups = userGroupMapper.selectByUserId(user.getId());
        List<String> groupIds = userGroups.stream().map(UserGroup::getGroupId).toList();
        // 2 保存（先删后加）
        userGroupMapper.deleteByUserId(user.getId());
        List<UserGroup> list = new ArrayList<>();
        for (String groupId : userGroupList) {
            UserGroup userGroup = new UserGroup();
            userGroup.setId(IDGenerator.getNextULID());
            userGroup.setUserId(user.getId());
            userGroup.setGroupId(groupId);
            list.add(userGroup);
        }
        userGroupMapper.batchInsert(list);
        // 3. 发布事件
        // 3.1 删除的角色
        List<String> delGroupIds = groupIds.stream().filter(item -> !userGroupList.contains(item)).collect(Collectors.toList());
        if (!delGroupIds.isEmpty()) {
            Map<String, List<String>> map = new HashMap<>();
            map.put("delGroup", delGroupIds);
            this.publishEvent(BusinessOperationEnum.DEL_RELATION, SystemReturnCode.SUCCESS, user, user, map);
        }
        // 3.2 新增的角色
        List<String> addGroupIds = userGroupList.stream().filter(item -> !groupIds.contains(item)).collect(Collectors.toList());
        if (!addGroupIds.isEmpty()) {
            Map<String, List<String>> map = new HashMap<>();
            map.put("addGroup", addGroupIds);
            this.publishEvent(BusinessOperationEnum.ADD_RELATION, SystemReturnCode.SUCCESS, user, user, map);
        }
    }

    /**
     * 校验手机号是否重复：系统唯一
     */
    private void checkPhoneRepeat(String id, String phone) {
        UserInfo user = userInfoMapper.selectByPhone(id, phone);
        if (user != null) {
            throw new UoquoException(AccountReturnCode.PHONE_EXIST);
        }
    }

    /**
     * 校验用户名是否重复：系统唯一
     */
    private void checkUserNameRepeat(String id, String userName) {
        UserInfo user = userInfoMapper.selectByUserName(id, userName);
        if (user != null) {
            throw new UoquoException(AccountReturnCode.ACCOUNT_EXIST);
        }
    }

    /**
     * 校验推介码是否重复：系统唯一
     */
    private boolean checkReferralCodeRepeat(String referralCode) {
        if (StringUtil.isNull(referralCode)) {
            return true;
        }
        UserInfo user = userInfoMapper.selectByReferralCode(referralCode);
        return user != null;
    }

    /**
     * 校验工号是否重复：机构内唯一
     */
    private void checkUserCodeRepeat(String id, String instituteId, String userCode) {
        if (StringUtil.isNull(userCode)) {
            return;
        }
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("id", id);
        paramMap.put("instituteId", instituteId);
        paramMap.put("userCode", userCode);
        UserInfo user = userInfoMapper.checkByInstitute(paramMap);
        if (user != null) {
            throw new UoquoException(AccountReturnCode.ACCOUNT_EXIST, "机构内员工编号[%s]重复", userCode);
        }
    }

    /**
     * 校验三方ID是否重复：机构内唯一
     */
    private void checkThirdIdRepeat(String id, String instituteId, String thirdId) {
        if (StringUtil.isNull(thirdId)) {
            return;
        }
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("id", id);
        paramMap.put("instituteId", instituteId);
        paramMap.put("thirdId", thirdId);
        UserInfo user = userInfoMapper.checkByInstitute(paramMap);
        if (user != null) {
            throw new UoquoException(AccountReturnCode.ACCOUNT_EXIST, "机构内三方ID[%s]重复", thirdId);
        }
    }

    /**
     * 保存头像文件
     */
    private String saveAvatar2File(String userId, String oldPath, String newAvatar) {
        // TODO 头像文件有效性、大小等判断，保存新文件
        UploadFileParam param = new UploadFileParam();
        param.setFileContent(newAvatar);
        param.setFinalFile(true);
        try {
            UploadFileDto dto = fileUploadService.uploadFileByBase64(param);
            return dto.getFilePath();
        } catch (Exception e) {
            logger.warn("保存用户[{}]头像失败", userId, e);
            return null;
        }
    }

    /**
     * 发布用户信息事件
     */
    private void publishEvent(BusinessOperationEnum type, BaseReturnCode status, UserInfo oldInfo, UserInfo newInfo, Map<String, List<String>> map) {
        RemoteEvent<UserInfo> event = new RemoteEvent<>(BusinessTypeEnum.ACCOUNT.getCode(), type.getCode(), status.getCode());
        event.setBusinessSubType("USER");
        // 去除敏感信息
        if (newInfo != null) {
            newInfo.setPassword(null);
        }
        if (oldInfo != null) {
            oldInfo.setPassword(null);
        }
        event.setOldData(oldInfo);
        event.setNewData(newInfo);
        // 补充业务信息
        UserInfo info = (newInfo == null) ? oldInfo : newInfo;
        if (info != null) {
            event.setBusinessId(info.getId());
            event.setBusinessInstituteId(info.getInstituteId());
        }
        event.setRemarks(status.getText());
        event.setExtension(map);
        eventPublisher.publishEvent(event);
    }

}
