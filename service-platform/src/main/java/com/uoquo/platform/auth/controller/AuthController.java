package com.uoquo.platform.auth.controller;

import com.uoquo.platform.auth.model.dto.TokenDto;
import com.uoquo.platform.auth.model.dto.UserAuthDto;
import com.uoquo.platform.auth.model.param.AccountLoginParam;
import com.uoquo.platform.auth.model.param.BasicLoginParam;
import com.uoquo.platform.auth.model.param.TokenLoginParam;
import com.uoquo.platform.auth.model.param.MfaLoginParam;
import com.uoquo.platform.auth.service.AuthService;
import com.uoquo.platform.role.model.dto.ModuleTreeDto;
import com.uoquo.utils.CurrentUser;
import com.uoquo.utils.StringUtil;
import com.uoquo.utils.json.JsonUtil;
import com.uoquo.web.ReturnData;
import com.uoquo.web.SystemReturnCode;
import com.uoquo.annotation.web.IgnoreAuth;
import com.uoquo.web.param.IdParam;
import com.uoquo.web.utils.WebUtil;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;

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
    @Operation(summary = "获取验证码图片", operationId = "getCaptcha", method = "POST")
    @PostMapping("/captcha")
    public ReturnData<String> getCaptcha(HttpServletRequest request, @RequestBody BasicLoginParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("getCaptcha param: {}", JsonUtil.serialize(param));
        }
        if (StringUtil.isNull(param.getUserAgent())) {
            param.setUserAgent(request.getHeader("User-Agent"));
        }
        String clientIp = WebUtil.getClientIp(request);
        String captcha = authService.getCaptcha(param, clientIp);
        return new ReturnData<>(captcha);
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
}
