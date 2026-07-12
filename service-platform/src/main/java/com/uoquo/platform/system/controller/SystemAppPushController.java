package com.uoquo.platform.system.controller;

import com.uoquo.platform.system.model.dto.AppPushDto;
import com.uoquo.platform.system.model.param.AppPushParam;
import com.uoquo.platform.system.model.param.AppPushStateParam;
import com.uoquo.platform.system.service.AppPushService;
import com.uoquo.utils.StringUtil;
import com.uoquo.utils.json.JsonUtil;
import com.uoquo.web.ReturnData;
import com.uoquo.web.exception.ParamEmtpyException;
import com.uoquo.web.param.IdParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "system", description = "接入授权推送")
@Validated
@RestController
@RequestMapping("/v1/system/apppush")
public class SystemAppPushController {

    final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private AppPushService appPushService;

    @Operation(summary = "新增推送配置", operationId = "addAppPush", method = "POST")
    @PostMapping("/add")
    public ReturnData<AppPushDto> addAppPush(@RequestBody @Valid AppPushParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("addAppPush param: {}", JsonUtil.serialize(param));
        }
        AppPushDto dto = appPushService.addAppPush(param);
        return new ReturnData<>(dto);
    }

    @Operation(summary = "修改推送配置", operationId = "updateAppPush", method = "POST")
    @PostMapping("/update")
    public ReturnData<String> updateAppPush(@RequestBody @Valid AppPushParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("updateAppPush param: {}", JsonUtil.serialize(param));
        }
        if (StringUtil.isNull(param.getId())) {
            throw new ParamEmtpyException("id不能为空");
        }
        appPushService.updateAppPush(param);
        return new ReturnData<>();
    }

    @Operation(summary = "修改推送配置状态", operationId = "updateAppPushState", method = "POST")
    @PostMapping("/update/status")
    public ReturnData<String> updateAppPushState(@RequestBody @Valid AppPushStateParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("updateAppPushState param: {}", JsonUtil.serialize(param));
        }
        appPushService.updateState(param);
        return new ReturnData<>();
    }

    @Operation(summary = "删除推送配置", operationId = "deleteAppPush", method = "POST",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "推送配置ID", required = true)
    )
    @PostMapping("/delete")
    public ReturnData<String> deleteAppPush(@RequestBody @Valid IdParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("deleteAppPush param: {}", JsonUtil.serialize(param));
        }
        appPushService.deleteByPrimaryKey(param.getId());
        return new ReturnData<>();
    }

    @Operation(summary = "推送配置详情", operationId = "getAppPush", method = "POST",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "推送配置ID", required = true)
    )
    @PostMapping("/info")
    public ReturnData<AppPushDto> getAppPush(@RequestBody @Valid IdParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("getAppPush param: {}", JsonUtil.serialize(param));
        }
        AppPushDto result = appPushService.selectByPrimaryKey(param.getId());
        return new ReturnData<>(result);
    }

    @Operation(summary = "推送配置列表（按appId）", operationId = "listAppPushByAppId", method = "POST",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "接入信息ID", required = true)
    )
    @PostMapping("/list")
    public ReturnData<List<AppPushDto>> listAppPushByAppId(@RequestBody @Valid IdParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("listAppPushByAppId param: {}", JsonUtil.serialize(param));
        }
        List<AppPushDto> result = appPushService.listByAppId(param.getId());
        return new ReturnData<>(result);
    }
}
