package com.uoquo.platform.system.controller;

import com.uoquo.platform.system.model.dto.SettingDto;
import com.uoquo.platform.system.model.param.SettingCodeParam;
import com.uoquo.platform.system.model.param.SettingSearchParam;
import com.uoquo.platform.system.model.param.SettingSaveParam;
import com.uoquo.platform.system.service.SysSettingService;
import com.uoquo.utils.json.JsonUtil;
import com.uoquo.web.ReturnData;
import com.uoquo.annotation.web.IgnoreAuth;
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

import jakarta.validation.Valid;
import java.util.List;

@Tag(name = "system", description = "系统设置")
@Validated
@RestController
@RequestMapping("/v1/system/settings")
public class SystemSettingController {

    final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private SysSettingService sysSettingService;

    // ============ 批量操作 ============

    @IgnoreAuth(all = true)
    @Operation(summary = "获取通用配置信息（无需认证）", operationId = "listPublicSettings", method = "POST")
    @PostMapping("/list/public")
    public ReturnData<List<SettingDto>> listPublicSettings() {
        return new ReturnData<>(sysSettingService.listPublicSettings());
    }

    @Operation(summary = "获取指定开头的配置信息", operationId = "listSettingsByCode", method = "POST")
    @PostMapping("/list/prefix")
    public ReturnData<List<SettingDto>> listSettingsByCode(@RequestBody @Valid SettingSearchParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("listSettingsByCode param: {}", JsonUtil.serialize(param));
        }
        List<SettingDto> result = sysSettingService.listByPrefix(param.getPrefix());
        return new ReturnData<>(result);
    }

    @Operation(summary = "修改配置信息", operationId = "saveSystemSetting", method = "POST")
    @PostMapping("/list/save")
    public ReturnData<String> saveSystemSetting(@RequestBody @Valid List<SettingSaveParam> list) {
        if (logger.isInfoEnabled()) {
            logger.info("saveSystemSetting param: {}", JsonUtil.serialize(list));
        }
        sysSettingService.saveSetting(list);
        return new ReturnData<>();
    }

    // ============ 单个操作 ============

    @Operation(summary = "删除系统配置", operationId = "deleteSystemSetting", method = "POST")
    @PostMapping("/delete")
    public ReturnData<Void> deleteSystemSetting(@RequestBody @Valid SettingCodeParam param) {
        sysSettingService.deleteByCode(param.getConfigCode());
        return new ReturnData<>();
    }

    @Operation(summary = "查询单个系统配置", operationId = "getSystemSetting", method = "POST")
    @PostMapping("/code")
    public ReturnData<String> getSystemSetting(@RequestBody @Valid SettingCodeParam param) {
        String result = sysSettingService.getValueByCode(param.getConfigCode());
        return new ReturnData<>(result);
    }
}
