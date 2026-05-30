package com.uoquo.platform.system.controller;

import com.uoquo.platform.system.model.dto.SysReturnCodeDto;
import com.uoquo.platform.system.model.param.SysReturnCodeParam;
import com.uoquo.platform.system.model.param.SysReturnCodeSearchParam;
import com.uoquo.platform.system.service.SysReturnCodeService;
import com.uoquo.utils.json.JsonUtil;
import com.uoquo.web.ReturnData;
import com.uoquo.web.param.IdParam;
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

@Tag(name = "system", description = "系统响应码")
@Validated
@RestController
@RequestMapping("/v1/system/return-code")
public class SystemReturnCodeController {

    final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private SysReturnCodeService sysReturnCodeService;

    @Operation(summary = "新增系统响应码", operationId = "saveReturnCode", method = "POST")
    @PostMapping("/save")
    public ReturnData<String> saveReturnCode(@RequestBody @Valid SysReturnCodeParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("saveReturnCode param: {}", JsonUtil.serialize(param));
        }
        String id = sysReturnCodeService.saveReturnCode(param);
        return new ReturnData<>(id);
    }

    @Operation(summary = "删除系统响应码", operationId = "deleteReturnCode", method = "POST")
    @PostMapping("/delete")
    public ReturnData<String> deleteReturnCode(@RequestBody @Valid IdParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("deleteReturnCode param: {}", JsonUtil.serialize(param));
        }
        sysReturnCodeService.deleteReturnCode(param.getId());
        return new ReturnData<>();
    }

    @Operation(summary = "查询系统响应码详情（按ID）", operationId = "getReturnCodeById", method = "POST")
    @PostMapping("/info/id")
    public ReturnData<SysReturnCodeDto> getReturnCodeById(@RequestBody @Valid IdParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("getReturnCodeById param: {}", JsonUtil.serialize(param));
        }
        SysReturnCodeDto dto = sysReturnCodeService.getReturnCodeById(param.getId());
        return new ReturnData<>(dto);
    }

    @Operation(summary = "查询系统响应码详情（按响应码）", operationId = "getReturnCodeByCode", method = "POST")
    @PostMapping("/info/code")
    public ReturnData<SysReturnCodeDto> getReturnCodeByCode(@RequestBody @Valid SysReturnCodeParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("getReturnCodeByCode param: {}", JsonUtil.serialize(param));
        }
        SysReturnCodeDto dto = sysReturnCodeService.getReturnCodeByCode(param.getReturnCode());
        return new ReturnData<>(dto);
    }

    @Operation(summary = "分页查询系统响应码", operationId = "listReturnCodeByPage", method = "POST")
    @PostMapping("/list/page")
    public ReturnData<PageResult<SysReturnCodeDto>> listReturnCodeByPage(@RequestBody @Valid SysReturnCodeSearchParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("listReturnCodeByPage param: {}", JsonUtil.serialize(param));
        }
        PageResult<SysReturnCodeDto> result = sysReturnCodeService.listReturnCodeByPage(param);
        return new ReturnData<>(result);
    }

    @Operation(summary = "查询所有系统响应码", operationId = "listAllReturnCodes", method = "POST")
    @PostMapping("/list/all")
    public ReturnData<List<SysReturnCodeDto>> listAllReturnCodes() {
        List<SysReturnCodeDto> result = sysReturnCodeService.listAllReturnCodes();
        return new ReturnData<>(result);
    }
}
