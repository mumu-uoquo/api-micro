package com.uoquo.platform.message.controller;

import com.uoquo.platform.common.DictionaryCodeEnum;
import com.uoquo.platform.message.model.dto.MsgInfoDto;
import com.uoquo.platform.message.model.dto.MsgPushLogDto;
import com.uoquo.platform.message.model.dto.MsgReceiverDto;
import com.uoquo.platform.message.model.dto.MsgReceiverSearchDto;
import com.uoquo.platform.message.model.param.*;
import com.uoquo.platform.message.service.MsgInfoService;
import com.uoquo.platform.message.service.MsgPushLogService;
import com.uoquo.platform.message.service.MsgReceiverService;
import com.uoquo.utils.CurrentUser;
import com.uoquo.utils.StringUtil;
import com.uoquo.utils.json.JsonUtil;
import com.uoquo.web.ReturnData;
import com.uoquo.annotation.web.IgnoreAuth;
import com.uoquo.web.param.IdParam;
import com.uoquo.web.exception.ForbiddenException;
import com.uoquo.web.exception.ParamEmtpyException;
import com.uoquo.web.exception.ParamErrorException;
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
 * 消息管理（普通管理）
 * @author xuhz
 */
@Tag(name = "message", description = "消息管理")
@Validated
@RestController
@RequestMapping("/v1/message/manage")
public class MessageManagerController {
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
        // 不允许添加系统消息
        if (DictionaryCodeEnum.MESSAGE_TYPE_SYSTEM.getCode().equals(param.getMessageType())) {
            throw new ParamErrorException("无权发布系统消息");
        }
        // 发布范围判断
        if (StringUtil.isNull(param.getReceiverRange())) {
            throw new ParamEmtpyException("发布范围不能为空");
        } else if (DictionaryCodeEnum.PUBLISH_RANGE_ALL.getCode().equals(param.getReceiverRange())) {
            throw new ParamErrorException("无权发布全员消息");
        }
        if (StringUtil.isNull(param.getReceiverIds())) {
            throw new ParamEmtpyException("发布对象不能为空");
        }
        // 保存
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
        // 只能修改自己添加的消息
        MsgInfoDto old = msgInfoService.getMessageDetail(param.getId());
        if (!old.getCreateUser().equals(CurrentUser.getInfo().getUserId())) {
            throw new ForbiddenException("只能管理自己的添加消息");
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
        // 发布范围判断
        if (StringUtil.notNull(param.getReceiverRange()) && StringUtil.isNull(param.getReceiverIds())) {
            throw new ParamEmtpyException("发布范围不为空时，发布对象不能为空");
        }
        // 只能发布自己添加的消息
        MsgInfoDto old = msgInfoService.getMessageDetail(param.getId());
        if (!old.getCreateUser().equals(CurrentUser.getInfo().getUserId())) {
            throw new ForbiddenException("只能发布自己的添加消息");
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
        // 只能撤回自己添加的消息
        MsgInfoDto old = msgInfoService.getMessageDetail(param.getId());
        if (!old.getCreateUser().equals(CurrentUser.getInfo().getUserId())) {
            throw new ForbiddenException("只能撤回自己的添加消息");
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
        // 只能删除自己添加的消息
        MsgInfoDto old = msgInfoService.getMessageDetail(param.getId());
        if (!old.getCreateUser().equals(CurrentUser.getInfo().getUserId())) {
            throw new ForbiddenException("只能删除自己的添加消息");
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
        PageResult<MsgReceiverSearchDto> dto = msgReceiverService.searchReceiverByRange(CurrentUser.getInfo().getInstituteId(), param);
        return new ReturnData<>(dto);
    }

    @Operation(summary = "消息管理：我发布的", operationId = "listMySendMessageByPage", method = "POST")
    @PostMapping("/list/sent/page")
    public ReturnData<PageResult<MsgInfoDto>> listMySendMessageByPage(@RequestBody MsgInfoListParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("listMySendMessageByPage param: {}", JsonUtil.serialize(param));
        }
        // 只能查询自己发布的消息
        param.setCreateUser(CurrentUser.getInfo().getUserId());
        PageResult<MsgInfoDto> result = msgInfoService.listMessage(param);
        return new ReturnData<>(result);
    }

    /* *******************  消息详情 ******************* */
    @Operation(summary = "消息详情：详情查询", operationId = "getMessage", method = "POST",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "消息ID", required = true)
    )
    @PostMapping("/info")
    public ReturnData<MsgInfoDto> getMessage(@RequestBody @Valid IdParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("getMessage param: {}", JsonUtil.serialize(param));
        }
        // 只能管理自己的添加消息
        MsgInfoDto info = msgInfoService.getMessageDetail(param.getId());
        if (!info.getCreateUser().equals(CurrentUser.getInfo().getUserId())) {
            throw new ForbiddenException("只能管理自己的添加消息");
        }
        return new ReturnData<>(info);
    }

    @Operation(summary = "消息详情：收件列表", operationId = "listReceiverByMessageId", method = "POST")
    @PostMapping("/info/receivers")
    public ReturnData<PageResult<MsgReceiverDto>> listReceiverByMessageId(@RequestBody MsgInfoListParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("listReceiverByMessageId param: {}", JsonUtil.serialize(param));
        }
        // 只能管理自己的添加消息
        MsgInfoDto info = msgInfoService.getMessageDetail(param.getMessageId());
        if (!info.getCreateUser().equals(CurrentUser.getInfo().getUserId())) {
            throw new ForbiddenException("只能管理自己的添加消息");
        }
        PageResult<MsgReceiverDto> dto = msgReceiverService.listReceiverByMessageId(param);
        return new ReturnData<>(dto);
    }

    @IgnoreAuth(inner = true)
    @Operation(summary = "消息详情：添加接收人（由发布事件触发）", hidden = true)
    @PostMapping("/info/receivers/add")
    public ReturnData<String> addReceiver4Message(@RequestBody @Valid  MsgInfoReceiveParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("addReceiver4Message param: {}", JsonUtil.serialize(param));
        }
        String recordId = msgReceiverService.addReceiver(param);
        return new ReturnData<>(recordId);
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
        // 只能管理自己的添加消息
        MsgInfoDto info = msgInfoService.getMessageDetail(param.getMessageId());
        if (!info.getCreateUser().equals(CurrentUser.getInfo().getUserId())) {
            throw new ForbiddenException("只能管理自己的添加消息");
        }
        msgReceiverService.deleteReceiver(param.getMessageId(), param.getReceiverId());
        return new ReturnData<>();
    }

    @Operation(summary = "消息详情：推送日志查询", operationId = "listPushLogByMessageId", method = "POST")
    @PostMapping("/info/push/logs")
    public ReturnData<PageResult<MsgPushLogDto>> listPushLogByMessageId(@RequestBody MsgInfoListParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("listPushLogByMessageId param: {}", JsonUtil.serialize(param));
        }
        // 只能管理自己的添加消息
        MsgInfoDto info = msgInfoService.getMessageDetail(param.getMessageId());
        if (!info.getCreateUser().equals(CurrentUser.getInfo().getUserId())) {
            throw new ForbiddenException("只能管理自己的添加消息");
        }
        PageResult<MsgPushLogDto> dto = msgPushLogService.listPushLogByMessageId(param);
        return new ReturnData<>(dto);
    }

    @IgnoreAuth(inner = true)
    @Operation(summary = "消息详情：推送日志新增（由发布事件触发）", hidden = true)
    @PostMapping("/info/push/logs/add")
    public ReturnData<String> addPushLog4Message(@RequestBody MsgPushLogParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("addPushLog4Message param: {}", JsonUtil.serialize(param));
        }
        String logId = msgPushLogService.addPushLog(param);
        return new ReturnData<>(logId);
    }

    @Operation(summary = "消息详情：消息重推", operationId = "retryPushByLogId", method = "POST",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "推送日志ID", required = true)
    )
    @PostMapping("/info/retry/push")
    public ReturnData<String> retryPushByLogId(@RequestBody @Valid IdParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("retryPushByLogId param: {}", JsonUtil.serialize(param));
        }
        // 查询推送日志
        MsgPushLogDto log = msgPushLogService.getPushLogDetail(param.getId());
//        // TODO 只有失败状态的日志才能重推
//        if (!log.getPushStatus().equals(MsgPushLogDto.PUSH_STATUS_FAILED)) {
//            throw new BadRequestException("推送日志状态不是失败");
//        }
        msgInfoService.retryPushMessage(log.getId(), log.getMessageId(), log.getReceiverId());
        return new ReturnData<>();
    }

}
