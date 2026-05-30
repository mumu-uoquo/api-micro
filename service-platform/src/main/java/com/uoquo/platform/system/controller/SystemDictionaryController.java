package com.uoquo.platform.system.controller;

import com.uoquo.platform.system.model.dto.SysAreaDto;
import com.uoquo.platform.system.model.dto.SysDictionaryDto;
import com.uoquo.platform.system.model.dto.SysDictionarySimpleDto;
import com.uoquo.platform.system.model.param.SysDictionaryParam;
import com.uoquo.platform.system.model.param.SysDictionarySearchParam;
import com.uoquo.platform.system.service.SysAreaService;
import com.uoquo.platform.system.service.SysDictionaryService;
import com.uoquo.utils.StringUtil;
import com.uoquo.utils.json.JsonUtil;
import com.uoquo.web.ReturnData;
import com.uoquo.web.param.IdParam;
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

@Tag(name = "system", description = "系统字典")
@Validated
@RestController
@RequestMapping("/v1/system/dictionary")
public class SystemDictionaryController {

    final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private SysDictionaryService sysDictionaryService;

    @Autowired
    private SysAreaService sysAreaService;

    /* *******************  字典（管理） ******************* */
    @Operation(summary = "保存字典信息", operationId = "saveDictionaryInfo", method = "POST")
    @PostMapping("/info/save")
    public ReturnData<String> saveDictionaryInfo(@RequestBody @Valid SysDictionaryParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("saveDictionaryInfo param: {}", JsonUtil.serialize(param));
        }
        String id = param.getId();
        if (StringUtil.notNull(id)) {
            sysDictionaryService.updateInfo(param);
        } else {
            id = sysDictionaryService.addInfo(param);
        }
        return new ReturnData<>(id);
    }

    @Operation(summary = "删除字典信息", operationId = "deleteDictionaryInfo", method = "POST",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "字典ID", required = true)
    )
    @PostMapping("/info/delete")
    public ReturnData<String> deleteDictionaryInfo(@RequestBody @Valid IdParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("deleteDictionaryInfo param: {}", JsonUtil.serialize(param));
        }
        sysDictionaryService.deleteInfo(param.getId());
        return new ReturnData<>();
    }

    @Operation(summary = "获取所有的字典列表", operationId = "listDictionaryByAll", method = "POST")
    @PostMapping("/list/all")
    public ReturnData<List<SysDictionaryDto>> listDictionaryByAll() {
        List<SysDictionaryDto> result = sysDictionaryService.listAllDictionary();
        return new ReturnData<>(result);
    }
    @Operation(summary = "获取指定开头的字典列表", operationId = "listDictionaryByCode", method = "POST")
    @PostMapping("/list/prefix")
    public ReturnData<List<SysDictionaryDto>> listDictionaryByCode(@RequestBody SysDictionarySearchParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("listDictionaryByCode param: {}", JsonUtil.serialize(param));
        }
        List<SysDictionaryDto> result = sysDictionaryService.listByPrefix(param.getItemCode());
        return new ReturnData<>(result);
    }

    /* *******************  字典简版（查询） ******************* */
    @Operation(summary = "获取指定开头的字典列表（简版）", operationId = "listDictionarySimpleByCode", method = "POST")
    @PostMapping("/list/simple/prefix")
    public ReturnData<List<SysDictionarySimpleDto>> listDictionarySimpleByCode(@RequestBody @Valid SysDictionarySearchParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("listDictionarySimpleByCode param: {}", JsonUtil.serialize(param));
        }
        List<SysDictionarySimpleDto> result = sysDictionaryService.listSimpleByPrefix(param.getItemCode());
        return new ReturnData<>(result);
    }

    @Operation(summary = "获取所有的字典列表（简版）", operationId = "listDictionarySimpleByAll", method = "POST")
    @PostMapping("/list/simple/all")
    public ReturnData<List<SysDictionarySimpleDto>> listDictionarySimpleByAll() {
        List<SysDictionarySimpleDto> result = sysDictionaryService.listSimpleByAll();
        return new ReturnData<>(result);
    }

    /* *******************  地区（查询） ******************* */
    @Operation(summary = "获取指定的地区下级区域", operationId = "listAreaByCode", method = "POST",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "地区编码")
    )
    @PostMapping("/list/area")
    public ReturnData<List<SysAreaDto>> listAreaByCode(@RequestBody IdParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("listAreaByCode param: {}", JsonUtil.serialize(param));
        }
        List<SysAreaDto> result = sysAreaService.selectByTree4PrevCode(param.getId());
        return new ReturnData<>(result);
    }

}
