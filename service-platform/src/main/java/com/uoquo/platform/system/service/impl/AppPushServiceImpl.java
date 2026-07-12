package com.uoquo.platform.system.service.impl;

import com.uoquo.platform.common.BaseConstant;
import com.uoquo.platform.common.DictionaryCodeEnum;
import com.uoquo.platform.system.mapper.AppPushMapper;
import com.uoquo.platform.system.model.dto.AppPushDto;
import com.uoquo.platform.system.model.param.AppPushParam;
import com.uoquo.platform.system.model.param.AppPushStateParam;
import com.uoquo.platform.system.model.pojo.AppPush;
import com.uoquo.platform.system.service.AppPushService;
import com.uoquo.utils.CurrentUser;
import com.uoquo.utils.IDGenerator;
import com.uoquo.web.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class AppPushServiceImpl implements AppPushService {

    final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private AppPushMapper appPushMapper;

    @Override
    public AppPushDto addAppPush(AppPushParam param) {
        AppPush push = new AppPush();
        BeanUtils.copyProperties(param, push);
        push.setId(IDGenerator.getNextULID());
        push.setStatus(DictionaryCodeEnum.STATE_NORMAL.getCode());
        push.setStatusTime(new Date());
        push.setCreateUser(CurrentUser.getInfo().getUserId());
        push.setCreateTime(new Date());
        push.setDeleteState(BaseConstant.NOT_DELETED);
        appPushMapper.insert(push);
        // 查询插入后的完整数据并返回
        AppPush saved = appPushMapper.selectByPrimaryKey(push.getId());
        AppPushDto dto = new AppPushDto();
        BeanUtils.copyProperties(saved, dto);
        return dto;
    }

    @Override
    public int updateAppPush(AppPushParam param) {
        AppPush old = appPushMapper.selectByPrimaryKey(param.getId());
        if (old == null) {
            throw new ResourceNotFoundException("推送配置不存在");
        }
        AppPush push = new AppPush();
        BeanUtils.copyProperties(param, push);
        // 更新基础信息不包含状态（由专用接口更新）
        push.setStatus(null);
        push.setUpdateUser(CurrentUser.getInfo().getUserId());
        push.setUpdateTime(new Date());
        return appPushMapper.updateByPrimaryKey(push);
    }

    @Override
    public void updateState(AppPushStateParam param) {
        AppPush old = appPushMapper.selectByPrimaryKey(param.getId());
        if (old == null) {
            throw new ResourceNotFoundException("推送配置不存在");
        }
        AppPush push = new AppPush();
        push.setId(param.getId());
        push.setStatus(param.getStatus());
        push.setStatusTime(new Date());
        push.setStatusMemo(param.getStatusMemo());
        push.setUpdateUser(CurrentUser.getInfo().getUserId());
        push.setUpdateTime(new Date());
        appPushMapper.updateByPrimaryKey(push);
    }

    @Override
    public int deleteByPrimaryKey(String id) {
        AppPush old = appPushMapper.selectByPrimaryKey(id);
        if (old == null) {
            throw new ResourceNotFoundException("推送配置不存在");
        }
        return appPushMapper.deleteByPrimaryKey(id, System.currentTimeMillis());
    }

    @Override
    public AppPushDto selectByPrimaryKey(String id) {
        AppPush push = appPushMapper.selectByPrimaryKey(id);
        if (push == null) {
            return null;
        }
        AppPushDto dto = new AppPushDto();
        BeanUtils.copyProperties(push, dto);
        return dto;
    }

    @Override
    public List<AppPushDto> listByAppId(String appId) {
        return appPushMapper.selectByAppId(appId);
    }
}
