package com.uoquo.platform.logs.controller;

import com.uoquo.platform.logs.model.dto.*;
import com.uoquo.platform.logs.model.param.BizEventRecordSearchParam;
import com.uoquo.platform.logs.model.param.LogsAuthSearchParam;
import com.uoquo.platform.logs.model.param.LogsBusinessSearchParam;
import com.uoquo.platform.logs.service.BizEventRecordService;
import com.uoquo.platform.logs.service.LogsBusinessService;
import com.uoquo.platform.logs.service.LogsAuthService;
import com.uoquo.utils.CurrentUser;
import com.uoquo.utils.StringUtil;
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

/**
 * 日志信息（普通查询）<br>
 * 备注：只允许查看自己机构的日志
 * @author xuhz
 */
@Tag(name = "logs", description = "日志记录")
@Validated
@RestController
@RequestMapping("/v1/logs")
public class LogsInfoController {
    final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private LogsAuthService logsAuthService;

    @Autowired
    private LogsBusinessService logsBusinessService;

    @Autowired
    private BizEventRecordService bizEventRecordService;

    @Operation(summary = "登录日志：列表", operationId = "logsByAuthList", method = "POST")
    @PostMapping("/auth/list")
    public ReturnData<PageResult<LogUserLoginDto>> logsByAuthList(@RequestBody LogsAuthSearchParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("logsByAuthList param: {}", JsonUtil.serialize(param));
        }
        // 默认查自己的日志
        if (StringUtil.isNull(param.getUserId())) {
            param.setUserId(CurrentUser.getInfo().getUserId());
        }
        // 没指定机构时，默认查自己机构的记录
        if (StringUtil.isNull(param.getInstituteId())) {
            param.setInstituteId(CurrentUser.getInfo().getInstituteId());
        }
        PageResult<LogUserLoginDto> list = logsAuthService.listBySearch(param);
        return new ReturnData<>(list);
    }

    @Operation(summary = "登录日志：详情", operationId = "logsByAuthInfo", method = "POST",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "登录日志ID", required = true)
    )
    @PostMapping("/auth/info")
    public ReturnData<LogUserLoginDto> logsByAuthInfo(@RequestBody @Valid IdParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("logsByAuthInfo param: {}", JsonUtil.serialize(param));
        }
        LogUserLoginDto dto = logsAuthService.getInfoById(param.getId());
        return new ReturnData<>(dto);
    }
    @Operation(summary = "操作日志：列表", operationId = "logsByOperationList", method = "POST")
    @PostMapping("/operation/list")
    public ReturnData<PageResult<BizEventRecordDto>> logsByOperationList(@RequestBody BizEventRecordSearchParam param) {
        // 默认查自己的日志
        if (StringUtil.isNull(param.getOperatorId())) {
            param.setOperatorId(CurrentUser.getInfo().getUserId());
        }
        // 没指定机构时，默认查自己机构的记录
        if (StringUtil.isNull(param.getBusinessInstituteId()) && StringUtil.isNull(param.getOperatorInstituteId())) {
//            param.setBusinessInstituteId(CurrentUser.getInfo().getInstituteId());
            param.setOperatorInstituteId(CurrentUser.getInfo().getInstituteId());
        }
        PageResult<BizEventRecordDto> result = bizEventRecordService.listRecords(param);
        return new ReturnData<>(result);
    }

    @Operation(summary = "操作日志：详情", operationId = "logsByOperationInfo", method = "POST",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "事件记录ID", required = true)
    )
    @PostMapping("/operation/info")
    public ReturnData<BizEventRecordDto> logsByOperationInfo(@RequestBody @Valid IdParam param) {
        BizEventRecordDto dto = bizEventRecordService.getRecordById(param.getId());
        return new ReturnData<>(dto);
    }

    @Operation(summary = "变更日志：列表", operationId = "logsByChangeList", method = "POST")
    @PostMapping("/change/list")
    public ReturnData<PageResult<LogBusinessChangeDto>> logsByChangeList(@RequestBody LogsBusinessSearchParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("logsByChangeList param: {}", JsonUtil.serialize(param));
        }
        param.setInstituteId(CurrentUser.getInfo().getInstituteId());
        PageResult<LogBusinessChangeDto> list = logsBusinessService.listChangeLogs(param);
        return new ReturnData<>(list);
    }

    @Operation(summary = "访问日志：列表", operationId = "logsByAccessList", method = "POST")
    @PostMapping("/access/list")
    public ReturnData<PageResult<LogBusinessAccessDto>> logsByAccessList(@RequestBody LogsBusinessSearchParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("logsByAccessList param: {}", JsonUtil.serialize(param));
        }
        param.setInstituteId(CurrentUser.getInfo().getInstituteId());
        PageResult<LogBusinessAccessDto> list = logsBusinessService.listAccessLogs(param);
        return new ReturnData<>(list);
    }
}
