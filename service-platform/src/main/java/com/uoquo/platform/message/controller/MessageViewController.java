package com.uoquo.platform.message.controller;

import com.uoquo.platform.message.model.dto.MsgInfoViewDto;
import com.uoquo.platform.message.model.param.MsgInfoListParam;
import com.uoquo.platform.message.model.param.MsgInfoMarkParam;
import com.uoquo.platform.message.service.MsgInfoService;
import com.uoquo.platform.message.service.MsgReceiverService;
import com.uoquo.utils.CurrentUser;
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
import java.util.List;

/**
 * 消息处理
 * @author xuhz
 */
@Tag(name = "message", description = "消息管理")
@Validated
@RestController
@RequestMapping("/v1/message/view")
public class MessageViewController {
    final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private MsgInfoService msgInfoService;

    @Autowired
    private MsgReceiverService msgReceiverService;


    @Operation(summary = "消息处理：我收到的", operationId = "listMyReceiveMessageByPage", method = "POST")
    @PostMapping("/list/page")
    public ReturnData<PageResult<MsgInfoViewDto>> listMyReceiveMessageByPage(@RequestBody MsgInfoListParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("listMyReceiveMessageByPage param: {}", JsonUtil.serialize(param));
        }
        PageResult<MsgInfoViewDto> result = msgInfoService.listMessage4View(param);
        return new ReturnData<>(result);
    }

    @Operation(summary = "消息处理：我的未读", operationId = "listMyMessageByUnread", method = "POST")
    @PostMapping("/list/unread")
    public ReturnData<List<MsgInfoViewDto>> listMyMessageByUnread(@RequestBody MsgInfoListParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("listMyMessageByUnread param: {}", JsonUtil.serialize(param));
        }
        List<MsgInfoViewDto> result = msgInfoService.listUnreadMessage(param.getCreateTimeStart(), param.getCreateTimeEnd());
        return new ReturnData<>(result);
    }

    @Operation(summary = "消息处理：详情查看", operationId = "viewMessage", method = "POST",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "消息ID", required = true)
    )
    @PostMapping("/info")
    public ReturnData<MsgInfoViewDto> viewMessage(@RequestBody @Valid IdParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("viewMessage param: {}", JsonUtil.serialize(param));
        }
        MsgInfoViewDto dto = msgInfoService.getMessage4View(param.getId());
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
        msgInfoService.downloadAttachment(param.getId(), true, request, response);
    }

    /* *******************  消息删除 ******************* */
    @Operation(summary = "消息处理：删除（单条）", operationId = "deleteMessageOne", method = "POST")
    @PostMapping("/delete/one")
    public ReturnData<String> deleteMessageOne(@RequestBody @Valid MsgInfoMarkParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("deleteMessageOne param: {}", JsonUtil.serialize(param));
        }
        if (StringUtil.isNull(param.getMessageId())) {
            throw new ParamEmtpyException("messageId");
        }
        msgReceiverService.deleteReceiver(param.getMessageId(), CurrentUser.getInfo().getUserId());
        return new ReturnData<>();
    }

    @Operation(summary = "消息处理：删除（批量）", operationId = "deleteMessageBatch", method = "POST")
    @PostMapping("/delete/batch")
    public ReturnData<String> deleteMessageBatch(@RequestBody @Valid MsgInfoMarkParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("deleteMessageBatch param: {}", JsonUtil.serialize(param));
        }
        if (param.getRecordIds() == null || param.getRecordIds().isEmpty()) {
            throw new ParamEmtpyException("recordIds");
        }
        msgReceiverService.deleteReceiver(param.getRecordIds());
        return new ReturnData<>();
    }

    @Operation(summary = "消息处理：删除（全部）", operationId = "deleteMessageAll", method = "POST")
    @PostMapping("/delete/all")
    public ReturnData<String> deleteMessageAll() {
        msgReceiverService.deleteReceiver(null, CurrentUser.getInfo().getUserId());
        return new ReturnData<>();
    }

    /* *******************  消息已读 ******************* */
    @Operation(summary = "消息处理：标记为已读（单条）", operationId = "markMessageReadOne", method = "POST")
    @PostMapping("/read/one")
    public ReturnData<String> markMessageReadOne(@RequestBody @Valid MsgInfoMarkParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("markMessageReadOne param: {}", JsonUtil.serialize(param));
        }
        if (StringUtil.isNull(param.getMessageId())) {
            throw new ParamEmtpyException("messageId");
        }
        msgReceiverService.markReceiverRead(param.getMessageId(), CurrentUser.getInfo().getUserId(), param.getDescription());
        return new ReturnData<>();
    }

    @Operation(summary = "消息处理：标记为已读（批量）", operationId = "markMessageReadBatch", method = "POST")
    @PostMapping("/read/batch")
    public ReturnData<String> markMessageReadBatch(@RequestBody @Valid MsgInfoMarkParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("markMessageReadBatch param: {}", JsonUtil.serialize(param));
        }
        if (param.getRecordIds() == null || param.getRecordIds().isEmpty()) {
            throw new ParamEmtpyException("recordIds");
        }
        msgReceiverService.markReceiverRead(param.getRecordIds());
        return new ReturnData<>();
    }

    @Operation(summary = "消息处理：标记为已读（全部）", operationId = "markMessageReadAll", method = "POST")
    @PostMapping("/read/all")
    public ReturnData<String> markMessageReadAll() {
        msgReceiverService.markAllUnreadReceiver();
        return new ReturnData<>();
    }

    /* *******************  消息处理 ******************* */
    @Operation(summary = "消息处理：处理（单条）", operationId = "markMessageProcessOne", method = "POST")
    @PostMapping("/process/one")
    public ReturnData<String> markMessageProcessOne(@RequestBody @Valid MsgInfoMarkParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("markMessageProcessOne param: {}", JsonUtil.serialize(param));
        }
        if (StringUtil.isNull(param.getMessageId())) {
            throw new ParamEmtpyException("messageId");
        }
        msgReceiverService.markReceiverProcessed(param.getMessageId(), CurrentUser.getInfo().getUserId(), param.getDescription());
        return new ReturnData<>();
    }

    @Operation(summary = "消息处理：标记为处理（批量）", operationId = "markMessageProcessBatch", method = "POST")
    @PostMapping("/process/batch")
    public ReturnData<String> markMessageProcessBatch(@RequestBody @Valid MsgInfoMarkParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("markMessageProcessBatch param: {}", JsonUtil.serialize(param));
        }
        if (param.getRecordIds() == null || param.getRecordIds().isEmpty()) {
            throw new ParamEmtpyException("recordIds");
        }
        msgReceiverService.markReceiverProcessed(param.getRecordIds());
        return new ReturnData<>();
    }

    @Operation(summary = "消息处理：标记为处理（全部）", operationId = "markMessageProcessAll", method = "POST")
    @PostMapping("/process/all")
    public ReturnData<String> markMessageProcessAll() {
        msgReceiverService.markAllUnprocessedReceiver();
        return new ReturnData<>();
    }

}
