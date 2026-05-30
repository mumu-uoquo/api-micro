package com.uoquo.platform.institute.controller;

import com.uoquo.platform.institute.model.dto.AreaInfoDto;
import com.uoquo.platform.institute.model.dto.DepartmentTreeDto;
import com.uoquo.platform.institute.model.dto.InstituteInfoDto;
import com.uoquo.platform.institute.model.dto.InstituteTreeDto;
import com.uoquo.platform.institute.model.param.InstituteInfoParam;
import com.uoquo.platform.institute.model.param.InstituteListParam;
import com.uoquo.platform.institute.model.param.InstituteStateParam;
import com.uoquo.platform.institute.service.AreaInfoService;
import com.uoquo.platform.institute.service.DepartmentInfoService;
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
 * 企业管理：超管，能操作所有机构
 */
@Tag(name = "adminInstitute", description = "企业管理（超管）")
@Validated
@RestController
@RequestMapping("/admin/v1/institute")
public class AdminInstituteInfoController {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private InstituteInfoService instituteInfoService;

    @Autowired
    private AreaInfoService areaInfoService;

    @Autowired
    private DepartmentInfoService departmentInfoService;

    /* *******************  企业管理 ******************* */
    @Operation(summary = "新增企业信息", operationId = "addInstituteInfo", method = "POST")
    @PostMapping("/add")
    public ReturnData<String> addInstituteInfo(@RequestBody @Valid InstituteInfoParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("addInstituteInfo param: {}", JsonUtil.serialize(param));
        }
        // 补齐父节点信息
        if (StringUtil.isNull(param.getParentId())) {
            param.setParentId(CurrentUser.getInfo().getInstituteId());
        }
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
        // 执行删除
        instituteInfoService.deleteInstituteInfo(param.getId());
        return new ReturnData<>();
    }

    @Operation(summary = "企业信息详情", operationId = "getInstituteInfo", method = "POST",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "企业ID", required = true)
    )
    @RequestMapping(value = "/info", method = RequestMethod.POST)
    public ReturnData<InstituteInfoDto> getInstituteInfo(@RequestBody @Valid IdParam param) {
        InstituteInfoDto dto = instituteInfoService.getInstituteInfo(param.getId());
        return new ReturnData<>(dto);
    }

    @Operation(summary = "企业列表（分页）", operationId = "listInstituteByPage", method = "POST")
    @RequestMapping(value = "/list/page", method = RequestMethod.POST)
    public ReturnData<PageResult<InstituteInfoDto>> listInstituteByPage(@RequestBody @Valid InstituteListParam param) {
        PageResult<InstituteInfoDto> result = instituteInfoService.listInstituteInfoByPage(null, param);
        return new ReturnData<>(result);
    }

    @Operation(summary = "企业列表（简单检索）", operationId = "listInstituteByAbbr", method = "POST")
    @RequestMapping(value = "/list/abbr", method = RequestMethod.POST)
    public ReturnData<List<InstituteInfoDto>> listInstituteByAbbr(@RequestBody @Valid InstituteListParam param) {
        PageResult<InstituteInfoDto> result = instituteInfoService.listInstituteInfoByPage(null, param);
        return new ReturnData<>(result.getResult());
    }

    @Operation(summary = "企业列表（树状）", operationId = "listInstituteByTree", method = "POST")
    @RequestMapping(value = "/list/tree", method = RequestMethod.POST)
    public ReturnData<List<InstituteTreeDto>> listInstituteByTree() {
        List<InstituteTreeDto> result = instituteInfoService.listInstituteInfoByTree(null);
        return new ReturnData<>(result);
    }

    /* *******************  部门管理 ******************* */
    @Operation(summary = "删除部门信息", operationId = "deleteDepartmentInfo", method = "POST",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "部门ID", required = true)
    )
    @PostMapping("/department/delete")
    public ReturnData<String> deleteDepartmentInfo(@RequestBody @Valid IdParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("deleteDepartmentInfo param: {}", JsonUtil.serialize(param));
        }
        // 有效判断
        DepartmentTreeDto dto = departmentInfoService.getDepartmentInfo(param.getId());
        if (dto.getDefaulted()) {
            throw new ForbiddenException("默认部门不允许删除");
        }
        // 执行删除
        departmentInfoService.deleteDepartmentInfo(param.getId());
        return new ReturnData<>();
    }

    /* *******************  分区管理 ******************* */
    @Operation(summary = "删除分区信息", operationId = "deleteAreaInfo", method = "POST",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "分区ID", required = true)
    )
    @PostMapping("/area/delete")
    public ReturnData<String> deleteAreaInfo(@RequestBody @Valid IdParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("deleteAreaInfo param: {}", JsonUtil.serialize(param));
        }
        // 有效判断
        AreaInfoDto dto = areaInfoService.getAreaInfo(param.getId());
        if (dto.getDefaulted()) {
            throw new ForbiddenException("默认分区不允许删除");
        }
        // 执行删除
        areaInfoService.deleteAreaInfo(param.getId());
        return new ReturnData<>();
    }
}
