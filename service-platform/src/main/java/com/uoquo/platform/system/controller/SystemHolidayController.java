package com.uoquo.platform.system.controller;

import com.uoquo.platform.system.model.dto.SysHolidayDto;
import com.uoquo.platform.system.model.param.SysHolidayInfoParam;
import com.uoquo.platform.system.model.param.SysHolidaySearchParam;
import com.uoquo.platform.system.service.SysHolidayService;
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

@Tag(name = "system", description = "节假日管理")
@Validated
@RestController
@RequestMapping("/v1/system/holiday")
public class SystemHolidayController {

    final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private SysHolidayService sysHolidayService;

    @Operation(summary = "新增节假日", operationId = "addHolidayInfo", method = "POST")
    @PostMapping("/add/info")
    public ReturnData<String> addHolidayInfo(@RequestBody @Valid SysHolidayInfoParam param) {
        String id = sysHolidayService.saveHolidayInfo(param);
        return new ReturnData<>(id);
    }

    @Operation(summary = "新增节假日", operationId = "addHolidayList", method = "POST",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "节假日集合", required = true)
    )
    @PostMapping("/add/list")
    public ReturnData<String> addHolidayList(@RequestBody @Valid List<SysHolidayInfoParam> params) {
        sysHolidayService.batchSaveHolidayInfo(params);
        return new ReturnData<>();
    }

    @Operation(summary = "修改节假日信息", operationId = "updateHolidayInfo", method = "POST")
    @PostMapping("/update")
    public ReturnData<String> updateHolidayInfo(@RequestBody @Valid SysHolidayInfoParam param) {
        sysHolidayService.updateHolidayInfo(param);
        return new ReturnData<>();
    }

    @Operation(summary = "删除节假日信息", operationId = "deleteHolidayInfo", method = "POST",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "节假日ID", required = true)
    )
    @PostMapping("/delete")
    public ReturnData<String> deleteHolidayInfo(@RequestBody @Valid IdParam param) {
        sysHolidayService.deleteHolidayInfo(param.getId());
        return new ReturnData<>();
    }

    @Operation(summary = "获取指定范围内的节假日", operationId = "listHoliday", method = "POST")
    @PostMapping("/list/all")
    public ReturnData<List<SysHolidayDto>> listHoliday(@RequestBody @Valid SysHolidaySearchParam param) {
        List<SysHolidayDto> result = sysHolidayService.listHolidayInfo(param);
        return new ReturnData<>(result);
    }

}
