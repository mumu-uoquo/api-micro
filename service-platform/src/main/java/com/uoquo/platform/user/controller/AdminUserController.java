package com.uoquo.platform.user.controller;

import com.uoquo.platform.user.model.dto.UserInfoDto;
import com.uoquo.platform.user.model.param.*;
import com.uoquo.platform.user.service.UserInfoService;
import com.uoquo.utils.json.JsonUtil;
import com.uoquo.web.ReturnData;
import com.uoquo.annotation.web.IgnoreAuth;
import com.uoquo.web.param.IdParam;
import com.uoquo.mybatis.page.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 用户管理：超管，能操作所有机构
 */
@Tag(name = "adminUser", description = "超管用户管理")
@Validated
@RestController
@RequestMapping("/admin/v1/user")
public class AdminUserController {

    final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private UserInfoService userInfoService;

    @Operation(summary = "列表查询", operationId = "listUserInfoByPage", method = "POST")
    @PostMapping("/list/page")
    public ReturnData<PageResult<UserInfoDto>> listUserInfoByPage(@RequestBody UserListParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("listUserInfo param: {}", JsonUtil.serialize(param));
        }
        PageResult<UserInfoDto> result = userInfoService.listUserInfo(param);
        return new ReturnData<>(result);
    }

    @Operation(summary = "用户列表（简单检索）", operationId = "listUserByAbbr", method = "POST")
    @RequestMapping(value = "/list/abbr", method = RequestMethod.POST)
    public ReturnData<List<UserInfoDto>> listUserByAbbr(@RequestBody @Valid UserListParam param) {
        PageResult<UserInfoDto> result = userInfoService.listUserInfo(param);
        return new ReturnData<>(result.getResult());
    }

    @IgnoreAuth(inner = true)
    @Operation(summary = "根据范围查找用户", hidden = true)
    @PostMapping("/list/range")
    public ReturnData<PageResult<UserInfoDto>> listUserByRange(@RequestBody UserListByRangeParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("listUserByRange param: {}", JsonUtil.serialize(param));
        }
        PageResult<UserInfoDto> result = userInfoService.listUserByRange(param);
        return new ReturnData<>(result);
    }

    @Operation(summary = "用户详情查询", operationId = "getUserInfo", method = "POST",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "用户ID", required = true)
    )
    @PostMapping("/info")
    public ReturnData<UserInfoDto> getUserInfo(@RequestBody @Valid IdParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("getUserInfo param: {}", JsonUtil.serialize(param));
        }
        UserInfoDto userInfo =  userInfoService.getUserInfo(param.getId());
        return new ReturnData<>(userInfo);
    }

    @Operation(summary = "新增用户信息", operationId = "addUserInfo", method = "POST")
    @PostMapping("/add")
    public ReturnData<String> addUserInfo(@RequestBody @Valid UserAddParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("addUserInfo param: {}", JsonUtil.serialize(param));
        }
        userInfoService.addUserInfo(param);
        return new ReturnData<>();
    }

    @Operation(summary = "修改用户信息", operationId = "updateUserInfo", method = "POST")
    @PostMapping("/update")
    public ReturnData<String> updateUserInfo(@RequestBody @Valid UserUpdateParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("updateUserInfo param: {}", JsonUtil.serialize(param));
        }
        userInfoService.updateUserInfo(param);
        return new ReturnData<>();
    }

    @Operation(summary = "修改用户密码", operationId = "updateUserPassword", method = "POST")
    @PostMapping("/update/password")
    public ReturnData<String> updateUserPassword(@RequestBody @Valid ChangePasswordParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("updateUserPassword param: {}", JsonUtil.serialize(param));
        }
        userInfoService.updateUserPassword(param, false);
        return new ReturnData<>();
    }

    @Operation(summary = "删除用户信息", operationId = "deleteUserInfo", method = "POST",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "用户ID", required = true)
    )
    @PostMapping("/delete")
    public ReturnData<String> deleteUserInfo(@RequestBody @Valid IdParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("deleteUser param: {}", JsonUtil.serialize(param));
        }
        userInfoService.deleteUser(param.getId());
        return new ReturnData<>();
    }

    @Operation(summary = "更新状态", operationId = "updateUserState", method = "POST")
    @PostMapping("/update/status")
    public ReturnData<String> updateUserState(@RequestBody @Valid UserStateParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("updateState param: {}", JsonUtil.serialize(param));
        }
        // 变更状态
        userInfoService.updateState(param);
        return new ReturnData<>();
    }

}
