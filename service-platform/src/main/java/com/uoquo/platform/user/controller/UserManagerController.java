package com.uoquo.platform.user.controller;

import com.uoquo.platform.institute.service.InstituteInfoService;
import com.uoquo.platform.user.model.dto.GroupDto;
import com.uoquo.platform.user.model.dto.UserInfoDto;
import com.uoquo.platform.user.model.param.*;
import com.uoquo.platform.user.service.UserInfoService;
import com.uoquo.utils.CurrentUser;
import com.uoquo.utils.StringUtil;
import com.uoquo.utils.json.JsonUtil;
import com.uoquo.web.ReturnData;
import com.uoquo.web.param.IdParam;
import com.uoquo.web.exception.ForbiddenException;
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
 * 用户管理：普通，只能操作自己的及下属所有的机构
 */
@Tag(name = "user", description = "用户管理")
@Validated
@RestController
@RequestMapping("/v1/user")
public class UserManagerController {

    final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    private InstituteInfoService instituteInfoService;

    /* *******************  用户管理 ******************* */
    @Operation(summary = "列表查询", operationId = "listUserInfoByPage", method = "POST")
    @PostMapping("/list/page")
    public ReturnData<PageResult<UserInfoDto>> listUserInfoByPage(@RequestBody UserListParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("listUserInfo param: {}", JsonUtil.serialize(param));
        }
        // 权限判断：只能管理自己管辖机构的用户
        if (StringUtil.notNull(param.getInstituteId())) {
            boolean flag = instituteInfoService.checkSelfManageInstitute(param.getInstituteId());
            if (!flag) {
                throw new ForbiddenException("只能管理自己管辖机构的用户");
            }
        } else {
            // 默认查询当前机构及子机构
            param.setInstituteParentId(CurrentUser.getInfo().getInstituteId());
        }
        PageResult<UserInfoDto> result = userInfoService.listUserInfo(param);
        return new ReturnData<>(result);
    }

    @Operation(summary = "用户列表（简单检索）", operationId = "listUserByAbbr", method = "POST")
    @RequestMapping(value = "/list/abbr", method = RequestMethod.POST)
    public ReturnData<List<UserInfoDto>> listUserByAbbr(@RequestBody @Valid UserListParam param) {
        // 权限判断：只能管理自己管辖机构的用户
        if (StringUtil.notNull(param.getInstituteId())) {
            boolean flag = instituteInfoService.checkSelfManageInstitute(param.getInstituteId());
            if (!flag) {
                throw new ForbiddenException("只能管理自己管辖机构的用户");
            }
        } else {
            // 默认查询当前机构及子机构
            param.setInstituteParentId(CurrentUser.getInfo().getInstituteId());
        }
        PageResult<UserInfoDto> result = userInfoService.listUserInfo(param);
        return new ReturnData<>(result.getResult());
    }

    @Operation(summary = "用户详情查询", operationId = "getUserInfo", method = "POST",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "用户ID", required = true)
    )
    @PostMapping("/info")
    public ReturnData<UserInfoDto> getUserInfo(@RequestBody @Valid IdParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("getUserInfo param: {}", JsonUtil.serialize(param));
        }
        UserInfoDto userInfo = userInfoService.getUserInfo(param.getId());
        // 权限判断：只能管理自己管辖机构的用户
        boolean flag = instituteInfoService.checkSelfManageInstitute(userInfo.getInstituteId());
        if (!flag) {
            throw new ForbiddenException("只能管理自己管辖机构的用户");
        }
        return new ReturnData<>(userInfo);
    }

    @Operation(summary = "新增用户信息", operationId = "addUserInfo", method = "POST")
    @PostMapping("/add")
    public ReturnData<String> addUserInfo(@RequestBody @Valid UserAddParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("addUserInfo param: {}", JsonUtil.serialize(param));
        }
        // 权限判断：只能管理自己管辖机构的用户
        if (StringUtil.notNull(param.getInstituteId())) {
            boolean flag = instituteInfoService.checkSelfManageInstitute(param.getInstituteId());
            if (!flag) {
                throw new ForbiddenException("只能添加自己管辖机构的用户");
            }
        } else {
            // 默认为当前用户所在机构
            param.setInstituteId(CurrentUser.getInfo().getInstituteId());
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
        // 权限判断：只能管理自己管辖机构的用户
        UserInfoDto userInfo = userInfoService.getUserInfo(param.getId());
        boolean flag = instituteInfoService.checkSelfManageInstitute(userInfo.getInstituteId());
        if (!flag) {
            throw new ForbiddenException("只能管理自己管辖机构的用户");
        }
        // 用户信息修改
        userInfoService.updateUserInfo(param);
        return new ReturnData<>();
    }

    @Operation(summary = "修改用户密码", operationId = "updateUserPassword", method = "POST")
    @PostMapping("/update/password")
    public ReturnData<String> updateUserPassword(@RequestBody @Valid ChangePasswordParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("updateUserPassword param: {}", JsonUtil.serialize(param));
        }
        // 权限判断：只能管理自己管辖机构的用户
        UserInfoDto userInfo = userInfoService.getUserInfo(param.getId());
        boolean flag = instituteInfoService.checkSelfManageInstitute(userInfo.getInstituteId());
        if (!flag) {
            throw new ForbiddenException("只能管理自己管辖机构的用户");
        }
        // 修改密码
        userInfoService.updateUserPassword(param, false);
        return new ReturnData<>();
    }

    @Operation(summary = "更新状态", operationId = "updateUserState", method = "POST")
    @PostMapping("/update/status")
    public ReturnData<String> updateUserState(@RequestBody @Valid UserStateParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("updateState param: {}", JsonUtil.serialize(param));
        }
        // 权限判断：只能管理自己管辖机构的用户
        UserInfoDto userInfo = userInfoService.getUserInfo(param.getId());
        boolean flag = instituteInfoService.checkSelfManageInstitute(userInfo.getInstituteId());
        if (!flag) {
            throw new ForbiddenException("只能管理自己管辖机构的用户");
        }
        // 变更状态
        userInfoService.updateState(param);
        return new ReturnData<>();
    }

    @Operation(summary = "删除用户信息", operationId = "deleteUserInfo", method = "POST",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "用户ID", required = true)
    )
    @PostMapping("/delete")
    public ReturnData<String> deleteUserInfo(@RequestBody @Valid IdParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("listAppInfo param: {}", JsonUtil.serialize(param));
        }
        // 权限判断：只能管理自己管辖机构的用户
        UserInfoDto userInfo = userInfoService.getUserInfo(param.getId());
        boolean flag = instituteInfoService.checkSelfManageInstitute(userInfo.getInstituteId());
        if (!flag) {
            throw new ForbiddenException("只能管理自己管辖机构的用户");
        }
        // 删除用户
        userInfoService.deleteUser(param.getId());
        return new ReturnData<>();
    }

    /* *******************  用户组管理（暂时未实现） ******************* */

    @Operation(summary = "获取分组列表", operationId = "listUserGroup", method = "POST",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "机构ID")
    )
    @PostMapping("/group/list")
    public ReturnData<List<GroupDto>> listUserGroup(@RequestBody IdParam param) {
        if (StringUtil.isNull(param.getId())) {
            param.setId(CurrentUser.getInfo().getInstituteId());
        }
        return new ReturnData<>(userInfoService.listGroupByInstituteId(param.getId()));
    }

}
