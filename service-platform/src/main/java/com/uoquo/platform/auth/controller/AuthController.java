package com.uoquo.platform.auth.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uoquo.annotation.web.IgnoreAuth;
import com.uoquo.platform.auth.model.dto.TokenDto;
import com.uoquo.platform.auth.model.dto.UserAuthDto;
import com.uoquo.platform.auth.model.param.AccountLoginParam;
import com.uoquo.platform.auth.model.param.CaptchaParam;
import com.uoquo.platform.auth.model.param.CredentialBindParam;
import com.uoquo.platform.auth.model.param.CredentialLoginParam;
import com.uoquo.platform.auth.model.param.MfaLoginParam;
import com.uoquo.platform.auth.model.param.PhoneCaptchaParam;
import com.uoquo.platform.auth.model.param.RegisterParam;
import com.uoquo.platform.auth.model.param.ResetPasswordParam;
import com.uoquo.platform.auth.model.param.SmsLoginParam;
import com.uoquo.platform.auth.model.param.TokenLoginParam;
import com.uoquo.platform.auth.service.AuthService;
import com.uoquo.platform.role.model.dto.ModuleTreeDto;
import com.uoquo.utils.CurrentUser;
import com.uoquo.utils.StringUtil;
import com.uoquo.utils.json.JsonUtil;
import com.uoquo.web.ReturnData;
import com.uoquo.web.SystemReturnCode;
import com.uoquo.web.exception.ParamEmtpyException;
import com.uoquo.web.exception.ParamErrorException;
import com.uoquo.web.param.IdParam;
import com.uoquo.web.utils.WebUtil;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

/**
 * 后缀为“/login”的接口，将忽略登录校验
 * 注解IgnoreAuth仅应用层有效，网关层只能根据路径是否/login结尾来控制
 */
@Tag(name = "auth", description = "用户认证相关")
@Validated
@RestController
@RequestMapping("/v1/auth")
public class AuthController {

    final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private AuthService authService;

    /**
     * 登录时的密码传输安全保障
     * <ul>
     *     <li>方案1：将密码做MD5后，直接传输，被截获后容易重放</li>
     *     <li>方案2：将密码做MD5后，再采用TOTP时间因子进行AES加密后传输（推荐）</li>
     *     <li>方案3：将密码做MD5后，提交前从后台接口获取动态RSA公钥，采用RSA加密后再传输</li>
     * </ul>
     */
    @IgnoreAuth(login = true)
    @Operation(summary = "用户账号登录", operationId = "accountLogin", method = "POST")
    @PostMapping("/account/login")
    public ReturnData<UserAuthDto> login(HttpServletRequest request, @RequestBody @Valid AccountLoginParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("login param: {}", JsonUtil.serialize(param));
        }
        if (StringUtil.isNull(param.getUserAgent())) {
            param.setUserAgent(request.getHeader("User-Agent"));
        }
        String clientIp = WebUtil.getClientIp(request);
        UserAuthDto result = authService.userLogin(param, clientIp);
        return new ReturnData<>(result);
    }

    @IgnoreAuth(login = true)
    @Operation(summary = "验证MFA（登录二次验证）", operationId = "mfaLogin", method = "POST")
    @PostMapping("/mfa/login")
    public ReturnData<UserAuthDto> mfaLogin(HttpServletRequest request, @RequestBody @Valid MfaLoginParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("mfaLogin: tempToken[{}]", param.getTempToken());
        }
        if (StringUtil.isNull(param.getUserAgent())) {
            param.setUserAgent(request.getHeader("User-Agent"));
        }
        UserAuthDto result = authService.totpLogin(param.getTempToken(), param.getTotpCode());
        return new ReturnData<>(result);
    }

    @IgnoreAuth(login = true)
    @Operation(summary = "刷新token登录", operationId = "tokenLogin", method = "POST")
    @PostMapping("/token/login")
    public ReturnData<TokenDto> tokenLogin(HttpServletRequest request, @RequestBody @Valid TokenLoginParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("tokenLogin param: {}", JsonUtil.serialize(param));
        }
        if (StringUtil.isNull(param.getUserAgent())) {
            param.setUserAgent(request.getHeader("User-Agent"));
        }
        String clientIp = WebUtil.getClientIp(request);
        TokenDto result = authService.userRefreshLogin(param.getRefreshToken(), param.getCurrentRoleId(), clientIp);
        return new ReturnData<>(result);
    }

    @IgnoreAuth(login = true)
    @Operation(summary = "获取图形验证码", operationId = "getCaptcha", method = "POST",
            description = "scene 需与后续流程一致：login=密码出错触发, register=用户注册, sms_login=短信登录发码前人机验证（非 login 场景的验证码 key 会按 scene 隔离）")
    @PostMapping("/captcha")
    public ReturnData<String> getCaptcha(HttpServletRequest request, @RequestBody CaptchaParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("getCaptcha scene={}", param.getScene());
        }
        String clientIp = WebUtil.getClientIp(request);
        String captcha = authService.getCaptcha(param, clientIp);
        return new ReturnData<>(captcha);
    }

    @IgnoreAuth(login = true)
    @Operation(summary = "获取手机短信验证码", operationId = "sendSmsCaptcha", method = "POST", description = "必须携带图像验证码进行人机交互验证")
    @PostMapping("/phone/captcha")
    public ReturnData<String> sendSmsCaptcha(HttpServletRequest request, @RequestBody @Valid PhoneCaptchaParam param) {
        // 手机号经 RSA 解密后校验格式（解密在反序列化阶段完成，此处已是明文）
        this.validatePhone(param.getPhone());
        if (logger.isInfoEnabled()) {
            logger.info("sendPhoneCaptcha scene={} phone={}",
                    param.getScene(), param.getPhone().replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2"));
        }
        // 因为非登录状态也可发送短信码，所以必须做人机交互验证
        if (StringUtil.isNull(param.getCaptcha())) {
            throw new ParamEmtpyException("请输入图像验证码");
        }
        String clientIp = WebUtil.getClientIp(request);
        String result = authService.sendPhoneCaptcha(param, clientIp);
        return new ReturnData<>(result);
    }

    @IgnoreAuth(login = true)
    @Operation(summary = "手机号短信码登录", operationId = "smsLogin", method = "POST")
    @PostMapping("/phone/login")
    public ReturnData<UserAuthDto> smsLogin(HttpServletRequest request,
                                            @RequestBody @Valid SmsLoginParam param) {
        this.validatePhone(param.getPhone());
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

    @IgnoreAuth(login = true)
    @Operation(summary = "密码找回", operationId = "resetPassword", method = "POST")
    @PostMapping("/password/reset")
    public ReturnData<String> resetPassword(HttpServletRequest request,
                                            @RequestBody @Valid ResetPasswordParam param) {
        this.validatePhone(param.getPhone());
        if (logger.isInfoEnabled()) {
            logger.info("resetPassword: phone={}", param.getPhone().replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2"));
        }
        String clientIp = WebUtil.getClientIp(request);
        authService.resetPassword(param, clientIp);
        return new ReturnData<>();
    }

    @IgnoreAuth(login = true)
    @Operation(summary = "用户注册", operationId = "register", method = "POST")
    @PostMapping("/register")
    public ReturnData<String> register(HttpServletRequest request,
                                       @RequestBody @Valid RegisterParam param) {
        this.validatePhone(param.getPhone());
        if (logger.isInfoEnabled()) {
            logger.info("register: userName={} phone={}", param.getUserName(),
                    param.getPhone().replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2"));
        }
        String clientIp = WebUtil.getClientIp(request);
        authService.register(param, clientIp);
        return new ReturnData<>();
    }

    @Hidden
    @IgnoreAuth(login = true)
    @PostMapping("/app/login")
    public ReturnData<TokenDto> appLogin(HttpServletRequest request, @RequestBody @Valid AccountLoginParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("appLogin param: {}", JsonUtil.serialize(param));
        }
        if (StringUtil.isNull(param.getUserAgent())) {
            param.setUserAgent(request.getHeader("User-Agent"));
        }
        String clientIp = WebUtil.getClientIp(request);
        TokenDto result = authService.appLogin(param, clientIp);
        return new ReturnData<>(result);
    }

    @Hidden
    @IgnoreAuth(login = true)
    @PostMapping("/app/token/login")
    public ReturnData<TokenDto> appTokenLogin(HttpServletRequest request, @RequestBody @Valid TokenLoginParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("appTokenLogin param: {}", JsonUtil.serialize(param));
        }
        if (StringUtil.isNull(param.getUserAgent())) {
            param.setUserAgent(request.getHeader("User-Agent"));
        }
        String clientIp = WebUtil.getClientIp(request);
        TokenDto result = authService.appRefreshLogin(param.getRefreshToken(), clientIp);
        return new ReturnData<>(result);
    }

    @Operation(summary = "用户登出", operationId = "logout", method = "POST")
    @PostMapping("/logout")
    public ReturnData<String> logout() {
        if (logger.isInfoEnabled()) {
            logger.info("logout: {}", JsonUtil.serialize(CurrentUser.getInfo()));
        }
        authService.logout(CurrentUser.getToken(), CurrentUser.getAppkey(), SystemReturnCode.SUCCESS);
        return new ReturnData<>();
    }

    @Operation(summary = "当前用户信息", operationId = "getInfo", method = "POST")
    @PostMapping("/info")
    public ReturnData<UserAuthDto> getInfo() {
        UserAuthDto result = authService.getUserInfo();
        return new ReturnData<>(result);
    }

    @Operation(summary = "根据角色获取功能列表（注：仅用于切换角色）", operationId = "permission", method = "POST",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "角色ID", required = true)
    )
    @PostMapping("/account/permission")
    public ReturnData<List<ModuleTreeDto>> permission(@RequestBody @Valid IdParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("permission: user[{}] role[{}]", CurrentUser.getInfo().getUserId(), param.getId());
        }
        List<ModuleTreeDto> result = authService.getPermissionByRoleId(param.getId());
        return new ReturnData<>(result);
    }

    /**
     * 校验手机号格式。
     * 手机号字段采用 RSA 加密传输（@Sensitive(CRYPT_RSA)），解密在 JSON 反序列化阶段完成，
     * 进入控制器方法时已是明文，因此格式校验放在此处而非用 @Pattern 注解（避免对密文做正则校验）。
     */
    private void validatePhone(String phone) {
        if (StringUtil.isNull(phone) || !phone.matches("^1[3-9]\\d{9}$")) {
            throw new ParamErrorException("手机号格式不正确");
        }
    }
}
