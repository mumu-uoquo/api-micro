package com.uoquo.platform.role.controller;

import com.uoquo.platform.common.DictionaryCodeEnum;
import com.uoquo.platform.institute.service.InstituteInfoService;
import com.uoquo.platform.role.model.dto.ModuleInfoDto;
import com.uoquo.platform.role.model.dto.RoleInfoDto;
import com.uoquo.platform.role.model.param.RoleInfoParam;
import com.uoquo.platform.role.model.param.RoleListParam;
import com.uoquo.platform.role.model.param.RoleModuleParam;
import com.uoquo.platform.role.service.ModuleInfoService;
import com.uoquo.platform.role.service.RoleInfoService;
import com.uoquo.utils.CurrentUser;
import com.uoquo.utils.StringUtil;
import com.uoquo.utils.json.JsonUtil;
import com.uoquo.web.ReturnData;
import com.uoquo.web.param.IdParam;
import com.uoquo.web.exception.ForbiddenException;
import com.uoquo.web.exception.ParamEmtpyException;
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
 * 角色管理：普通，只能操作自己的及下属所有的机构
 */
@Tag(name = "role", description = "角色管理")
@Validated
@RestController
@RequestMapping("/v1/role")
public class RoleController {
    final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private RoleInfoService roleInfoService;

    @Autowired
    private ModuleInfoService moduleInfoService;

    @Autowired
    private InstituteInfoService instituteInfoService;

    @Operation(summary = "新增角色信息", operationId = "addRoleInfo", method = "POST")
    @PostMapping("/add")
    public ReturnData<String> addRoleInfo(@RequestBody @Valid RoleInfoParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("addRoleInfo param: {}", JsonUtil.serialize(param));
        }
        // 权限判断：只能添加自己管辖机构的角色
        if (StringUtil.notNull(param.getInstituteId())) {
            boolean flag = instituteInfoService.checkSelfManageInstitute(param.getInstituteId());
            if (!flag) {
                throw new ForbiddenException("只能给自己管辖机构添加角色");
            }
        } else {
            // 默认为当前用户所在机构
            param.setInstituteId(CurrentUser.getInfo().getInstituteId());
        }
        // 权限判断：只能复制自己管辖机构的角色
        if (StringUtil.notNull(param.getFromRoleId())) {
            RoleInfoDto info = roleInfoService.getRoleInfo(param.getFromRoleId());
            boolean flag = instituteInfoService.checkSelfManageInstitute(info.getInstituteId());
            if (!flag) {
                throw new ForbiddenException("只能复制自己管辖机构的角色");
            }
        }
        // 角色默认都是30等级
        param.setRoleGrade(30);
        // 除了指定为通用的，其他都是私有的
        if (DictionaryCodeEnum.ROLE_TYPE_NORMAL.getCode().equals(param.getRoleType())) {
            param.setRoleType(DictionaryCodeEnum.ROLE_TYPE_NORMAL.getCode());
        } else {
            param.setRoleType(DictionaryCodeEnum.ROLE_TYPE_PRIVATE.getCode());
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
        // 基础校验
        if (StringUtil.isNull(param.getId())) {
            throw new ParamEmtpyException("id必须传");
        }
        // 权限判断：只能修改自己管辖机构的角色
        RoleInfoDto info = roleInfoService.getRoleInfo(param.getId());
        boolean flag = instituteInfoService.checkSelfManageInstitute(info.getInstituteId());
        if (!flag) {
            throw new ForbiddenException("只能管理自己管辖机构的角色");
        }
        // 信息修改
        // 普通管理员只能修改名称
        param.setRoleGrade(null);
        param.setRoleType(null);
        roleInfoService.updateRoleInfo(param);
        return new ReturnData<>();
    }

    @Operation(summary = "删除角色信息", operationId = "deleteRoleInfo", method = "POST",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "角色ID", required = true)
    )
    @PostMapping("/delete")
    public ReturnData<String> deleteRoleInfo(@RequestBody @Valid IdParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("deleteRoleInfo param: {}", JsonUtil.serialize(param));
        }
        // 权限判断：只能修改自己管辖机构的角色
        RoleInfoDto info = roleInfoService.getRoleInfo(param.getId());
        boolean flag = instituteInfoService.checkSelfManageInstitute(info.getInstituteId());
        if (!flag) {
            throw new ForbiddenException("只能管理自己管辖机构的角色");
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
        // 权限判断：只能修改自己管辖机构的角色
        RoleInfoDto dto = roleInfoService.getRoleInfo(param.getId());
        boolean flag = instituteInfoService.checkSelfManageInstitute(dto.getInstituteId());
        if (!flag) {
            throw new ForbiddenException("只能管理自己管辖机构的角色");
        }
        return new ReturnData<>(dto);
    }

    @Operation(summary = "列表查询", operationId = "listRoleInfo", method = "POST")
    @PostMapping("/list/search")
    public ReturnData<List<RoleInfoDto>> listRoleInfo(@RequestBody RoleListParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("listRoleInfo param: {}", JsonUtil.serialize(param));
        }
        // 权限判断：只能查询自己管辖机构的角色
        if (StringUtil.notNull(param.getInstituteId())) {
            boolean flag = instituteInfoService.checkSelfManageInstitute(param.getInstituteId());
            if (!flag) {
                throw new ForbiddenException("只能给自己管辖机构添加角色");
            }
        } else {
            // 默认为当前用户所在机构
            param.setInstituteId(CurrentUser.getInfo().getInstituteId());
        }
        // 仅获取当前角色所在分组的
        RoleInfoDto role = roleInfoService.getRoleInfo(CurrentUser.getInfo().getCurrentRoleId());
        param.setRoleGroup(role.getRoleGroup());
        param.setRoleGrade(role.getRoleGrade());
        // 执行查询
        List<RoleInfoDto> result = roleInfoService.listRoleInfoByInstitute(param);
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
        // 权限判断：只能查询自己管辖机构的角色
        RoleInfoDto info = roleInfoService.getRoleInfo(param.getId());
        boolean flag = instituteInfoService.checkSelfManageInstitute(info.getInstituteId());
        if (!flag) {
            throw new ForbiddenException("只能管理自己管辖机构的角色");
        }
        List<ModuleInfoDto> dto = moduleInfoService.listModuleByRoleId(param.getId());
        return new ReturnData<>(dto);
    }

    @Operation(summary = "修改角色的模块信息", operationId = "updateModuleRoleRelation", method = "POST")
    @PostMapping("/module/update")
    public ReturnData<String> updateModuleRoleRelation(@RequestBody @Valid RoleModuleParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("updateModuleRoleRelation param: {}", JsonUtil.serialize(param));
        }
        // 基础校验
        if (CurrentUser.getInfo().getCurrentRoleId().equals(param.getRoleId())) {
            throw new ForbiddenException("不能修改当前的角色的授权模块");
        }
        // 权限判断：只能管理自己管辖机构的角色
        RoleInfoDto info = roleInfoService.getRoleInfo(param.getRoleId());
        boolean flag = instituteInfoService.checkSelfManageInstitute(info.getInstituteId());
        if (!flag) {
            throw new ForbiddenException("只能管理自己管辖机构的角色");
        }
        // 需循环处理，避免父节点或子节点遗漏
        param.getModuleIds().forEach(moduleId -> {
            roleInfoService.updateRoleRelationModule(param.getRoleId(), moduleId);
        });
        return new ReturnData<>();
    }

}
