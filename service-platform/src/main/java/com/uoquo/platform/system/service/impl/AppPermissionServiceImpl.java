package com.uoquo.platform.system.service.impl;

import com.uoquo.cloud.events.RemoteEvent;
import com.uoquo.platform.common.BusinessOperationEnum;
import com.uoquo.platform.common.BusinessTypeEnum;
import com.uoquo.platform.common.DictionaryCodeEnum;
import com.uoquo.platform.role.mapper.ResourceInfoMapper;
import com.uoquo.platform.role.model.pojo.ResourceInfo;
import com.uoquo.platform.system.mapper.AppInfoMapper;
import com.uoquo.platform.system.mapper.AppInheritMapper;
import com.uoquo.platform.system.mapper.AppPermissionMapper;
import com.uoquo.platform.system.model.param.AppInhertAddParam;
import com.uoquo.platform.system.model.param.AppPermissionAddParam;
import com.uoquo.platform.system.model.pojo.AppInfo;
import com.uoquo.platform.system.model.pojo.AppInherit;
import com.uoquo.platform.system.model.pojo.AppPermission;
import com.uoquo.platform.system.service.AppPermissionService;
import com.uoquo.utils.CurrentUser;
import com.uoquo.utils.IDGenerator;
import com.uoquo.web.BaseReturnCode;
import com.uoquo.web.SystemReturnCode;
import com.uoquo.web.events.UoquoEventPublisher;
import com.uoquo.web.exception.ForbiddenException;
import com.uoquo.web.exception.ParamErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AppPermissionServiceImpl implements AppPermissionService {
    Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private AppInfoMapper appInfoMapper;

    @Autowired
    private AppPermissionMapper appPermissionMapper;

    @Autowired
    private AppInheritMapper appInheritMapper;

    @Autowired
    private ResourceInfoMapper resourceInfoMapper;

    @Autowired
    private UoquoEventPublisher eventPublisher;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void copyAppPermission(String fromAppId, String toAppId) {
        // 1. 基础判断
        AppInfo fromApp = appInfoMapper.selectByPrimaryKey(fromAppId);
        if (fromApp == null) {
            throw new ParamErrorException(String.format("AppId[%s]不存在", fromAppId));
        }
        AppInfo toApp = appInfoMapper.selectByPrimaryKey(toAppId);
        if (toApp == null) {
            throw new ParamErrorException(String.format("AppId[%s]不存在", toAppId));
        } else if (DictionaryCodeEnum.TEMPLATE_TYPE_INNER.getCode().equals(toApp.getTemplateType())) {
            throw new ForbiddenException("无权修改内置模板");
        } else if (DictionaryCodeEnum.TEMPLATE_TYPE_SYSTEM.getCode().equals(toApp.getTemplateType())) {
            throw new ForbiddenException("无权修改系统模板");
        }
        // 2. 复制模板授权
        List<AppInherit> inherits = appInheritMapper.selectByAppId(fromAppId);
        if (!inherits.isEmpty()) {
            List<AppInherit> inheritList = new ArrayList<>();
            for (AppInherit item : inherits) {
                AppInherit info = new AppInherit();
                info.setId(IDGenerator.getNextULID());
                info.setAppId(toAppId);
                info.setParentId(item.getParentId());
                info.setCreateTime(new Date());
                info.setCreateUser(CurrentUser.getInfo().getUserId());
                inheritList.add(info);
            }
            appInheritMapper.batchInsert(inheritList);
        }
        // 3. 复制个性授权
        List<AppPermission> permissions = appPermissionMapper.selectByAppId(fromAppId);
        if (!permissions.isEmpty()) {
            List<AppPermission> permissionList = new ArrayList<>();
            for (AppPermission item : permissions) {
                AppPermission info = new AppPermission();
                info.setId(IDGenerator.getNextULID());
                info.setAppId(toAppId);
                info.setResourceId(item.getResourceId());
                permissionList.add(info);
            }
            appPermissionMapper.batchInsert(permissionList);
        }
        // 4. 事件发布（新增授权）
        Map<String, List<String>> map = new HashMap<>();
        map.put("addInherit", inherits.stream().map(AppInherit::getParentId).collect(Collectors.toList()));
        map.put("addResource", permissions.stream().map(AppPermission::getResourceId).collect(Collectors.toList()));
        this.publishEvent(BusinessOperationEnum.ADD_RELATION, SystemReturnCode.SUCCESS, toApp, map);
    }

    @Override
    public String addAppResourcePermission(String appId, String resourceId) {
        // 1. 基础校验
        AppInfo toApp = appInfoMapper.selectByPrimaryKey(appId);
        if (toApp == null) {
            throw new ParamErrorException(String.format("AppId[%s]不存在", appId));
        }
        // 2. 保存授权
        AppPermission info = new AppPermission();
        info.setId(IDGenerator.getNextULID());
        info.setAppId(appId);
        info.setResourceId(resourceId);
        appPermissionMapper.insert(info);
        // 3. 事件发布（新增授权）
        Map<String, List<String>> map = new HashMap<>();
        map.put("addResource", List.of(resourceId));
        this.publishEvent(BusinessOperationEnum.ADD_RELATION, SystemReturnCode.SUCCESS, toApp, map);
        return info.getId();
    }

    @Override
    public int batchInsertPermission(AppPermissionAddParam param) {
        // 1. 基础校验
        AppInfo toApp = appInfoMapper.selectByPrimaryKey(param.getAppId());
        if (toApp == null) {
            throw new ParamErrorException(String.format("AppId[%s]不存在", param.getAppId()));
        }
        // 2. 保存授权
        List<AppPermission> list = new ArrayList<>();
        for (String resourceId : param.getResourceIdList()) {
            AppPermission info = new AppPermission();
            info.setId(IDGenerator.getNextULID());
            info.setAppId(toApp.getId());
            info.setResourceId(resourceId);
            list.add(info);
        }
        int rows = appPermissionMapper.batchInsert(list);
        // 不刷新缓存，由调用方刷新（或手动刷新）
        // 3. 事件发布（新增授权）
        Map<String, List<String>> map = new HashMap<>();
        map.put("addResource", param.getResourceIdList());
        this.publishEvent(BusinessOperationEnum.ADD_RELATION, SystemReturnCode.SUCCESS, toApp, map);
        return rows;
    }

    @Override
    public AppPermission deletePermissionByPrimaryKey(String relateId) {
        // 1. 基础校验
        AppPermission info = appPermissionMapper.selectByPrimaryKey(relateId);
        if (info == null) {
            throw new ParamErrorException("非法入参");
        }
        AppInfo app = appInfoMapper.selectByPermissionRelateId(relateId);
        if (app == null) {
            throw new ParamErrorException("对应的APP不存在");
        }
        if (logger.isDebugEnabled()) {
            ResourceInfo resource = resourceInfoMapper.selectByAppPermissionRelateId(relateId);
            logger.info("删除应用[{}的授权[{}]", app.getAppkey(), resource.getResourceUrl());
        }
        // 2. 删除授权
        int rows = appPermissionMapper.deleteByPrimaryKey(relateId);
        // 不刷新缓存，由调用方刷新（或手动刷新）
        // 3. 事件发布（新增授权）
        Map<String, List<String>> map = new HashMap<>();
        map.put("delResource", List.of(info.getResourceId()));
        this.publishEvent(BusinessOperationEnum.DEL_RELATION, SystemReturnCode.SUCCESS, app, map);
        return rows > 0 ? info : null;
    }

    @Override
    public int batchInsertInherit(AppInhertAddParam param) {
        // 1. 基础校验
        AppInfo toApp = appInfoMapper.selectByPrimaryKey(param.getAppId());
        if (toApp == null) {
            throw new ParamErrorException(String.format("AppId[%s]不存在", param.getAppId()));
        }
        // 2. 保存授权
        List<AppInherit> list = new ArrayList<>();
        for (String parentId : param.getParentIdList()) {
            // 只有能继承模板
            AppInfo parent = appInfoMapper.selectByPrimaryKey(parentId);
            if (parent == null) {
                throw new ParamErrorException(String.format("AppId[%s]不存在", parentId));
            } else if (DictionaryCodeEnum.TEMPLATE_TYPE_NONE.getCode().equals(parent.getTemplateType())) {
                throw new ParamErrorException(String.format("授权[%s]不是模板应用", parent.getAppName()));
            }
            // 组装数据
            AppInherit info = new AppInherit();
            info.setId(IDGenerator.getNextULID());
            info.setAppId(param.getAppId());
            info.setParentId(parentId);
            info.setCreateTime(new Date());
            info.setCreateUser(CurrentUser.getInfo().getUserId());
            list.add(info);
        }
        // 不刷新缓存，由调用方刷新（或手动刷新）
        // 3. 事件发布（新增授权）
        Map<String, List<String>> map = new HashMap<>();
        map.put("addInherit", param.getParentIdList());
        this.publishEvent(BusinessOperationEnum.ADD_RELATION, SystemReturnCode.SUCCESS, toApp, map);
        return appInheritMapper.batchInsert(list);
    }

    @Override
    public AppInherit deleteInheritByPrimaryKey(String relateId) {
        // 1. 基础校验
        AppInherit info = appInheritMapper.selectByPrimaryKey(relateId);
        if (info == null) {
            throw new ParamErrorException("非法入参");
        }
        AppInfo app = appInfoMapper.selectByInheritRelateId(relateId);
        if (app == null) {
            throw new ParamErrorException("对应的APP不存在");
        }
        if (logger.isDebugEnabled()) {
            AppInfo parent = appInfoMapper.selectByPrimaryKey(info.getParentId());
            logger.info("删除应用[{}的继承[{}]", app.getAppkey(), parent.getAppkey());
        }
        // 2. 删除授权
        int rows = appInheritMapper.deleteByPrimaryKey(relateId);
        // 不刷新缓存，由调用方刷新（或手动刷新）
        // 3. 事件发布（新增授权）
        Map<String, List<String>> map = new HashMap<>();
        map.put("delInherit", List.of(info.getParentId()));
        this.publishEvent(BusinessOperationEnum.DEL_RELATION, SystemReturnCode.SUCCESS, app, map);
        return rows > 0 ? info : null;
    }

    private void publishEvent(BusinessOperationEnum type, BaseReturnCode status, AppInfo info, Map<String, List<String>> map) {
        RemoteEvent<AppInfo> event = new RemoteEvent<>(BusinessTypeEnum.ACCOUNT.getCode(), type.getCode(), status.getCode());
        event.setBusinessSubType("APP");
        // 因为只是变更授权关系，数据没有变化，所以使用旧数据
        event.setOldData(info);
        event.setNewData(info);
        // 补充业务信息
        event.setBusinessId(info.getId());
        event.setBusinessInstituteId(info.getInstituteId());
        event.setRemarks(status.getText());
        event.setExtension(map);
        eventPublisher.publishEvent(event);
    }
}
