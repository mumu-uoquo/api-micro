package com.uoquo.platform.role.service.impl;

import com.uoquo.cloud.events.RemoteEvent;
import com.uoquo.platform.common.BusinessOperationEnum;
import com.uoquo.platform.common.BusinessTypeEnum;
import com.uoquo.platform.role.mapper.ModuleResourceMapper;
import com.uoquo.platform.role.mapper.ResourceInfoMapper;
import com.uoquo.platform.role.model.dto.ResourceInfoDto;
import com.uoquo.platform.role.model.param.ResourceInfoParam;
import com.uoquo.platform.role.model.pojo.ResourceInfo;
import com.uoquo.platform.role.service.ResourceInfoService;
import com.uoquo.platform.system.mapper.AppPermissionMapper;
import com.uoquo.utils.CurrentUser;
import com.uoquo.utils.IDGenerator;
import com.uoquo.web.BaseReturnCode;
import com.uoquo.web.SystemReturnCode;
import com.uoquo.web.events.UoquoEventPublisher;
import com.uoquo.web.exception.ParamErrorException;
import com.uoquo.web.exception.ResourceNotFoundException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class ResourceInfoServiceImpl implements ResourceInfoService {

    @Autowired
    private ResourceInfoMapper resourceInfoMapper;

    @Autowired
    private ModuleResourceMapper moduleResourceMapper;

    @Autowired
    private AppPermissionMapper appPermissionMapper;

    @Autowired
    private UoquoEventPublisher eventPublisher;

    @Override
    public String addResource(ResourceInfoParam param) {
        // 1. 基本校验
        int count = resourceInfoMapper.checkUrlIsExist(null, param.getResourceUrl());
        if (count > 0) {
            throw new ParamErrorException(String.format("资源 URL[%s] 已经存在", param.getResourceUrl()));
        }
        // 2. 保存数据
        ResourceInfo info = new ResourceInfo();
        BeanUtils.copyProperties(param, info);
        info.setId(IDGenerator.getNextULID());
        info.setCreateTime(new Date());
        info.setCreateUser(CurrentUser.getInfo().getUserId());
        resourceInfoMapper.insert(info);
        // 3. 发布事件（新增资源）
        this.publishEvent(BusinessOperationEnum.CREATE, SystemReturnCode.SUCCESS, null, info);
        
        return info.getId();
    }

    @Override
    public int updateResource(ResourceInfoParam param) {
        // 1. 基本校验
        ResourceInfo old = resourceInfoMapper.selectByPrimaryKey(param.getId());
        if (old == null) {
            throw new ResourceNotFoundException("资源不存在");
        }
        int count = resourceInfoMapper.checkUrlIsExist(param.getId(), param.getResourceUrl());
        if (count > 0) {
            throw new ParamErrorException(String.format("资源 URL[%s] 已经存在", param.getResourceUrl()));
        }
        // 2. 保存数据
        ResourceInfo info = new ResourceInfo();
        BeanUtils.copyProperties(param, info);
        info.setCreateTime(new Date());
        info.setCreateUser(CurrentUser.getInfo().getUserId());
        int result = resourceInfoMapper.updateByPrimaryKey(info);
        // 3. 发布事件（更新资源）
        ResourceInfo newInfo = resourceInfoMapper.selectByPrimaryKey(param.getId());
        this.publishEvent(BusinessOperationEnum.UPDATE, SystemReturnCode.SUCCESS, old, newInfo);
        
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteResource(String id) {
        // 1. 基本校验
        ResourceInfo old = resourceInfoMapper.selectByPrimaryKey(id);
        if (old == null) {
            throw new ResourceNotFoundException("资源不存在");
        }
        // 2. 删除数据
        // 删除资源本身
        int rows = resourceInfoMapper.deleteByPrimaryKey(id);
        // 删除与模块的关联关系
        moduleResourceMapper.deleteByResourceId(id);
        // 删除与应用的关联关系
        appPermissionMapper.deleteByResourceId(id);
        // 3. 发布事件（删除资源）
        this.publishEvent(BusinessOperationEnum.DELETE, SystemReturnCode.SUCCESS, old, null);
        
        return rows;
    }

    @Override
    public List<ResourceInfoDto> listAllResource() {
        List<ResourceInfo> list = resourceInfoMapper.listBySearch(null);
        // 对象转换
        List<ResourceInfoDto> result = new ArrayList<>();
        for (ResourceInfo item : list) {
            ResourceInfoDto dto = new ResourceInfoDto();
            dto.setId(item.getId());
            dto.setResourceName(item.getResourceName());
            dto.setResourceUrl(item.getResourceUrl());
            result.add(dto);
        }
        return result;
    }

    @Override
    public List<ResourceInfoDto> listByRoleId(String roleId) {
        List<ResourceInfoDto> list = resourceInfoMapper.listByRoleId(roleId);
        return list;
    }

    @Override
    public List<ResourceInfoDto> listByAppId(String appId) {
        List<ResourceInfoDto> list = resourceInfoMapper.listByAppId(appId);
        return list;
    }

    @Override
    public List<ResourceInfoDto> listNotInApp(String appId) {
        List<ResourceInfoDto> list = resourceInfoMapper.listNotRelateAppId(appId, null);
        return list;
    }

    @Override
    public List<ResourceInfoDto> listByModuleId(String moduleId) {
        List<ResourceInfoDto> list = resourceInfoMapper.listByModuleId(moduleId);
        return list;
    }

    @Override
    public List<ResourceInfoDto> listNotInModule(String moduleId) {
        List<ResourceInfoDto> list = resourceInfoMapper.listNotRelateModuleId(moduleId, null);
        return list;
    }

    private void publishEvent(BusinessOperationEnum type, BaseReturnCode status, ResourceInfo oldInfo, ResourceInfo newInfo) {
        RemoteEvent<ResourceInfo> event = new RemoteEvent<>(BusinessTypeEnum.SYSTEM.getCode(), type.getCode(), status.getCode());
        event.setBusinessSubType("RESOURCE");
        event.setOldData(oldInfo);
        event.setNewData(newInfo);
        // 补充业务信息
        ResourceInfo info = (newInfo == null) ? oldInfo : newInfo;
        if (info != null) {
            event.setBusinessId(info.getId());
        }
        event.setRemarks(status.getText());
        eventPublisher.publishEvent(event);
    }

}
