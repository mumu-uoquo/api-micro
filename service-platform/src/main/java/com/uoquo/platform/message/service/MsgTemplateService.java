package com.uoquo.platform.message.service;

import com.uoquo.platform.message.model.dto.MsgTemplateDto;
import com.uoquo.platform.message.model.param.MsgTemplateInfoParam;
import com.uoquo.platform.message.model.param.MsgTemplateListParam;
import com.uoquo.platform.message.model.param.MsgTemplateStatusParam;
import com.uoquo.mybatis.page.PageResult;

import java.util.List;

/**
 * 消息模板管理
 */
public interface MsgTemplateService {

    /**
     * 新增消息模板
     */
    String addTemplate(MsgTemplateInfoParam param);

    /**
     * 修改消息模板
     */
    void updateTemplate(MsgTemplateInfoParam param);

    /**
     * 修改状态
     */
    void updateState(MsgTemplateStatusParam param);

    /**
     * 设置/取消默认模板
     */
    void updateDefault(String id);

    /**
     * 删除消息模板
     */
    void deleteTemplate(String id);

    /**
     * 获取消息模板列表
     */
    PageResult<MsgTemplateDto> listTemplateByPage(MsgTemplateListParam param);

    /**
     * 获取消息模板列表
     */
    List<MsgTemplateDto> listTemplateByType(String messageType, String pushWay);

    /**
     * 获取消息模板详情
     */
    MsgTemplateDto getTemplateById(String id);

    /**
     * 获取消息模板详情
     */
    MsgTemplateDto getTemplateByCode(String code);

    /**
     * 获取默认的消息模板
     */
    MsgTemplateDto getTemplateByDefault(String messageType, String pushWay);

}
