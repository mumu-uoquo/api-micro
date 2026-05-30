package com.uoquo.platform.logs.controller;

import com.uoquo.platform.logs.model.dto.*;
import com.uoquo.platform.logs.model.param.*;
import com.uoquo.platform.logs.service.BizEventRecordService;
import com.uoquo.platform.logs.service.LogsBusinessService;
import com.uoquo.platform.logs.service.LogsAuthService;
import com.uoquo.platform.logs.service.OnlineUserService;
import com.uoquo.utils.json.JsonUtil;
import com.uoquo.web.ReturnData;
import com.uoquo.annotation.web.IgnoreAuth;
import com.uoquo.web.param.IdParam;
import com.uoquo.web.exception.ResourceNotFoundException;
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
 * 日志信息（管理员）
 * @author xuhz
 */
@Tag(name = "adminLogs", description = "日志记录")
@Validated
@RestController
@RequestMapping("/admin/v1/logs")
public class AdminLogsInfoController {
    final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private LogsAuthService logsAuthService;

    @Autowired
    private LogsBusinessService logsBusinessService;

    @Autowired
    private OnlineUserService onlineUserService;

    @Autowired
    private BizEventRecordService bizEventRecordService;

    /* ********************************* 新增日志（内部调用） ********************************* */
    @IgnoreAuth(inner = true)
    @Operation(summary = "日志新增：认证日志", hidden = true)
    @PostMapping("/login/save")
    public ReturnData<String> addLogs4Login(@RequestBody @Valid LogUserLoginParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("addLogs4Login param: {}", JsonUtil.serialize(param));
        }
        String id = logsAuthService.addLogsInfo(param);
        return new ReturnData<>(id);
    }

    @IgnoreAuth(inner = true)
    @Operation(summary = "日志新增：登出日志", hidden = true)
    @PostMapping("/logout/save")
    public ReturnData<String> addLogs4Logout(@RequestBody @Valid LogUserLogoutParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("addLogs4Logout param: {}", JsonUtil.serialize(param));
        }
        String id = logsAuthService.updateLogoutInfo(param);
        return new ReturnData<>(id);
    }

    @IgnoreAuth(inner = true)
    @Operation(summary = "事件记录：保存", hidden = true)
    @PostMapping("/event/save")
    public ReturnData<String> saveEventRecord(@RequestBody @Valid BizEventRecordParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("saveEventRecord param: {}", JsonUtil.serialize(param));
        }
        String id = bizEventRecordService.saveEventRecord(param);
        return new ReturnData<>(id);
    }

    /* ********************************* 在线用户 ********************************* */
    @Operation(summary = "在线用户：列表", operationId = "onlineList", method = "POST")
    @PostMapping("/online/list")
    public ReturnData<PageResult<LogUserOnlineDto>> onlineList(@RequestBody OnlineUserSearchParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("onlineList param: {}", JsonUtil.serialize(param));
        }
        PageResult<LogUserOnlineDto> list = onlineUserService.listOnlineUsers(param);
        return new ReturnData<>(list);
    }

    @Operation(summary = "在线用户：踢出", operationId = "onlineKickOut", method = "POST",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "在线日志ID", required = true)
    )
    @PostMapping("/online/kick")
    public ReturnData<Boolean> onlineKickOut(@RequestBody @Valid IdParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("onlineKickOut param: {}", JsonUtil.serialize(param));
        }
        boolean result = onlineUserService.kickOutUser(param.getId());
        if (result) {
            return new ReturnData<>(true);
        } else {
            throw new ResourceNotFoundException("在线记录不存在");
        }
    }

    /* ********************************* 事件记录（操作日志） ********************************* */
    @Operation(summary = "事件记录：分页", operationId = "listEventRecords", method = "POST")
    @PostMapping("/event/list")
    public ReturnData<PageResult<BizEventRecordDto>> listEventRecords(@RequestBody BizEventRecordSearchParam param) {
        PageResult<BizEventRecordDto> result = bizEventRecordService.listRecords(param);
        return new ReturnData<>(result);
    }

    @Operation(summary = "事件记录：详情", operationId = "getEventRecordById", method = "POST",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "事件记录ID", required = true)
    )
    @PostMapping("/event/info")
    public ReturnData<BizEventRecordDto> getEventRecordById(@RequestBody @Valid IdParam param) {
        BizEventRecordDto dto = bizEventRecordService.getRecordById(param.getId());
        return new ReturnData<>(dto);
    }

    @Operation(summary = "事件记录：重试", operationId = "retryEvent", method = "POST",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "事件记录ID", required = true)
    )
    @PostMapping("/event/retry")
    public ReturnData<String> retryEvent(@RequestBody @Valid IdParam param) {
        bizEventRecordService.retryEvent(param.getId());
        return new ReturnData<>();
    }

    @Operation(summary = "事件记录：重试列表", operationId = "listEventByRetry", method = "POST",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "事件记录ID", required = true)
    )
    @PostMapping("/event/retry/list")
    public ReturnData<List<BizEventRetryDto>> listEventByRetry(@RequestBody @Valid IdParam param) {
        List<BizEventRetryDto> list = bizEventRecordService.listByRetry(param.getId());
        return new ReturnData<>(list);
    }

    /* ********************************* 登录日志 ********************************* */
    @Operation(summary = "登录日志：列表", operationId = "logsByAuthList", method = "POST")
    @PostMapping("/auth/list")
    public ReturnData<PageResult<LogUserLoginDto>> logsByAuthList(@RequestBody LogsAuthSearchParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("logsByAuthList param: {}", JsonUtil.serialize(param));
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

    @Operation(summary = "变更日志：列表", operationId = "logsByChangeList", method = "POST")
    @PostMapping("/change/list")
    public ReturnData<PageResult<LogBusinessChangeDto>> logsByChangeList(@RequestBody LogsBusinessSearchParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("logsByChangeList param: {}", JsonUtil.serialize(param));
        }
        PageResult<LogBusinessChangeDto> list = logsBusinessService.listChangeLogs(param);
        return new ReturnData<>(list);
    }

    @Operation(summary = "访问日志：列表", operationId = "logsByOperationList", method = "POST")
    @PostMapping("/access/list")
    public ReturnData<PageResult<LogBusinessAccessDto>> logsByAccessList(@RequestBody LogsBusinessSearchParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("logsByAccessList param: {}", JsonUtil.serialize(param));
        }
        PageResult<LogBusinessAccessDto> list = logsBusinessService.listAccessLogs(param);
        return new ReturnData<>(list);
    }
}
