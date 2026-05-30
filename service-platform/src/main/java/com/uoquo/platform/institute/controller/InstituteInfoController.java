package com.uoquo.platform.institute.controller;

import com.uoquo.platform.institute.model.dto.InstituteInfoDto;
import com.uoquo.platform.institute.model.dto.InstituteTreeDto;
import com.uoquo.platform.institute.model.param.InstituteInfoParam;
import com.uoquo.platform.institute.model.param.InstituteListParam;
import com.uoquo.platform.institute.model.param.InstituteStateParam;
import com.uoquo.platform.institute.service.InstituteInfoService;
import com.uoquo.utils.CurrentUser;
import com.uoquo.utils.StringUtil;
import com.uoquo.utils.json.JsonUtil;
import com.uoquo.web.ReturnData;
import com.uoquo.web.param.IdParam;
import com.uoquo.web.exception.ForbiddenException;
import com.uoquo.web.exception.ParamEmtpyException;
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
 * 企业管理：普通，只能操作自己的及下属所有的机构
 */
@Tag(name = "institute", description = "企业信息相关")
@Validated
@RestController
@RequestMapping("/v1/institute")
public class InstituteInfoController {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private InstituteInfoService instituteInfoService;

    @Operation(summary = "新增企业信息", operationId = "addInstituteInfo", method = "POST")
    @PostMapping("/add")
    public ReturnData<String> addInstituteInfo(@RequestBody @Valid InstituteInfoParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("addInstituteInfo param: {}", JsonUtil.serialize(param));
        }
        // 权限判断：只能管理自己管辖的机构
        if (StringUtil.notNull(param.getParentId())) {
            boolean flag = instituteInfoService.checkSelfManageInstitute(param.getParentId());
            if (!flag) {
                throw new ForbiddenException("只能管理自己管辖的机构");
            }
        } else {
            // 补齐父节点信息
            param.setParentId(CurrentUser.getInfo().getInstituteId());
        }
        // 普通用户不允许修改这两项，需随父机构走
        param.setRoleGroup(null);
        param.setInstituteType(null);
        instituteInfoService.addInstituteInfo(param);
        return new ReturnData<>();
    }

    @Operation(summary = "修改企业信息", operationId = "updateInstituteInfo", method = "POST")
    @PostMapping("/update")
    public ReturnData<String> updateInstituteInfo(@RequestBody @Valid InstituteInfoParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("updateInstituteInfo param: {}", JsonUtil.serialize(param));
        }
        // 基本校验
        if (StringUtil.isNull(param.getId())) {
            throw new ParamEmtpyException("id必须传");
        }
        // 权限判断：只能管理自己管辖的机构
        boolean flag = instituteInfoService.checkSelfManageInstitute(param.getId());
        if (!flag) {
            throw new ForbiddenException("只能管理自己管辖的机构");
        }
        // 普通用户不允许修改这两项
        param.setRoleGroup(null);
        param.setInstituteType(null);
        // 信息修改
        instituteInfoService.updateInstituteInfo(param);
        return new ReturnData<>();
    }

    @Operation(summary = "更新状态", operationId = "updateInstituteState", method = "POST")
    @PostMapping("/update/status")
    public ReturnData<String> updateInstituteState(@RequestBody @Valid InstituteStateParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("updateState param: {}", JsonUtil.serialize(param));
        }
        // 权限判断：只能管理自己管辖的机构
        boolean flag = instituteInfoService.checkSelfManageInstitute(param.getId());
        if (!flag) {
            throw new ForbiddenException("只能管理自己管辖的机构");
        }
        // 变更状态
        instituteInfoService.updateInstituteStatus(param);
        return new ReturnData<>();
    }

    @Operation(summary = "删除企业信息", operationId = "deleteInstituteInfo", method = "POST",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "企业ID", required = true)
    )
    @PostMapping("/delete")
    public ReturnData<String> deleteInstituteInfo(@RequestBody @Valid IdParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("addRoleInfo param: {}", JsonUtil.serialize(param));
        }
        // 权限判断：只能管理自己管辖的机构
        boolean flag = instituteInfoService.checkSelfManageInstitute(param.getId());
        if (!flag) {
            throw new ForbiddenException("只能管理自己管辖的机构");
        }
        // 执行删除
        instituteInfoService.deleteInstituteInfo(param.getId());
        return new ReturnData<>();
    }

    @Operation(summary = "企业信息详情", operationId = "getInstituteInfo", method = "POST",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "企业ID", required = true)
    )
    @RequestMapping(value = "/info", method = RequestMethod.POST)
    public ReturnData<InstituteInfoDto> getInstituteInfo(@RequestBody @Valid IdParam param) {
        // 权限判断：只能管理自己管辖的机构
        boolean flag = instituteInfoService.checkSelfManageInstitute(param.getId());
        if (!flag) {
            throw new ForbiddenException("只能管理自己管辖的机构");
        }
        InstituteInfoDto dto = instituteInfoService.getInstituteInfo(param.getId());
        return new ReturnData<>(dto);
    }

    @Operation(summary = "企业列表（分页）", operationId = "listInstituteByPage", method = "POST")
    @RequestMapping(value = "/list/page", method = RequestMethod.POST)
    public ReturnData<PageResult<InstituteInfoDto>> listInstituteByPage(@RequestBody @Valid InstituteListParam param) {
        // 权限判断：只能管理自己管辖的机构
        if (StringUtil.notNull(param.getParentId())) {
            boolean flag = instituteInfoService.checkSelfManageInstitute(param.getParentId());
            if (!flag) {
                throw new ForbiddenException("只能管理自己管辖的机构");
            }
        }
        // 只能查询自己的所有子机构
        String rootInstituteId = CurrentUser.getInfo().getInstituteId();
        PageResult<InstituteInfoDto> result = instituteInfoService.listInstituteInfoByPage(rootInstituteId, param);
        return new ReturnData<>(result);
    }

    @Operation(summary = "企业列表（简单检索）", operationId = "listInstituteByAbbr", method = "POST")
    @RequestMapping(value = "/list/abbr", method = RequestMethod.POST)
    public ReturnData<List<InstituteInfoDto>> listInstituteByAbbr(@RequestBody @Valid InstituteListParam param) {
        // 权限判断：只能管理自己管辖的机构
        if (StringUtil.notNull(param.getParentId())) {
            boolean flag = instituteInfoService.checkSelfManageInstitute(param.getParentId());
            if (!flag) {
                throw new ForbiddenException("只能管理自己管辖的机构");
            }
        }
        // 只能查询自己的所有子机构
        String rootInstituteId = CurrentUser.getInfo().getInstituteId();
        PageResult<InstituteInfoDto> result = instituteInfoService.listInstituteInfoByPage(rootInstituteId, param);
        return new ReturnData<>(result.getResult());
    }

    @Operation(summary = "企业列表（树状）", operationId = "listInstituteByTree", method = "POST")
    @RequestMapping(value = "/list/tree", method = RequestMethod.POST)
    public ReturnData<List<InstituteTreeDto>> listInstituteByTree() {
        // 只能查询自己的所有子机构
        String rootInstituteId = CurrentUser.getInfo().getInstituteId();
        List<InstituteTreeDto> result = instituteInfoService.listInstituteInfoByTree(rootInstituteId);
        return new ReturnData<>(result);
    }
}
