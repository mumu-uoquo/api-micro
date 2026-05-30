package com.uoquo.platform.user.controller;

import com.uoquo.platform.system.model.dto.SettingDto;
import com.uoquo.platform.system.model.param.SettingCodeParam;
import com.uoquo.platform.system.model.param.SettingSearchParam;
import com.uoquo.platform.system.model.param.SettingSaveParam;
import com.uoquo.platform.user.service.UserSettingService;
import com.uoquo.utils.CurrentUser;
import com.uoquo.web.ReturnData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 用户配置控制器
 */
@Tag(name = "user", description = "用户配置")
@Validated
@RestController
@RequestMapping("/v1/user/settings")
public class UserSettingController {

    @Autowired
    private UserSettingService userSettingService;

    @Operation(summary = "新增或修改用户配置", operationId = "saveUserSetting", method = "POST")
    @PostMapping("/save")
    public ReturnData<Void> saveUserSetting(@RequestBody @Valid List<SettingSaveParam> param) {
        CurrentUser.UserInfo currentUser = CurrentUser.getInfo();
        userSettingService.saveSetting(currentUser.getUserId(), param);
        return new ReturnData<>();
    }

    @Operation(summary = "删除用户配置", operationId = "deleteUserSetting", method = "POST")
    @PostMapping("/delete")
    public ReturnData<Void> deleteUserSetting(@RequestBody @Valid SettingCodeParam param) {
        CurrentUser.UserInfo currentUser = CurrentUser.getInfo();
        userSettingService.deleteByCode(currentUser.getUserId(), param.getConfigCode());
        return new ReturnData<>();
    }

    @Operation(summary = "查询单个用户配置", operationId = "getUserSetting", method = "POST")
    @PostMapping("/code")
    public ReturnData<String> getUserSetting(@RequestBody @Valid SettingCodeParam param) {
        CurrentUser.UserInfo currentUser = CurrentUser.getInfo();
        String result = userSettingService.getValueByCode(currentUser.getUserId(), param.getConfigCode());
        return new ReturnData<>(result);
    }

    @Operation(summary = "查询用户配置列表", operationId = "listUserSettings", method = "POST")
    @PostMapping("/list")
    public ReturnData<List<SettingDto>> listUserSettings(@RequestBody @Valid SettingSearchParam param) {
        CurrentUser.UserInfo currentUser = CurrentUser.getInfo();
        List<SettingDto> result = userSettingService.listByPrefix(currentUser.getUserId(), param.getPrefix());
        return new ReturnData<>(result);
    }
}
