package com.uoquo.platform.message.service.impl;

import com.uoquo.cloud.events.RemoteEvent;
import com.uoquo.platform.common.BusinessOperationEnum;
import com.uoquo.platform.common.BusinessTypeEnum;
import com.uoquo.platform.common.DictionaryCodeEnum;
import com.uoquo.platform.common.exception.MessageReturnCode;
import com.uoquo.platform.institute.model.dto.InstituteInfoDto;
import com.uoquo.platform.institute.model.param.InstituteListParam;
import com.uoquo.platform.institute.service.InstituteInfoService;
import com.uoquo.platform.message.mapper.MsgReceiverMapper;
import com.uoquo.platform.message.model.dto.MsgReceiverDto;
import com.uoquo.platform.message.model.dto.MsgReceiverSearchDto;
import com.uoquo.platform.message.model.param.MsgInfoListParam;
import com.uoquo.platform.message.model.param.MsgInfoReceiveParam;
import com.uoquo.platform.message.model.param.MsgReceiverSearchParam;
import com.uoquo.platform.message.model.pojo.MsgReceiver;
import com.uoquo.platform.message.service.MsgReceiverService;
import com.uoquo.platform.role.model.dto.RoleInfoDto;
import com.uoquo.platform.role.model.param.RoleListParam;
import com.uoquo.platform.role.service.RoleInfoService;
import com.uoquo.platform.user.model.dto.UserInfoDto;
import com.uoquo.platform.user.model.param.UserListParam;
import com.uoquo.platform.user.service.UserInfoService;
import com.uoquo.utils.CurrentUser;
import com.uoquo.utils.IDGenerator;
import com.uoquo.utils.StringUtil;
import com.uoquo.platform.common.BaseConstant;
import com.uoquo.web.BaseReturnCode;
import com.uoquo.web.SystemReturnCode;
import com.uoquo.web.events.UoquoEventPublisher;
import com.uoquo.web.exception.ForbiddenException;
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

@Service
public class MsgReceiverServiceImpl implements MsgReceiverService {

    @Autowired
    private MsgReceiverMapper msgReceiverMapper;

    @Autowired
    private RoleInfoService roleInfoService;

    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    private InstituteInfoService instituteInfoService;

    @Autowired
    private UoquoEventPublisher eventPublisher;

    @Override
    public String addReceiver(MsgInfoReceiveParam param) {
        // 1. 基本校验
        // 判断消息是否存在
        MsgReceiver old = msgReceiverMapper.selectByMessageAndReceiver(param.getMessageId(), param.getReceiverId());
        if (old != null) {
            throw new UoquoException(MessageReturnCode.RECEIVER_EXIST);
        }
        // 2. 保存
        // 构造对象
        CurrentUser.UserInfo loginUser = CurrentUser.getInfo();
        MsgReceiver info = new MsgReceiver();
        info.setId(IDGenerator.getNextULID());
        info.setMessageId(param.getMessageId());
        info.setReceiverId(param.getReceiverId());
        if (StringUtil.notNull(param.getReceiverName())) {
            info.setReceiverName(param.getReceiverName());
        } else {
            try {
                UserInfoDto userInfo = userInfoService.getUserInfo(param.getReceiverId());
                info.setReceiverName(userInfo.getUserName());
            } catch (Exception e) {
                info.setReceiverName("");
            }
        }
        info.setReadState(false);
        info.setProcessedState(false);
        info.setCreateTime(new Date());
        info.setUpdateUser(loginUser.getUserId());
        info.setUpdateTime(new Date());
        info.setDeleteState(BaseConstant.NOT_DELETED);
        msgReceiverMapper.insert(info);
        // 暂时不发送事件
        return info.getId();
    }

    @Override
    public void deleteReceiver(List<String> recordIds) {
        CurrentUser.UserInfo loginUser = CurrentUser.getInfo();
        // 1. 校验合法性
        Map<String, Object> map = new HashMap<>();
        map.put("ids", recordIds);
        List<MsgReceiver> list = msgReceiverMapper.listBySearch(map);
        for (MsgReceiver msgReceiver : list) {
            String receiverId = msgReceiver.getReceiverId();
            if (!receiverId.equals(loginUser.getUserId())) {
                throw new ForbiddenException("无权操作非自己的信息");
            }
        }
        // 2. 批量删除
        map.put("deleteState", System.currentTimeMillis());
        msgReceiverMapper.deleteByMap(map);
        // 暂时不发送事件
    }

    @Override
    public void deleteReceiver(String messageId, String receiverId) {
        // 删除
        Map<String, Object> map = new HashMap<>();
        map.put("messageId", messageId);
        map.put("receiverId", receiverId);
        map.put("deleteState", System.currentTimeMillis());
        msgReceiverMapper.deleteByMap(map);
        // 暂时不发送事件
    }

    @Override
    public void markReceiverRead(String messageId, String receiverId, String description) {
        // 1. 基本校验
        MsgReceiver old = msgReceiverMapper.selectByMessageAndReceiver(messageId, receiverId);
        if (old == null) {
            throw new ResourceNotFoundException("接收信息不存在");
        }
        // 2. 修改已读状态
        MsgReceiver info = new MsgReceiver();
        info.setReadState(true);
        info.setReadTime(new Date());
        info.setDescription(description);
        CurrentUser.UserInfo loginUser = CurrentUser.getInfo();
        info.setUpdateUser(loginUser.getUserId());
        info.setUpdateTime(new Date());
        info.setId(old.getId());
        msgReceiverMapper.updateReadStatus(info);
        // 3. 发送事件
        info = msgReceiverMapper.selectByPrimaryKey(info.getId());
        this.publishEvent(BusinessOperationEnum.VIEW, SystemReturnCode.SUCCESS, old, info, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markReceiverRead(List<String> recordIds) {
        CurrentUser.UserInfo loginUser = CurrentUser.getInfo();
        // 校验合法性
        Map<String, Object> map = new HashMap<>();
        map.put("ids", recordIds);
        List<MsgReceiver> list = msgReceiverMapper.listBySearch(map);
        for (MsgReceiver msgReceiver : list) {
            String receiverId = msgReceiver.getReceiverId();
            if (!receiverId.equals(loginUser.getUserId())) {
                throw new ForbiddenException("无权操作非自己的信息");
            }
        }
        // 更新状态（该方法调用较少，忽略性能问题）
        MsgReceiver info = new MsgReceiver();
        info.setReadState(true);
        info.setReadTime(new Date());
        info.setUpdateUser(loginUser.getUserId());
        info.setUpdateTime(new Date());
        recordIds.forEach(recordId -> {
            info.setId(recordId);
            msgReceiverMapper.updateReadStatus(info);
        });
        this.publishEvent(BusinessOperationEnum.VIEW, SystemReturnCode.SUCCESS, null, null, map);
    }

    @Override
    public void markAllUnreadReceiver() {
        MsgReceiver info = new MsgReceiver();
        info.setReadState(true);
        info.setReadTime(new Date());
        CurrentUser.UserInfo loginUser = CurrentUser.getInfo();
        info.setUpdateUser(loginUser.getUserId());
        info.setUpdateTime(new Date());
        info.setReceiverId(loginUser.getUserId());
        msgReceiverMapper.updateReadStatus(info);
    }

    @Override
    public void markReceiverProcessed(String messageId, String receiverId, String description) {
        MsgReceiver old = msgReceiverMapper.selectByMessageAndReceiver(messageId, receiverId);
        if (old == null) {
            throw new ResourceNotFoundException("接收信息不存在");
        }
        MsgReceiver info = new MsgReceiver();
        info.setProcessedState(true);
        info.setProcessedTime(new Date());
        info.setProcessedResult("");
        info.setDescription(description);
        CurrentUser.UserInfo loginUser = CurrentUser.getInfo();
        info.setUpdateUser(loginUser.getUserId());
        info.setUpdateTime(new Date());
        info.setId(old.getId());
        msgReceiverMapper.updateProcessedStatus(info);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markReceiverProcessed(List<String> recordIds) {
        CurrentUser.UserInfo loginUser = CurrentUser.getInfo();
        // 校验合法性
        Map<String, Object> map = new HashMap<>();
        map.put("ids", recordIds);
        List<MsgReceiver> list = msgReceiverMapper.listBySearch(map);
        for (MsgReceiver msgReceiver : list) {
            String receiverId = msgReceiver.getReceiverId();
            if (!receiverId.equals(loginUser.getUserId())) {
                throw new ForbiddenException("无权操作非自己的信息");
            }
        }
        // 更新状态（该方法调用较少，忽略性能问题）
        MsgReceiver info = new MsgReceiver();
        info.setProcessedState(true);
        info.setProcessedTime(new Date());
        info.setProcessedResult("");
        info.setUpdateUser(loginUser.getUserId());
        info.setUpdateTime(new Date());
        recordIds.forEach(recordId -> {
            info.setId(recordId);
            msgReceiverMapper.updateProcessedStatus(info);
        });
    }

    @Override
    public void markAllUnprocessedReceiver() {
        MsgReceiver info = new MsgReceiver();
        info.setProcessedState(true);
        info.setProcessedTime(new Date());
        info.setProcessedResult("");
        CurrentUser.UserInfo loginUser = CurrentUser.getInfo();
        info.setUpdateUser(loginUser.getUserId());
        info.setUpdateTime(new Date());
        info.setReceiverId(loginUser.getUserId());
        msgReceiverMapper.updateProcessedStatus(info);
    }

    @Override
    public PageResult<MsgReceiverDto> listReceiverByMessageId(MsgInfoListParam param) {
        if (StringUtil.isNull(param.getMessageId())) {
            return PageResult.empty();
        }
        Map<String, Object> map = new HashMap<>();
        map.put("messageId", param.getMessageId());
        PageHelper.startPage(param.getPageNum(), param.getPageSize());
        PageList<MsgReceiver> list =  (PageList<MsgReceiver>) msgReceiverMapper.listBySearch(map);
        // 对象转换
        List<MsgReceiverDto> resultList = new ArrayList<>();
        for (MsgReceiver item : list.getResult()) {
            resultList.add(this.convert2Dto(item));
        }
        return PageResult.of(list, resultList);
    }

    @Override
    public PageResult<MsgReceiverSearchDto> searchReceiverByRange(String rootInstituteId, MsgReceiverSearchParam param) {
        if (StringUtil.isNull(param.getReceiverRange()) || DictionaryCodeEnum.PUBLISH_RANGE_ALL.getCode().equals(param.getReceiverRange())) {
            return PageResult.empty();
        }
        if (DictionaryCodeEnum.PUBLISH_RANGE_ROLE.getCode().equals(param.getReceiverRange())) {
            return listReceiver4Role(rootInstituteId, param);
        } else if (DictionaryCodeEnum.PUBLISH_RANGE_USER.getCode().equals(param.getReceiverRange())) {
            return listReceiver4User(rootInstituteId, param);
        } else if (DictionaryCodeEnum.PUBLISH_RANGE_INSTITUTE.getCode().equals(param.getReceiverRange())) {
            return listReceiver4Institute(rootInstituteId, param);
        }
        return PageResult.empty();
    }

    /**
     * 查询指定机构下的角色
     */
    private PageResult<MsgReceiverSearchDto> listReceiver4Role(String rootInstituteId, MsgReceiverSearchParam param) {
        // 1. 拼接查询条件
        RoleListParam queryParam = new RoleListParam();
        queryParam.setInstituteId(param.getInstituteId());
        queryParam.setRoleName(param.getKeywords());
        // 2. 执行查询
        List<RoleInfoDto> list;
        if (StringUtil.notNull(queryParam.getInstituteId())) {
            // 指定机构时，查指定机构的角色
            list = roleInfoService.listRoleInfoByInstitute(queryParam);
        } else if (StringUtil.notNull(rootInstituteId)) {
            // 未指定机构时，普通用户只能查自己机构的角色
            queryParam.setInstituteId(rootInstituteId);
            list = roleInfoService.listRoleInfoByInstitute(queryParam);
        } else {
            // 未指定机构时，管理员只查内置的角色
            queryParam.setRoleType(DictionaryCodeEnum.ROLE_TYPE_INNER.getCode());
            PageResult<RoleInfoDto> pageResult = roleInfoService.listRoleInfoByPage(queryParam);
            list = pageResult.getResult();
        }
        // 3. 对象转换
        List<MsgReceiverSearchDto> result = new ArrayList<>();
        list.forEach(item -> {
            MsgReceiverSearchDto dto = new MsgReceiverSearchDto();
            dto.setId(item.getId());
            dto.setName(String.format("%s（%s）", item.getRoleName(), DictionaryCodeEnum.getTextByCode(item.getRoleGroup())));
            result.add(dto);
        });
        return PageResult.of(result);
    }

    /**
     * 查询指定机构下的用户
     */
    private PageResult<MsgReceiverSearchDto> listReceiver4User(String rootInstituteId, MsgReceiverSearchParam param) {
        // 1. 拼接查询条件
        UserListParam queryParam = new UserListParam();
        queryParam.setInstituteId(param.getInstituteId());
        queryParam.setUserName(param.getKeywords());
        // 2. 执行查询
        if (StringUtil.isNull(queryParam.getInstituteId())) {
            // 未指定机构时，普通用户只能查自己机构的角色
            queryParam.setInstituteId(rootInstituteId);
        }
        PageResult<UserInfoDto> pageResult = userInfoService.listUserInfo(queryParam);
        // 3. 对象转换
        List<MsgReceiverSearchDto> result = new ArrayList<>();
        pageResult.getResult().forEach(item -> {
            MsgReceiverSearchDto dto = new MsgReceiverSearchDto();
            dto.setId(item.getId());
            dto.setName(String.format("%s（%s）", item.getUserName(), StringUtil.desensitize(item.getPhone(), 3, 4)));
            result.add(dto);
        });
        return PageResult.of(result);
    }

    /**
     * 查询指定机构下的子机构
     */
    private PageResult<MsgReceiverSearchDto> listReceiver4Institute(String rootInstituteId, MsgReceiverSearchParam param) {
        // 1. 拼接查询条件
        InstituteListParam queryParam = new InstituteListParam();
        queryParam.setParentId(param.getInstituteId());
        queryParam.setInstituteName(param.getKeywords());
        // 2. 执行查询
        PageResult<InstituteInfoDto> pageResult = instituteInfoService.listInstituteInfoByPage(rootInstituteId, queryParam);
        // 3. 对象转换
        List<MsgReceiverSearchDto> result = new ArrayList<>();
        pageResult.getResult().forEach(item -> {
            MsgReceiverSearchDto dto = new MsgReceiverSearchDto();
            dto.setId(item.getId());
            dto.setName(item.getInstituteName());
            result.add(dto);
        });
        return PageResult.of(result);
    }

    private MsgReceiverDto convert2Dto(MsgReceiver info) {
        MsgReceiverDto dto = new MsgReceiverDto();
        BeanUtils.copyProperties(info, dto);
        return dto;
    }

    /**
     * 发布消息事件
     */
    private void publishEvent(BusinessOperationEnum type, BaseReturnCode status, MsgReceiver oldInfo, MsgReceiver newInfo, Map<String, Object> map) {
        RemoteEvent<MsgReceiver> event = new RemoteEvent<>(BusinessTypeEnum.MESSAGE.getCode(), type.getCode(), status.getCode());
        event.setBusinessSubType("MESSAGE");
        event.setOldData(oldInfo);
        event.setNewData(newInfo);
        // 补充业务信息
        MsgReceiver info = newInfo == null ? oldInfo : newInfo;
        if (info != null) {
            event.setBusinessId(info.getId());
        }
        // 用户阅读、处理的消息都是自己的消息，所以此处直接写死业务所属机构ID
        event.setBusinessInstituteId(CurrentUser.getInfo().getInstituteId());
        event.setRemarks(status.getText());
        event.setExtension(map);
        eventPublisher.publishEvent(event);
    }
}
