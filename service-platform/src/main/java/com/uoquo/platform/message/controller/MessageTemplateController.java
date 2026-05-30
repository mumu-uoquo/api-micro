package com.uoquo.platform.message.controller;

import com.uoquo.platform.message.model.dto.MsgTemplateDto;
import com.uoquo.platform.message.model.param.MsgTemplateInfoParam;
import com.uoquo.platform.message.model.param.MsgTemplateListParam;
import com.uoquo.platform.message.model.param.MsgTemplateStatusParam;
import com.uoquo.platform.message.service.MsgTemplateService;
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

import jakarta.validation.Valid;
import java.util.List;

@Tag(name = "message", description = "消息管理")
@Validated
@RestController
@RequestMapping("/v1/message/template")
public class MessageTemplateController {
    final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private MsgTemplateService msgTemplateService;

    @Operation(summary = "消息模板：新增", operationId = "addTemplateInfo", method = "POST")
    @PostMapping("/add")
    public ReturnData<String> addTemplateInfo(@RequestBody @Valid MsgTemplateInfoParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("addTemplateInfo param: {}", JsonUtil.serialize(param));
        }
        // 基本校验
        if (StringUtil.isNull(param.getTemplateCode())) {
            throw new ParamEmtpyException("编码必须传");
        }
        msgTemplateService.addTemplate(param);
        return new ReturnData<>();
    }

    @Operation(summary = "消息模板：修改", operationId = "updateTemplateInfo", method = "POST")
    @PostMapping("/update")
    public ReturnData<String> updateTemplateInfo(@RequestBody @Valid MsgTemplateInfoParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("updateTemplateInfo param: {}", JsonUtil.serialize(param));
        }
        // 基本校验
        if (StringUtil.isNull(param.getId())) {
            throw new ParamEmtpyException("id必须传");
        }
        msgTemplateService.updateTemplate(param);
        return new ReturnData<>();
    }

    @Operation(summary = "消息模板：更新状态", operationId = "updateTemplateState", method = "POST")
    @PostMapping("/update/status")
    public ReturnData<String> updateTemplateState(@RequestBody @Valid MsgTemplateStatusParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("updateTemplateState param: {}", JsonUtil.serialize(param));
        }
        msgTemplateService.updateState(param);
        return new ReturnData<>();
    }

    @Operation(summary = "消息模板：更新默认模板", operationId = "updateTemplate2Default", method = "POST",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "模板ID", required = true)
    )
    @PostMapping("/update/defaulted")
    public ReturnData<String> updateTemplate2Default(@RequestBody @Valid IdParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("updateTemplate2Default param: {}", JsonUtil.serialize(param));
        }
        msgTemplateService.updateDefault(param.getId());
        return new ReturnData<>();
    }

    @Operation(summary = "消息模板：删除", operationId = "deleteTemplateInfo", method = "POST",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "模板ID", required = true)
    )
    @PostMapping("/delete")
    public ReturnData<String> deleteTemplateInfo(@RequestBody @Valid IdParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("deleteTemplateInfo param: {}", JsonUtil.serialize(param));
        }
        msgTemplateService.deleteTemplate(param.getId());
        return new ReturnData<>();
    }

    @Operation(summary = "消息模板：详情查询", operationId = "getTemplateInfo", method = "POST",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "模板ID", required = true)
    )
    @PostMapping("/info")
    public ReturnData<MsgTemplateDto> getTemplateInfo(@RequestBody @Valid IdParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("getTemplateInfo param: {}", JsonUtil.serialize(param));
        }
        MsgTemplateDto dto = msgTemplateService.getTemplateById(param.getId());
        return new ReturnData<>(dto);
    }

    @Operation(summary = "消息模板：列表查询", operationId = "listTemplateByPage", method = "POST")
    @PostMapping("/list/page")
    public ReturnData<PageResult<MsgTemplateDto>> listTemplateByPage(@RequestBody MsgTemplateListParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("listTemplateByPage param: {}", JsonUtil.serialize(param));
        }
        PageResult<MsgTemplateDto> result = msgTemplateService.listTemplateByPage(param);
        return new ReturnData<>(result);
    }

    @Operation(summary = "消息模板：按类型查询", operationId = "listTemplateByType", method = "POST")
    @PostMapping("/list/type")
    public ReturnData<List<MsgTemplateDto>> listTemplateByType(@RequestBody MsgTemplateListParam param) {
        if (logger.isInfoEnabled()) {
            logger.info("listTemplateByType param: {}", JsonUtil.serialize(param));
        }
        List<MsgTemplateDto> result = msgTemplateService.listTemplateByType(param.getMessageType(), param.getPushWay());
        return new ReturnData<>(result);
    }

}
