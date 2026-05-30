package com.uoquo.platform.system.controller;

import com.uoquo.platform.common.DictionaryCodeEnum;
import com.uoquo.platform.role.model.dto.ResourceInfoDto;
import com.uoquo.platform.role.model.param.ResourceInfoParam;
import com.uoquo.platform.role.service.ResourceInfoService;
import com.uoquo.platform.system.model.dto.AppInfoDto;
import com.uoquo.platform.system.model.param.*;
import com.uoquo.platform.system.model.pojo.AppInherit;
import com.uoquo.platform.system.model.pojo.AppPermission;
import com.uoquo.platform.system.service.AppInfoService;
import com.uoquo.platform.system.service.AppPermissionService;
import com.uoquo.utils.StringUtil;
import com.uoquo.utils.json.JsonUtil;
import com.uoquo.web.ReturnData;
import com.uoquo.web.param.IdParam;
import com.uoquo.web.exception.ParamEmtpyException;
import com.uoquo.mybatis.page.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import java.util.List;

@Tag(name = "system", description = "接入授权")
@Validated
@RestController
@RequestMapping("/v1/system/appinfo")
public class SystemAppInfoController {

    final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private AppInfoService appInfoService;

    @Autowired
    private AppPermissionService appPermissionService;

    @Autowired
    private ResourceInfoService resourceInfoService;

    @Operation(summary = "分页查询AppInfo信息", operationId = "listAppInfoByPage", method = "POST")
    @PostMapping("/list/page")
    public ReturnData<PageResult<AppInfoDto>> listAppInfoByPage(@RequestBody AppInfoListParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("listAppInfoByPage param: {}", JsonUtil.serialize(param));
        }
        PageResult<AppInfoDto> result = appInfoService.listByPage(param);
        return new ReturnData<>(result);
    }

    @Operation(summary = "查询AppInfo信息（简单检索）", operationId = "listAppInfoByAbbr", method = "POST")
    @PostMapping("/list/abbr")
    public ReturnData<List<AppInfoDto>> listAppInfoByAbbr(@RequestBody AppInfoListParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("listAppInfo param: {}", JsonUtil.serialize(param));
        }
        PageResult<AppInfoDto> result = appInfoService.listByPage(param);
        return new ReturnData<>(result.getResult());
    }

    @Operation(summary = "查询AppInfo信息（模板列表）", operationId = "listAppInfoByTemplate", method = "POST")
    @PostMapping("/list/template")
    public ReturnData<List<AppInfoDto>> listAppInfoByTemplate() {
        List<AppInfoDto> result = appInfoService.listByTemplate(null);
        return new ReturnData<>(result);
    }

    @Operation(summary = "新增AppInfo信息", operationId = "addAppInfo", method = "POST")
    @PostMapping("/add")
    public ReturnData<AppInfoDto> addAppInfo(@RequestBody @Valid AppInfoParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("addAppInfo param: {}", JsonUtil.serialize(param));
        }
        // 基础校验
        if (StringUtil.isNull(param.getAppkey())) {
            throw new ParamEmtpyException("appkey不能为空");
        }
        if (StringUtil.isNull(param.getSecret())) {
            throw new ParamEmtpyException("secret不能为空");
        }
        if (StringUtil.isNull(param.getTemplateType())) {
            param.setTemplateType(DictionaryCodeEnum.TEMPLATE_TYPE_NONE.getCode());
        }
        // 保存
        AppInfoDto dto = appInfoService.addAppInfo(param);
        return new ReturnData<>(dto);
    }

    @Operation(summary = "修改AppInfo信息", operationId = "updateAppInfo", method = "POST")
    @PostMapping("/update")
    public ReturnData<String> updateAppInfo(@RequestBody @Valid AppInfoParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("updateAppInfo param: {}", JsonUtil.serialize(param));
        }
        // 基础校验
        if (StringUtil.isNull(param.getId())) {
            throw new ParamEmtpyException("id不能为空");
        }
        // 更新
        appInfoService.updateAppInfo(param);
        return new ReturnData<>();
    }

    @Operation(summary = "修改AppInfo状态", operationId = "updateAppInfoState", method = "POST")
    @PostMapping("/update/status")
    public ReturnData<String> updateAppInfoState(@RequestBody @Valid AppInfoStateParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("updateAppInfoState param: {}", JsonUtil.serialize(param));
        }
        // 更新
        appInfoService.updateState(param);
        return new ReturnData<>();
    }

    @Operation(summary = "修改AppInfo模板类型", operationId = "updateAppInfoTemplateType", method = "POST")
    @PostMapping("/update/template")
    public ReturnData<String> updateAppInfoTemplateType(@RequestBody @Valid AppInfoParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("updateAppInfoTemplateType param: {}", JsonUtil.serialize(param));
        }
        // 基础校验
        if (StringUtil.isNull(param.getId())) {
            throw new ParamEmtpyException("id不能为空");
        }
        if (StringUtil.isNull(param.getTemplateType())) {
            throw new ParamEmtpyException("模板类型不能为空");
        }
        List<AppInfoDto> subList = appInfoService.listInheritBySub(param.getId());
        // 更新模板类型
        appInfoService.updateTemplateType(param.getId(), param.getTemplateType());
        // 刷新授权信息（异步）
        appInfoService.flushAppPermissionCache(param.getId());
        for (AppInfoDto dto : subList) {
            appInfoService.flushAppPermissionCache(dto.getId());
        }
        return new ReturnData<>();
    }

    @Operation(summary = "删除AppInfo信息", operationId = "deleteAppInfo", method = "POST",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "应用ID", required = true)
    )
    @PostMapping("/delete")
    public ReturnData<String> deleteAppInfo(@RequestBody @Valid IdParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("deleteAppInfo param: {}", JsonUtil.serialize(param));
        }
        List<AppInfoDto> subList = appInfoService.listInheritBySub(param.getId());
        // 执行删除
        appInfoService.deleteByPrimaryKey(param.getId());
        // 刷新授权信息（异步）
        appInfoService.flushAppPermissionCache(param.getId());
        for (AppInfoDto dto : subList) {
            appInfoService.flushAppPermissionCache(dto.getId());
        }
        return new ReturnData<>();
    }

    @Operation(summary = "AppInfo详情查询", operationId = "getAppInfo", method = "POST",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "应用ID", required = true)
    )
    @PostMapping("/info")
    public ReturnData<AppInfoDto> getAppInfo(@RequestBody @Valid IdParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("getAppInfo param: {}", JsonUtil.serialize(param));
        }
        AppInfoDto result = appInfoService.selectByPrimaryKey(param.getId());
        return new ReturnData<>(result);
    }

    @Operation(summary = "复制AppInfo的授权信息", operationId = "copyAppInfoPermission", method = "POST")
    @PostMapping("/permission/copy")
    public ReturnData<String> copyAppInfoPermission(@RequestBody @Valid AppPermissionCopyParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("copyAppInfoPermission param: {}", JsonUtil.serialize(param));
        }
        appPermissionService.copyAppPermission(param.getFromAppId(), param.getToAppId());
        return new ReturnData<>();
    }

    /* *******************  继承管理 ******************* */
    @Operation(summary = "关联模板：已关联的模板列表", operationId = "listRelateInherit", method = "POST",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "应用ID", required = true)
    )
    @PostMapping("/inherit/relate")
    public ReturnData<List<AppInfoDto>> listRelateInherit(@RequestBody @Valid IdParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("listRelateInherit param: {}", JsonUtil.serialize(param));
        }
        List<AppInfoDto> result = appInfoService.listInheritByApp(param.getId());
        return new ReturnData<>(result);
    }

    @Operation(summary = "关联模板：未关联的模板列表", operationId = "listNotRelateInherit", method = "POST",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "应用ID", required = true)
    )
    @PostMapping("/inherit/undelegated")
    public ReturnData<List<AppInfoDto>> listNotRelateInherit(@RequestBody @Valid IdParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("listNotRelateInherit param: {}", JsonUtil.serialize(param));
        }
        List<AppInfoDto> result = appInfoService.listByTemplate(param.getId());
        return new ReturnData<>(result);
    }

    @Operation(summary = "关联模板：添加关联模板", operationId = "addRelateInherit", method = "POST")
    @PostMapping("/inherit/add")
    public ReturnData<String> addRelateInherit(@RequestBody @Valid AppInhertAddParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("addRelateInherit param: {}", JsonUtil.serialize(param));
        }
        // 添加模板
        appPermissionService.batchInsertInherit(param);
        // 刷新授权信息（异步）
        appInfoService.flushAppPermissionCache(param.getAppId());
        return new ReturnData<>();
    }

    @Operation(summary = "关联模板：删除关联模板", operationId = "deleteRelateInherit", method = "POST",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "应用与模板的关联ID", required = true)
    )
    @PostMapping("/inherit/delete")
    public ReturnData<String> deleteRelateInherit(@RequestBody @Valid IdParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("deleteRelateInherit param: {}", JsonUtil.serialize(param));
        }
        // 删除继承的模板
        AppInherit old = appPermissionService.deleteInheritByPrimaryKey(param.getId());
        // 刷新授权信息（异步）
        if (old != null) {
            appInfoService.flushAppPermissionCache(old.getAppId());
        }
        return new ReturnData<>();
    }

    /* *******************  资源管理 ******************* */
    @Operation(summary = "资源信息：新增", operationId = "addResourceInfo4App", method = "POST")
    @PostMapping("/resource/add")
    @Transactional(rollbackFor = Exception.class)
    public ReturnData<String> addResourceInfo4App(@RequestBody @Valid ResourceInfoParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("addResourceInfo4App param: {}", JsonUtil.serialize(param));
        }
        // 保存资源信息
        String resourceId = resourceInfoService.addResource(param);
        // 关联应用资源
        if (StringUtil.notNull(param.getRelateId())) {
            appPermissionService.addAppResourcePermission(param.getRelateId(), resourceId);
            // 刷新授权信息（异步）
            flushAppPermissionCache(param.getRelateId());
        }
        return new ReturnData<>(resourceId);
    }

    @Operation(summary = "资源信息：修改", operationId = "updateResourceInfo4App", method = "POST")
    @PostMapping("/resource/update")
    public ReturnData<String> updateResourceInfo4App(@RequestBody @Valid ResourceInfoParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("updateModuleInfo4App param: {}", JsonUtil.serialize(param));
        }
        // 基本校验
        if (StringUtil.isNull(param.getId())) {
            throw new ParamEmtpyException("id必须传");
        }
        resourceInfoService.updateResource(param);
        return new ReturnData<>();
    }

    /* *******************  授权管理 ******************* */
    @Operation(summary = "关联资源：已关联的资源列表", operationId = "listAppRelationResource", method = "POST",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "应用ID", required = true)
    )
    @PostMapping("/resource/relate/list")
    public ReturnData<List<ResourceInfoDto>> listAppRelationResource(@RequestBody @Valid IdParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("listAppRelationResource param: {}", JsonUtil.serialize(param));
        }
        List<ResourceInfoDto> result = resourceInfoService.listByAppId(param.getId());
        return new ReturnData<>(result);
    }

    @Operation(summary = "关联资源：未关联的资源列表", operationId = "listAppNotRelationResource", method = "POST",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "应用ID", required = true)
    )
    @PostMapping("/resource/relate/undelegated")
    public ReturnData<List<ResourceInfoDto>> listAppNotRelationResource(@RequestBody @Valid IdParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("listAppNotRelationResource param: {}", JsonUtil.serialize(param));
        }
        List<ResourceInfoDto> result = resourceInfoService.listNotInApp(param.getId());
        return new ReturnData<>(result);
    }

    @Operation(summary = "关联资源：添加关联资源", operationId = "addAppRelationResource", method = "POST")
    @PostMapping("/resource/relate/add")
    public ReturnData<String> addAppRelationResource(@RequestBody @Valid AppPermissionAddParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("addAppRelationResource param: {}", JsonUtil.serialize(param));
        }
        // 添加资源
        appPermissionService.batchInsertPermission(param);
        // 刷新授权信息（异步）
        flushAppPermissionCache(param.getAppId());
        return new ReturnData<>();
    }

    @Operation(summary = "关联资源：删除关联资源", operationId = "deleteAppRelationResource", method = "POST",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "应用与资源的关联ID", required = true)
    )
    @PostMapping("/resource/relate/delete")
    public ReturnData<String> deleteAppRelationResource(@RequestBody @Valid IdParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("deleteAppRelationResource param: {}", JsonUtil.serialize(param));
        }
        // 删除资源
        AppPermission old = appPermissionService.deletePermissionByPrimaryKey(param.getId());
        // 刷新授权信息（异步）
        if (old != null) {
            flushAppPermissionCache(old.getAppId());
        }
        return new ReturnData<>();
    }

    /**
     * 刷新授权
     */
    private void flushAppPermissionCache(String appId) {
        // 刷新授权信息（异步）
        appInfoService.flushAppPermissionCache(appId);
        // 如果是模板，还需要刷新子应用的授权
        AppInfoDto info = appInfoService.selectByPrimaryKey(appId);
        if (DictionaryCodeEnum.TEMPLATE_TYPE_NORMAL.getCode().equals(info.getTemplateType())) {
            List<AppInfoDto> subList = appInfoService.listInheritBySub(info.getId());
            for (AppInfoDto dto : subList) {
                // 刷新授权信息（异步）
                appInfoService.flushAppPermissionCache(dto.getId());
            }
        }

    }
}
