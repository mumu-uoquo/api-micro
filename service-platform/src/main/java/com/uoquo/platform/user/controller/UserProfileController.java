package com.uoquo.platform.user.controller;

import com.uoquo.platform.auth.model.dto.TotpDto;
import com.uoquo.platform.auth.model.param.TotpBindParam;
import com.uoquo.platform.user.model.dto.UserInfoDto;
import com.uoquo.platform.user.model.param.*;
import com.uoquo.platform.user.service.UserInfoService;
import com.uoquo.utils.CurrentUser;
import com.uoquo.utils.StringUtil;
import com.uoquo.utils.json.JsonUtil;
import com.uoquo.web.ReturnData;
import com.uoquo.web.exception.ParamEmtpyException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * 用户信息：维护自己的信息
 */
@Tag(name = "user", description = "用户信息")
@Validated
@RestController
@RequestMapping("/v1/user/profile")
public class UserProfileController {

    final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private UserInfoService userInfoService;

    @Operation(summary = "用户详情查询", operationId = "getUserProfileInfo", method = "POST")
    @PostMapping("/info")
    public ReturnData<UserInfoDto> getUserProfileInfo() {
        // 只能查自己的信息
        String userId = CurrentUser.getInfo().getUserId();
        UserInfoDto userInfo = userInfoService.getUserInfo(userId);
        return new ReturnData<>(userInfo);
    }

    @Operation(summary = "修改自己的密码", operationId = "updateSelfPassword", method = "POST")
    @PostMapping("/update/password")
    public ReturnData<String> updateSelfPassword(@RequestBody @Valid ChangePasswordParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("updateSelfPassword param: {}", JsonUtil.serialize(param));
        }
        if (StringUtil.isNull(param.getOldPassword())) {
            throw new ParamEmtpyException("请输入旧密码");
        }
        // 只能修改自己的密码
        String userId = CurrentUser.getInfo().getUserId();
        param.setId(userId);
        userInfoService.updateUserPassword(param, true);
        return new ReturnData<>();
    }

    @Operation(summary = "更新头像", operationId = "updateSelfAvatar", method = "POST")
    @PostMapping("/update/avatar")
    public ReturnData<String> updateSelfAvatar(@RequestBody @Valid UpdateAvatarParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("updateSelfAvatar param: {}", JsonUtil.serialize(param));
        }
        // 只能修改自己的头像
        String userId = CurrentUser.getInfo().getUserId();
        userInfoService.updateUserAvatar(userId, param.getAvatar());
        return new ReturnData<>();
    }

    @Operation(summary = "获取TOTP二维码", operationId = "getMfaQrCode", method = "POST")
    @PostMapping("/mfa/qrcode")
    public ReturnData<TotpDto> getMfaQrCode() {
        if (logger.isInfoEnabled()) {
            logger.info("getMfaQrCode: user[{}]", CurrentUser.getInfo().getUserId());
        }
        String userId = CurrentUser.getInfo().getUserId();
        TotpDto result = userInfoService.getTotpQrCode(userId);
        return new ReturnData<>(result);
    }

    @Operation(summary = "绑定TOTP", operationId = "bindMfa", method = "POST")
    @PostMapping("/mfa/bind")
    public ReturnData<String> bindMfa(@RequestBody @Valid TotpBindParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("bindMfa: user[{}]", CurrentUser.getInfo().getUserId());
        }
        // 只能绑定自己的秘钥
        String userId = CurrentUser.getInfo().getUserId();
        userInfoService.bindTotp(param.getTotpCode(), userId);
        return new ReturnData<>("绑定成功");
    }

    @Operation(summary = "更新真实姓名", operationId = "updateSelfRealName", method = "POST")
    @PostMapping("/update/realName")
    public ReturnData<String> updateSelfRealName(@RequestBody @Valid UpdateRealNameParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("updateSelfRealName param: {}", JsonUtil.serialize(param));
        }
        String userId = CurrentUser.getInfo().getUserId();
        userInfoService.updateRealName(userId, param.getRealName());
        return new ReturnData<>();
    }

    @Operation(summary = "发送手机验证码", operationId = "sendPhoneCaptcha", method = "POST")
    @PostMapping("/phone/captcha")
    public ReturnData<String> sendPhoneCaptcha(@RequestBody @Valid SendPhoneCodeParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("sendPhoneCaptcha: phone={}", param.getPhone().replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2"));
        }
        // 已登录场景：直接取 userId，无需查库
        String userId = CurrentUser.getInfo().getUserId();
        String result = userInfoService.sendPhoneCaptcha(param.getPhone(), userId);
        return new ReturnData<>(result);
    }

    @Operation(summary = "更换手机号", operationId = "updateSelfPhone", method = "POST")
    @PostMapping("/update/phone")
    public ReturnData<String> updateSelfPhone(@RequestBody @Valid UpdatePhoneParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("updateSelfPhone: phone={}", param.getPhone().replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2"));
        }
        String userId = CurrentUser.getInfo().getUserId();
        userInfoService.updatePhone(userId, param);
        return new ReturnData<>();
    }

    @Operation(summary = "发送邮箱验证码", operationId = "sendEmailCaptcha", method = "POST")
    @PostMapping("/email/captcha")
    public ReturnData<String> sendEmailCaptcha(@RequestBody @Valid SendEmailCodeParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("sendEmailCaptcha: email={}", param.getEmail().replaceAll("(.{2}).+(@.+)", "$1***$2"));
        }
        String result = userInfoService.sendEmailCaptcha(param.getEmail());
        return new ReturnData<>(result);
    }

    @Operation(summary = "更换邮箱", operationId = "updateSelfEmail", method = "POST")
    @PostMapping("/update/email")
    public ReturnData<String> updateSelfEmail(@RequestBody @Valid UpdateEmailParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("updateSelfEmail: email={}", param.getEmail().replaceAll("(.{2}).+(@.+)", "$1***$2"));
        }
        String userId = CurrentUser.getInfo().getUserId();
        userInfoService.updateEmail(userId, param);
        return new ReturnData<>();
    }

}
