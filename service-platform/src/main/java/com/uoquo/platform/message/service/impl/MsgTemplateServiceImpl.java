package com.uoquo.platform.message.service.impl;

import com.uoquo.cloud.events.RemoteEvent;
import com.uoquo.platform.common.BusinessOperationEnum;
import com.uoquo.platform.common.BusinessTypeEnum;
import com.uoquo.platform.common.DictionaryCodeEnum;
import com.uoquo.platform.common.exception.MessageReturnCode;
import com.uoquo.platform.message.mapper.MsgTemplateMapper;
import com.uoquo.platform.message.model.dto.MsgTemplateDto;
import com.uoquo.platform.message.model.param.MsgTemplateInfoParam;
import com.uoquo.platform.message.model.param.MsgTemplateListParam;
import com.uoquo.platform.message.model.param.MsgTemplateStatusParam;
import com.uoquo.platform.message.model.pojo.MsgTemplate;
import com.uoquo.platform.message.service.MsgTemplateService;
import com.uoquo.utils.CurrentUser;
import com.uoquo.utils.IDGenerator;
import com.uoquo.utils.StringUtil;
import com.uoquo.platform.common.BaseConstant;
import com.uoquo.web.BaseReturnCode;
import com.uoquo.web.SystemReturnCode;
import com.uoquo.web.events.UoquoEventPublisher;
import com.uoquo.web.exception.ResourceNotFoundException;
import com.uoquo.web.exception.UoquoException;
import com.uoquo.web.mybatis.page.PageHelper;
import com.uoquo.mybatis.page.PageList;
import com.uoquo.mybatis.page.PageResult;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MsgTemplateServiceImpl implements MsgTemplateService {

    @Autowired
    private UoquoEventPublisher eventPublisher;

    @Autowired
    private MsgTemplateMapper msgTemplateMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String addTemplate(MsgTemplateInfoParam param) {
        // 1. 判断code是否重复
        MsgTemplate old = msgTemplateMapper.selectByCode(param.getTemplateCode());
        if (old != null) {
            throw new UoquoException(MessageReturnCode.TEMPLATE_CODE_EXIST);
        }
        // 若设为了默认，则需要取消其他同类型的默认模板
        if (param.getDefaulted() != null && param.getDefaulted()) {
            msgTemplateMapper.updateClearDefault(param.getMessageType(), param.getPushWay());
        } else {
            param.setDefaulted(false);
        }
        // 2. 拼接数据
        MsgTemplate obj = new MsgTemplate();
        BeanUtils.copyProperties(param, obj);
        obj.setId(IDGenerator.getNextULID());
        // 状态
        obj.setStatus(DictionaryCodeEnum.STATE_NORMAL.getCode());
        obj.setStatusTime(new Date());
        // 操作人
        CurrentUser.UserInfo loginUser = CurrentUser.getInfo();
        obj.setCreateUser(loginUser.getUserId());
        obj.setCreateTime(new Date());
        obj.setUpdateUser(loginUser.getUserId());
        obj.setUpdateTime(new Date());
        obj.setDeleteState(BaseConstant.NOT_DELETED);
        msgTemplateMapper.insert(obj);
        // 3. 发布事件（新增）
        this.publishEvent(BusinessOperationEnum.CREATE, SystemReturnCode.SUCCESS, null, obj);
        return obj.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTemplate(MsgTemplateInfoParam param) {
        // 1. 基础校验
        MsgTemplate old = msgTemplateMapper.selectByPrimaryKey(param.getId());
        if (old == null) {
            throw new ResourceNotFoundException("模板信息不存在");
        }
        // 若设为了默认，则需要取消其他同类型的默认模板
        if (param.getDefaulted() != null && param.getDefaulted()) {
            msgTemplateMapper.updateClearDefault(param.getMessageType(), param.getPushWay());
        }
        // 2. 保存修改
        MsgTemplate obj = new MsgTemplate();
        BeanUtils.copyProperties(param, obj);
        // 修改人
        CurrentUser.UserInfo loginUser = CurrentUser.getInfo();
        obj.setUpdateUser(loginUser.getUserId());
        obj.setUpdateTime(new Date());
        msgTemplateMapper.updateByPrimaryKey(obj);
        // 3. 发布事件（修改）
        MsgTemplate info = msgTemplateMapper.selectByPrimaryKey(param.getId());
        this.publishEvent(BusinessOperationEnum.UPDATE, SystemReturnCode.SUCCESS, old, info);
    }

    @Override
    public void updateState(MsgTemplateStatusParam param) {
        // 1. 基础校验
        MsgTemplate old = msgTemplateMapper.selectByPrimaryKey(param.getId());
        if (old == null) {
            throw new ResourceNotFoundException("模板信息不存在");
        }
        // 2. 修改状态
        MsgTemplate obj = new MsgTemplate();
        obj.setId(param.getId());
        obj.setStatus(param.getStatus());
        obj.setStatusTime(new Date());
        obj.setStatusMemo(param.getStatusMemo());
        // 修改人
        CurrentUser.UserInfo loginUser = CurrentUser.getInfo();
        obj.setUpdateUser(loginUser.getUserId());
        obj.setUpdateTime(new Date());
        msgTemplateMapper.updateStatus(obj);
        // 3. 发布事件（修改状态）
        MsgTemplate info = msgTemplateMapper.selectByPrimaryKey(param.getId());
        if (DictionaryCodeEnum.STATE_NORMAL.getCode().equals(param.getStatus())) {
            this.publishEvent(BusinessOperationEnum.ENABLE, SystemReturnCode.SUCCESS, old, info);
        } else if (DictionaryCodeEnum.STATE_DISABLE.getCode().equals(param.getStatus())) {
            this.publishEvent(BusinessOperationEnum.DISABLE, SystemReturnCode.SUCCESS, old, info);
        } else {
            this.publishEvent(BusinessOperationEnum.CHANGE_STATUS, SystemReturnCode.SUCCESS, old, info);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDefault(String id) {
        // 1. 基础校验
        MsgTemplate old = msgTemplateMapper.selectByPrimaryKey(id);
        if (old == null) {
            throw new ResourceNotFoundException("模板信息不存在");
        }
        // 2. 先取消同类型的默认（包括当前记录）
        msgTemplateMapper.updateClearDefault(old.getMessageType(), old.getPushWay());
        // 2.1 若旧记录已经是默认，则说明本次操作是取消默认，不需要后续处理
        if (old.getDefaulted()) {
            // 发布事件（取消默认）
            MsgTemplate info = msgTemplateMapper.selectByPrimaryKey(id);
            this.publishEvent(BusinessOperationEnum.UNSET_DEFAULT, SystemReturnCode.SUCCESS, old, info);
            return;
        }
        // 2.2 设置为默认
        MsgTemplate obj = new MsgTemplate();
        obj.setId(old.getId());
        // 修改人
        CurrentUser.UserInfo loginUser = CurrentUser.getInfo();
        obj.setUpdateUser(loginUser.getUserId());
        obj.setUpdateTime(new Date());
        msgTemplateMapper.updateToDefault(obj);
        // 3. 发布事件（设为默认）
        MsgTemplate info = msgTemplateMapper.selectByPrimaryKey(id);
        this.publishEvent(BusinessOperationEnum.SET_DEFAULT, SystemReturnCode.SUCCESS, old, info);
    }

    @Override
    public void deleteTemplate(String id) {
        // 1. 基础校验
        MsgTemplate old = msgTemplateMapper.selectByPrimaryKey(id);
        if (old == null) {
            throw new ResourceNotFoundException("模板信息不存在");
        }
        // 2. 删除（仅逻辑删除，防止有其他关联数据查询报错）
        msgTemplateMapper.deleteByPrimaryKey(id, System.currentTimeMillis());
        // 3. 发布事件（删除）
        this.publishEvent(BusinessOperationEnum.DELETE, SystemReturnCode.SUCCESS, old, null);
    }

    @Override
    public PageResult<MsgTemplateDto> listTemplateByPage(MsgTemplateListParam param) {
        // 分页查询
        Map<String, Object> paramMap = new HashMap<>();
        if (StringUtil.notNull(param.getTemplateCode())) {
            paramMap.put("templateCode", param.getTemplateCode());
        }
        if (StringUtil.notNull(param.getTemplateName())) {
            paramMap.put("templateName", param.getTemplateName());
        }
        if (StringUtil.notNull(param.getMessageType())) {
            paramMap.put("messageType", param.getMessageType());
        }
        if (StringUtil.notNull(param.getPushWay())) {
            paramMap.put("pushWay", param.getPushWay());
        }
        if (param.getDefaulted() != null) {
            paramMap.put("defaulted", param.getDefaulted());
        }
        if (StringUtil.notNull(param.getStatus())) {
            paramMap.put("status", param.getStatus());
        }
        if (param.getCreateTimeStart() != null) {
            paramMap.put("createTimeStart", param.getCreateTimeStart());
            paramMap.put("createTimeEnd", param.getCreateTimeEnd() == null ? new Date() : param.getCreateTimeEnd());
        }
        PageHelper.startPage(param.getPageNum(), param.getPageSize());
        PageList<MsgTemplate> list = (PageList<MsgTemplate>) msgTemplateMapper.listBySearch(paramMap);
        // 2. 对象转换
        List<MsgTemplateDto> resultList = new ArrayList<>();
        for (MsgTemplate item : list.getResult()) {
            resultList.add(convert2Dto(item));
        }
        // 3. 返回结果
        return PageResult.of(list, resultList);
    }

    @Override
    public List<MsgTemplateDto> listTemplateByType(String messageType, String pushWay) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("messageType", messageType);
        paramMap.put("pushWay", pushWay);
        List<MsgTemplate> list = msgTemplateMapper.listBySearch(paramMap);
        return list.stream().map(this::convert2Dto).collect(Collectors.toList());
    }

    @Override
    public MsgTemplateDto getTemplateById(String id) {
        MsgTemplate info = msgTemplateMapper.selectByPrimaryKey(id);
        if (info == null) {
            throw new ResourceNotFoundException("模板信息不存在");
        }
        return convert2Dto(info);
    }

    @Override
    public MsgTemplateDto getTemplateByCode(String code) {
        MsgTemplate info = msgTemplateMapper.selectByCode(code);
        if (info == null) {
            throw new ResourceNotFoundException("模板信息不存在");
        }
        return convert2Dto(info);
    }

    @Override
    public MsgTemplateDto getTemplateByDefault(String messageType, String pushWay) {
        MsgTemplate info = msgTemplateMapper.selectByDefault(messageType, pushWay);
        if (info == null) {
            throw new ResourceNotFoundException("模板信息不存在");
        }
        return convert2Dto(info);
    }

    private MsgTemplateDto convert2Dto(MsgTemplate template) {
        MsgTemplateDto dto = new MsgTemplateDto();
        BeanUtils.copyProperties(template, dto);
        // TODO 这里要注意，变量的转换（List属性是否可以复制）
        return dto;
    }

    /**
     * 发布消息模板事件
     */
    private void publishEvent(BusinessOperationEnum type, BaseReturnCode status, MsgTemplate oldInfo, MsgTemplate newInfo) {
        RemoteEvent<MsgTemplate> event = new RemoteEvent<>(BusinessTypeEnum.MESSAGE.getCode(), type.getCode(), status.getCode());
        event.setBusinessSubType("TEMPLATE");
        event.setOldData(oldInfo);
        event.setNewData(newInfo);
        // 补充业务信息
        MsgTemplate info = (newInfo == null) ? oldInfo : newInfo;
        if (info != null) {
            event.setBusinessId(info.getId());
            // 消息模板都是全局的，所以所属机构为空
        }
        event.setRemarks(status.getText());
        eventPublisher.publishEvent(event);
    }
}
