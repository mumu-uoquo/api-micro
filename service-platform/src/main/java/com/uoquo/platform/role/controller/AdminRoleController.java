package com.uoquo.platform.role.controller;

import com.uoquo.platform.role.model.dto.ModuleInfoDto;
import com.uoquo.platform.role.model.dto.ModuleTreeDto;
import com.uoquo.platform.role.model.dto.RoleInfoDto;
import com.uoquo.platform.role.model.param.RoleInfoParam;
import com.uoquo.platform.role.model.param.RoleListParam;
import com.uoquo.platform.role.model.param.RoleModuleParam;
import com.uoquo.platform.role.service.ModuleInfoService;
import com.uoquo.platform.role.service.RoleInfoService;
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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 角色管理：超管，能操作所有机构
 */
@Tag(name = "adminRole", description="角色管理（超管）")
@Validated
@RestController
@RequestMapping("/admin/v1/role")
public class AdminRoleController{
    final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private RoleInfoService roleInfoService;

    @Autowired
    private ModuleInfoService moduleInfoService;

    @Operation(summary = "新增角色信息", operationId = "addRoleInfo", method = "POST")
    @PostMapping("/add")
    public ReturnData<String> addRoleInfo(@RequestBody @Valid RoleInfoParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("addRoleInfo param: {}", JsonUtil.serialize(param));
        }
        if (StringUtil.isNull(param.getInstituteId())) {
            throw new ParamEmtpyException("机构id不能为空");
        }
        roleInfoService.addRoleInfo(param);
        return new ReturnData<>();
    }

    @Operation(summary = "修改角色信息", operationId = "updateRoleInfo", method = "POST")
    @PostMapping("/update")
    public ReturnData<String> updateRoleInfo(@RequestBody @Valid RoleInfoParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("updateRoleInfo param: {}", JsonUtil.serialize(param));
        }
        // 基本校验
        if (StringUtil.isNull(param.getId())) {
            throw new ParamEmtpyException("id必须传");
        }
        roleInfoService.updateRoleInfo(param);
        return new ReturnData<>();
    }

    @Operation(summary = "删除角色信息", operationId = "deleteRoleInfo", method = "POST",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "角色ID", required = true)
    )
    @PostMapping("/delete")
    public ReturnData<String> deleteRoleInfo(@RequestBody @Valid IdParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("deleteRole param: {}", JsonUtil.serialize(param));
        }
        roleInfoService.deleteRole(param.getId());
        return new ReturnData<>();
    }

    @Operation(summary = "角色详情查询", operationId = "getRoleInfo", method = "POST",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "角色ID", required = true)
    )
    @PostMapping("/info")
    public ReturnData<RoleInfoDto> getRoleInfo(@RequestBody @Valid IdParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("getRoleInfo param: {}", JsonUtil.serialize(param));
        }
        RoleInfoDto roleInfo = roleInfoService.getRoleInfo(param.getId());
        return new ReturnData<>(roleInfo);
    }

    @Operation(summary = "列表查询", operationId = "listRoleInfoByPage", method = "POST")
    @PostMapping("/list/page")
    public ReturnData<PageResult<RoleInfoDto>> listRoleInfoByPage(@RequestBody RoleListParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("listRoleInfoByPage param: {}", JsonUtil.serialize(param));
        }
        PageResult<RoleInfoDto> result = roleInfoService.listRoleInfoByPage(param);
        return new ReturnData<>(result);
    }

    @Operation(summary = "获取角色的模块信息", operationId = "listRoleSelectedModule", method = "POST",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "角色ID", required = true)
    )
    @PostMapping("/module/selected")
    public ReturnData<List<ModuleInfoDto>> listRoleSelectedModule(@RequestBody @Valid IdParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("listRoleSelectedModule param: {}", JsonUtil.serialize(param));
        }
        List<ModuleInfoDto> dto = moduleInfoService.listModuleByRoleId(param.getId());
        return new ReturnData<>(dto);
    }

    @Operation(summary = "模块树查询", operationId = "listModuleByTree", method = "POST")
    @PostMapping("/module/tree")
    public ReturnData<List<ModuleTreeDto>> listModuleByTree() {
        // 获取所有模块列表
        List<ModuleTreeDto> result = moduleInfoService.listModuleTreeByAll();
        return new ReturnData<>(result);
    }

    @Operation(summary = "修改角色的模块信息", operationId = "updateModuleRoleRelation", method = "POST")
    @PostMapping("/module/update")
    public ReturnData<String> updateModuleRoleRelation(@RequestBody @Valid RoleModuleParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("updateModuleRoleRelation param: {}", JsonUtil.serialize(param));
        }
        roleInfoService.updateRoleRelationModule(param.getRoleId(), param.getModuleIds());
        return new ReturnData<>();
    }
}
