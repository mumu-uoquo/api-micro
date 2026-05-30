package com.uoquo.platform.institute.controller;

import com.uoquo.platform.institute.model.dto.AreaInfoDto;
import com.uoquo.platform.institute.model.dto.DepartmentTreeDto;
import com.uoquo.platform.institute.model.param.*;
import com.uoquo.platform.institute.service.AreaInfoService;
import com.uoquo.platform.institute.service.DepartmentInfoService;
import com.uoquo.platform.institute.service.InstituteInfoService;
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
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 部门管理、区域管理：操作指定机构下的（不再做权限的强判断）
 */
@Tag(name = "department", description = "部门信息相关")
@Validated
@RestController
@RequestMapping("/v1/department")
public class DepartmentInfoController {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private InstituteInfoService instituteInfoService;

    @Autowired
    private AreaInfoService areaInfoService;

    @Autowired
    private DepartmentInfoService departmentInfoService;


    /* *******************  部门管理 ******************* */
    @Operation(summary = "新增部门信息", operationId = "addDepartmentInfo", method = "POST")
    @PostMapping("/add")
    public ReturnData<String> addDepartmentInfo(@RequestBody @Valid DepartmentInfoParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("addDepartmentInfo param: {}", JsonUtil.serialize(param));
        }
        // 目前仅创建机构时自动创建的为默认，手动不允许创建默认
        param.setDefaulted(false);
        String deptId = departmentInfoService.addDepartmentInfo(param);
        return new ReturnData<>(deptId);
    }

    @Operation(summary = "修改部门信息", operationId = "updateDepartmentInfo", method = "POST")
    @PostMapping("/update")
    public ReturnData<String> updateDepartmentInfo(@RequestBody @Valid DepartmentInfoParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("updateDepartmentInfo param: {}", JsonUtil.serialize(param));
        }
        // 基本校验
        if (StringUtil.isNull(param.getId())) {
            throw new ParamEmtpyException("id必须传");
        }
        // 信息修改
        departmentInfoService.updateDepartmentInfo(param);
        return new ReturnData<>();
    }

    @Operation(summary = "删除部门信息", operationId = "deleteDepartmentInfo", method = "POST",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "部门ID", required = true)
    )
    @PostMapping("/delete")
    public ReturnData<String> deleteDepartmentInfo(@RequestBody @Valid IdParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("deleteDepartmentInfo param: {}", JsonUtil.serialize(param));
        }
        // 权限判断：只能管理自己管辖的机构
        DepartmentTreeDto dto = departmentInfoService.getDepartmentInfo(param.getId());
        boolean flag = instituteInfoService.checkSelfManageInstitute(dto.getInstituteId());
        if (!flag) {
            throw new ForbiddenException("只能管理自己管辖的机构");
        }
        if (dto.getDefaulted()) {
            throw new ForbiddenException("默认部门不允许删除");
        }
        // 执行删除
        departmentInfoService.deleteDepartmentInfo(param.getId());
        return new ReturnData<>();
    }

    @Operation(summary = "部门列表（树状）", operationId = "listDepartmentByTree", method = "POST",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "企业ID", required = true)
    )
    @RequestMapping(value = "/list/tree", method = RequestMethod.POST)
    public ReturnData<List<DepartmentTreeDto>> listDepartmentByTree(@RequestBody @Valid IdParam param) {
        List<DepartmentTreeDto> result = departmentInfoService.listDepartmentInfoByTree(param.getId());
        return new ReturnData<>(result);
    }

    /* *******************  分区管理 ******************* */
    @Operation(summary = "新增分区信息", operationId = "addAreaInfo", method = "POST")
    @PostMapping("/area/add")
    public ReturnData<String> addAreaInfo(@RequestBody @Valid AreaInfoParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("addAreaInfo param: {}", JsonUtil.serialize(param));
        }
        // 目前仅创建机构时自动创建的为默认，手动不允许创建默认
        param.setDefaulted(false);
        String areaId = areaInfoService.addAreaInfo(param);
        return new ReturnData<>(areaId);
    }

    @Operation(summary = "修改分区信息", operationId = "updateAreaInfo", method = "POST")
    @PostMapping("/area/update")
    public ReturnData<String> updateAreaInfo(@RequestBody @Valid AreaInfoParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("updateAreaInfo param: {}", JsonUtil.serialize(param));
        }
        // 基本校验
        if (StringUtil.isNull(param.getId())) {
            throw new ParamEmtpyException("id必须传");
        }
        // 信息修改
        areaInfoService.updateAreaInfo(param);
        return new ReturnData<>();
    }

    @Operation(summary = "删除分区信息", operationId = "deleteAreaInfo", method = "POST",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "分区ID", required = true)
    )
    @PostMapping("/area/delete")
    public ReturnData<String> deleteAreaInfo(@RequestBody @Valid IdParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("deleteAreaInfo param: {}", JsonUtil.serialize(param));
        }
        // 权限判断：只能管理自己管辖的机构
        AreaInfoDto dto = areaInfoService.getAreaInfo(param.getId());
        boolean flag = instituteInfoService.checkSelfManageInstitute(dto.getInstituteId());
        if (!flag) {
            throw new ForbiddenException("只能管理自己管辖的机构");
        }
        if (dto.getDefaulted()) {
            throw new ForbiddenException("默认分区不允许删除");
        }
        // 执行删除
        areaInfoService.deleteAreaInfo(param.getId());
        return new ReturnData<>();
    }

    @Operation(summary = "取消部门关联的分区", operationId = "unlinkDepartmentAreaInfo", method = "POST",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "部门ID", required = true)
    )
    @PostMapping("/area/unlink")
    public ReturnData<String> unlinkDepartmentAreaInfo(@RequestBody @Valid IdParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("unlinkDepartmentAreaInfo param: {}", JsonUtil.serialize(param));
        }
        // 基本校验
        if (StringUtil.isNull(param.getId())) {
            throw new ParamEmtpyException("id必须传");
        }
        // 信息修改
        departmentInfoService.updateDepartmentArea2Default(param.getId());
        return new ReturnData<>();
    }

    @Operation(summary = "分区列表", operationId = "listArea", method = "POST",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "企业ID", required = true)
    )
    @RequestMapping(value = "/area/list", method = RequestMethod.POST)
    public ReturnData<List<AreaInfoDto>> listArea(@RequestBody @Valid IdParam param) {
        List<AreaInfoDto> result = areaInfoService.listAreaInfoByList(param.getId());
        return new ReturnData<>(result);
    }
}
