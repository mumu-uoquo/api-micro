package com.uoquo.platform.role.controller;

import com.uoquo.platform.role.model.dto.ModuleInfoDto;
import com.uoquo.platform.role.model.dto.ModuleTreeDto;
import com.uoquo.platform.role.model.dto.ResourceInfoDto;
import com.uoquo.platform.role.model.param.ModuleInfoParam;
import com.uoquo.platform.role.model.param.ModuleResourceParam;
import com.uoquo.platform.role.model.param.ResourceInfoParam;
import com.uoquo.platform.role.service.ModuleInfoService;
import com.uoquo.platform.role.service.ResourceInfoService;
import com.uoquo.utils.CurrentUser;
import com.uoquo.utils.StringUtil;
import com.uoquo.utils.json.JsonUtil;
import com.uoquo.web.ReturnData;
import com.uoquo.web.param.IdParam;
import com.uoquo.web.exception.ParamEmtpyException;
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

@Tag(name = "module", description = "模块管理")
@Validated
@RestController
@RequestMapping("/v1/module")
public class ModuleController{
    final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ModuleInfoService moduleInfoService;

    @Autowired
    private ResourceInfoService resourceInfoService;

    /* *******************  模块管理 ******************* */
    @Operation(summary = "模块信息：新增", operationId = "addModuleInfo", method = "POST")
    @PostMapping("/add")
    public ReturnData<String> addModuleInfo(@RequestBody @Valid ModuleInfoParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("addModuleInfo param: {}", JsonUtil.serialize(param));
        }
        moduleInfoService.addModule(param);
        return new ReturnData<>();
    }

    @Operation(summary = "模块信息：修改", operationId = "updateModuleInfo", method = "POST")
    @PostMapping("/update")
    public ReturnData<String> updateModuleInfo(@RequestBody @Valid ModuleInfoParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("updateModuleInfo param: {}", JsonUtil.serialize(param));
        }
        // 基本校验
        if (StringUtil.isNull(param.getId())) {
            throw new ParamEmtpyException("id必须传");
        }
        moduleInfoService.updateModuleInfo(param);
        return new ReturnData<>();
    }

    @Operation(summary = "模块信息：删除", operationId = "deleteModuleInfo", method = "POST",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "模块ID", required = true)
    )
    @PostMapping("/delete")
    public ReturnData<String> deleteModuleInfo(@RequestBody @Valid IdParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("deleteModuleInfo param: {}", JsonUtil.serialize(param));
        }
        moduleInfoService.deleteModule(param.getId());
        return new ReturnData<>();
    }

    @Operation(summary = "模块查询：详情查询", operationId = "getModuleInfo", method = "POST",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "模块ID", required = true)
    )
    @PostMapping("/info")
    public ReturnData<ModuleInfoDto> getModuleInfo(@RequestBody @Valid IdParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("getModuleInfo param: {}", JsonUtil.serialize(param));
        }
        ModuleInfoDto dto = moduleInfoService.selectByPrimaryKey(param.getId());
        return new ReturnData<>(dto);
    }

    @Operation(summary = "模块查询：树查询", operationId = "listModuleInfoByTree", method = "POST")
    @PostMapping("/tree")
    public ReturnData<List<ModuleTreeDto>> listModuleInfoByTree() {
        // 获取当前角色的授权模块列表
        List<ModuleTreeDto> result = moduleInfoService.listModuleTreeByRoleId(CurrentUser.getInfo().getCurrentRoleId(), null);
        return new ReturnData<>(result);
    }
    @Operation(summary = "模块查询：根列表", operationId = "listModuleByRoot", method = "POST")
    @PostMapping("/list/root")
    public ReturnData<List<ModuleInfoDto>> listModuleByRoot() {
        // 获取当前角色的授权模块列表
        List<ModuleInfoDto> result = moduleInfoService.listModuleByRoot();
        return new ReturnData<>(result);
    }

    /* *******************  资源管理 ******************* */
    @Operation(summary = "资源信息：新增", operationId = "addResourceInfo", method = "POST")
    @PostMapping("/resource/add")
    @Transactional(rollbackFor = Exception.class)
    public ReturnData<String> addResourceInfo(@RequestBody @Valid ResourceInfoParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("addResourceInfo param: {}", JsonUtil.serialize(param));
        }
        // 保存资源信息
        String resourceId = resourceInfoService.addResource(param);
        // 关联模块
        if (StringUtil.notNull(param.getRelateId())) {
            moduleInfoService.addModuleResource(param.getRelateId(), resourceId);
            // 不自动更新关联角色的授权缓存，改为在系统页面手动更新（因为资源会持续添加修改，所以不实时刷关联的授权缓存）
        }
        return new ReturnData<>(resourceId);
    }

    @Operation(summary = "资源信息：修改", operationId = "updateResourceInfo", method = "POST")
    @PostMapping("/resource/update")
    public ReturnData<String> updateResourceInfo(@RequestBody @Valid ResourceInfoParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("updateResourceInfo param: {}", JsonUtil.serialize(param));
        }
        // 基本校验
        if (StringUtil.isNull(param.getId())) {
            throw new ParamEmtpyException("id必须传");
        }
        resourceInfoService.updateResource(param);
        return new ReturnData<>();
    }

    @Operation(summary = "资源信息：删除", operationId = "deleteResourceInfo", method = "POST",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "资源ID", required = true)
    )
    @PostMapping("/resource/delete")
    public ReturnData<String> deleteResourceInfo(@RequestBody @Valid IdParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("deleteResourceInfo param: {}", JsonUtil.serialize(param));
        }
        resourceInfoService.deleteResource(param.getId());
        return new ReturnData<>();
    }

    /* *******************  模块与资源的关联 ******************* */
    @Operation(summary = "关联资源：已关联的资源列表", operationId = "listModuleRelationResource", method = "POST",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "模块ID", required = true)
    )
    @PostMapping("/resource/relate/list")
    public ReturnData<List<ResourceInfoDto>> listModuleRelationResource(@RequestBody @Valid IdParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("listModuleRelationResource param: {}", JsonUtil.serialize(param));
        }
        List<ResourceInfoDto> result = resourceInfoService.listByModuleId(param.getId());
        return new ReturnData<>(result);
    }

    @Operation(summary = "关联资源：未关联的资源列表", operationId = "listModuleNotRelationResource", method = "POST",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "模块ID", required = true)
    )
    @PostMapping("/resource/relate/undelegated")
    public ReturnData<List<ResourceInfoDto>> listModuleNotRelationResource(@RequestBody @Valid IdParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("listModuleNotRelationResource param: {}", JsonUtil.serialize(param));
        }
        List<ResourceInfoDto> result = resourceInfoService.listNotInModule(param.getId());
        return new ReturnData<>(result);
    }

    @Operation(summary = "关联资源：添加关联资源", operationId = "addModuleRelationResource", method = "POST")
    @PostMapping("/resource/relate/add")
    public ReturnData<String> addModuleRelationResource(@RequestBody @Valid ModuleResourceParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("addModuleRelationResource param: {}", JsonUtil.serialize(param));
        }
        moduleInfoService.batchInsertRelationResource(param);
        // 不自动更新关联角色的授权缓存，改为在系统页面手动更新（因为资源会持续添加修改，所以不实时刷关联的授权缓存）
        return new ReturnData<>();
    }

    @Operation(summary = "关联资源：删除关联资源", operationId = "deleteModuleRelationResource", method = "POST",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "模块与资源的关联ID", required = true)
    )
    @PostMapping("/resource/relate/delete")
    public ReturnData<String> deleteModuleRelationResource(@RequestBody @Valid IdParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("deleteModuleRelationResource param: {}", JsonUtil.serialize(param));
        }
        moduleInfoService.deleteRelationResourceByPrimaryKey(param.getId());
        // 不自动更新关联角色的授权缓存，改为在系统页面手动更新（因为资源会持续添加修改，所以不实时刷关联的授权缓存）
        return new ReturnData<>();
    }
}
