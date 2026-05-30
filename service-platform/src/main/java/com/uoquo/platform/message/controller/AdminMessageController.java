package com.uoquo.platform.message.controller;

import com.uoquo.platform.message.model.dto.MsgInfoDto;
import com.uoquo.platform.message.model.dto.MsgPushLogDto;
import com.uoquo.platform.message.model.dto.MsgReceiverDto;
import com.uoquo.platform.message.model.dto.MsgReceiverSearchDto;
import com.uoquo.platform.message.model.param.*;
import com.uoquo.platform.message.service.MsgInfoService;
import com.uoquo.platform.message.service.MsgPushLogService;
import com.uoquo.platform.message.service.MsgReceiverService;
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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

/**
 * 消息管理（超管账号）
 * @author xuhz
 */
@Tag(name = "adminMessage", description = "超管消息管理")
@Validated
@RestController
@RequestMapping("/admin/v1/message")
public class AdminMessageController {
    final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private MsgInfoService msgInfoService;

    @Autowired
    private MsgPushLogService msgPushLogService;

    @Autowired
    private MsgReceiverService msgReceiverService;

    /* *******************  消息管理 ******************* */
    @Operation(summary = "消息管理：新增消息", operationId = "addMessage", method = "POST")
    @PostMapping("/add")
    public ReturnData<String> addMessage(@RequestBody @Valid MsgInfoParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("addMessage param: {}", JsonUtil.serialize(param));
        }
        String id = msgInfoService.addMessage(param);
        return new ReturnData<>(id);
    }

    @Operation(summary = "消息管理：修改", operationId = "updateMessage", method = "POST")
    @PostMapping("/update")
    public ReturnData<String> updateMessage(@RequestBody @Valid MsgInfoParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("updateMessage param: {}", JsonUtil.serialize(param));
        }
        // 基本校验
        if (StringUtil.isNull(param.getId())) {
            throw new ParamEmtpyException("id必须传");
        }
        msgInfoService.updateMessage(param);
        return new ReturnData<>();
    }

    @Operation(summary = "消息管理：发布", operationId = "publishMessage", method = "POST")
    @PostMapping("/publish")
    public ReturnData<String> publishMessage(@RequestBody MsgInfoParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("publishMessage param: {}", JsonUtil.serialize(param));
        }
        // 基本校验
        if (StringUtil.isNull(param.getId())) {
            throw new ParamEmtpyException("id必须传");
        }
        msgInfoService.publishMessage(param);
        return new ReturnData<>();
    }

    @Operation(summary = "消息管理：撤回", operationId = "withdrawMessage", method = "POST",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "消息ID", required = true)
    )
    @PostMapping("/withdraw")
    public ReturnData<String> withdrawMessage(@RequestBody @Valid IdParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("withdrawMessage param: {}", JsonUtil.serialize(param));
        }
        msgInfoService.withdrawMessage(param.getId());
        return new ReturnData<>();
    }

    @Operation(summary = "消息管理：删除", operationId = "deleteMessage", method = "POST",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "消息ID", required = true)
    )
    @PostMapping("/delete")
    public ReturnData<String> deleteMessage(@RequestBody @Valid IdParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("deleteMessage param: {}", JsonUtil.serialize(param));
        }
        msgInfoService.deleteMessage(param.getId());
        return new ReturnData<>();
    }

    @Operation(summary = "消息管理：搜索发布目标", operationId = "searchReceiverByRange", method = "POST")
    @PostMapping("/receiver/search")
    public ReturnData<PageResult<MsgReceiverSearchDto>> searchReceiverByRange(@RequestBody MsgReceiverSearchParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("searchReceiverByRange param: {}", JsonUtil.serialize(param));
        }
        PageResult<MsgReceiverSearchDto> dto = msgReceiverService.searchReceiverByRange(null, param);
        return new ReturnData<>(dto);
    }

    @Operation(summary = "消息管理：列表查询", operationId = "listMessageInfoByPage", method = "POST")
    @PostMapping("/list/page")
    public ReturnData<PageResult<MsgInfoDto>> listMessageInfoByPage(@RequestBody MsgInfoListParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("listMessageInfoByPage param: {}", JsonUtil.serialize(param));
        }
        PageResult<MsgInfoDto> result = msgInfoService.listMessage(param);
        return new ReturnData<>(result);
    }

    /* *******************  消息详情 ******************* */
    @Operation(summary = "消息管理：详情查询", operationId = "getMessage", method = "POST",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "消息ID", required = true)
    )
    @PostMapping("/info")
    public ReturnData<MsgInfoDto> getMessage(@RequestBody @Valid IdParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("getMessage param: {}", JsonUtil.serialize(param));
        }
        MsgInfoDto dto = msgInfoService.getMessageDetail(param.getId());
        return new ReturnData<>(dto);
    }

    @Operation(summary = "消息详情：收件列表", operationId = "listReceiverByMessageId", method = "POST")
    @PostMapping("/info/receivers")
    public ReturnData<PageResult<MsgReceiverDto>> listReceiverByMessageId(@RequestBody MsgInfoListParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("listReceiverByMessageId param: {}", JsonUtil.serialize(param));
        }
        PageResult<MsgReceiverDto> dto = msgReceiverService.listReceiverByMessageId(param);
        return new ReturnData<>(dto);
    }

    @Operation(summary = "消息详情：删除接收人", operationId = "deleteReceiver4Message", method = "POST")
    @PostMapping("/info/receivers/delete")
    public ReturnData<String> deleteReceiver4Message(@RequestBody MsgInfoListParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("deleteReceiver4Message param: {}", JsonUtil.serialize(param));
        }
        // 基础校验
        if (StringUtil.isNull(param.getMessageId())) {
            throw new ParamEmtpyException("messageId");
        }
        if (StringUtil.isNull(param.getReceiverId())) {
            throw new ParamEmtpyException("receiverId");
        }
        msgReceiverService.deleteReceiver(param.getMessageId(), param.getReceiverId());
        return new ReturnData<>();
    }

    @Operation(summary = "消息详情：推送日志", operationId = "listPushLogByMessageId", method = "POST")
    @PostMapping("/info/push/logs")
    public ReturnData<PageResult<MsgPushLogDto>> listPushLogByMessageId(@RequestBody MsgInfoListParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("listPushLogByMessageId param: {}", JsonUtil.serialize(param));
        }
        PageResult<MsgPushLogDto> dto = msgPushLogService.listPushLogByMessageId(param);
        return new ReturnData<>(dto);
    }

    @Operation(summary = "消息处理：下载附件", operationId = "downloadAttachment", method = "POST",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "附件ID", required = true),
            responses = @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    description = "文件内容",
                    content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/octet-stream")
            )
    )
    @PostMapping("/attachment/download")
    public void downloadAttachment(@RequestBody @Valid IdParam param, HttpServletRequest request, HttpServletResponse response) {
        if (logger.isInfoEnabled()) {
            logger.info("downloadAttachment param: {}", JsonUtil.serialize(param));
        }
        msgInfoService.downloadAttachment(param.getId(), false, request, response);
    }
}
