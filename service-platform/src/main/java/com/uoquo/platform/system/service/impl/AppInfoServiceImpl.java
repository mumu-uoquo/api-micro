package com.uoquo.platform.system.service.impl;

import com.uoquo.cloud.events.RemoteEvent;
import com.uoquo.platform.common.BusinessOperationEnum;
import com.uoquo.platform.common.BusinessTypeEnum;
import com.uoquo.platform.common.DictionaryCodeEnum;
import com.uoquo.platform.role.mapper.ModuleInfoMapper;
import com.uoquo.platform.role.mapper.ResourceInfoMapper;
import com.uoquo.platform.role.model.dto.ResourceInfoDto;
import com.uoquo.platform.role.model.pojo.ModuleInfo;
import com.uoquo.platform.system.mapper.AppInfoMapper;
import com.uoquo.platform.system.mapper.AppInheritMapper;
import com.uoquo.platform.system.model.dto.AppInfoDto;
import com.uoquo.platform.system.model.param.AppInfoListParam;
import com.uoquo.platform.system.model.param.AppInfoParam;
import com.uoquo.platform.system.model.param.AppInfoStateParam;
import com.uoquo.platform.system.model.pojo.AppInfo;
import com.uoquo.platform.system.model.pojo.AppInherit;
import com.uoquo.platform.system.service.AppInfoService;
import com.uoquo.utils.CurrentUser;
import com.uoquo.utils.IDGenerator;
import com.uoquo.utils.StringUtil;
import com.uoquo.platform.common.BaseConstant;
import com.uoquo.web.BaseReturnCode;
import com.uoquo.web.SystemReturnCode;
import com.uoquo.web.events.UoquoEventPublisher;
import com.uoquo.web.BaseCacheKey;
import com.uoquo.web.exception.ForbiddenException;
import com.uoquo.web.exception.ResourceNotFoundException;
import com.uoquo.web.exception.SystemErrorException;
import com.uoquo.utils.spring.RedisUtil;
import com.uoquo.web.mybatis.page.PageHelper;
import com.uoquo.mybatis.page.PageList;
import com.uoquo.mybatis.page.PageResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AppInfoServiceImpl implements AppInfoService {
    final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private AppInfoMapper appInfoMapper;

    @Autowired
    private AppInheritMapper appInheritMapper;

    @Autowired
    private ResourceInfoMapper resourceInfoMapper;

    @Autowired
    private ModuleInfoMapper moduleInfoMapper;

    @Autowired
    private UoquoEventPublisher eventPublisher;

    @Override
    public AppInfoDto addAppInfo(AppInfoParam param) {
        // 1. 基础判断 APPKEY 不能为空，secret不能为空
        // 只要不是普通模板，都设置为非模板
        if (StringUtil.isNull(param.getTemplateType())
                || !DictionaryCodeEnum.TEMPLATE_TYPE_NORMAL.getCode().equals(param.getTemplateType())) {
            param.setTemplateType(DictionaryCodeEnum.TEMPLATE_TYPE_NONE.getCode());
        }
        // 2. 保存
        AppInfo info = new AppInfo();
        BeanUtils.copyProperties(param, info);
        info.setId(IDGenerator.getNextULID());
        info.setStatus(DictionaryCodeEnum.STATE_NORMAL.getCode());
        info.setStatusTime(new Date());
        info.setCreateUser(CurrentUser.getInfo().getUserId());
        info.setCreateTime(new Date());
        info.setDeleteState(BaseConstant.NOT_DELETED);
        appInfoMapper.insert(info);
        // 3. 缓存应用信息
        info = appInfoMapper.selectByPrimaryKey(info.getId());
        flushAppInfoCache(info);
        // 4. 发送事件（新增)
        this.publishEvent(BusinessOperationEnum.CREATE, SystemReturnCode.SUCCESS, null, info);
        // 5. 对象转换（前端需要用ID在新增后做授权，所以需要返回对象）
        AppInfoDto dto = new AppInfoDto();
        BeanUtils.copyProperties(info, dto);
        return perfectDto(dto);
    }

    @Override
    public int updateAppInfo(AppInfoParam param) {
        // 1. 基础校验
        AppInfo old = appInfoMapper.selectByPrimaryKey(param.getId());
        if (old == null) {
            throw new ResourceNotFoundException("APP信息不存在");
        } else if (DictionaryCodeEnum.TEMPLATE_TYPE_INNER.getCode().equals(old.getTemplateType())) {
            throw new ForbiddenException("无权修改内置模板");
        } else if (DictionaryCodeEnum.TEMPLATE_TYPE_SYSTEM.getCode().equals(old.getTemplateType())) {
            throw new ForbiddenException("无权修改系统模板");
        }
        // 2. 修改
        AppInfo info = new AppInfo();
        BeanUtils.copyProperties(param, info);
        // 更新基础信息不包括状态和模板（由专用接口更新）
        info.setStatus(null);
        info.setTemplateType(null);
        info.setUpdateUser(CurrentUser.getInfo().getUserId());
        info.setUpdateTime(new Date());
        int rows = appInfoMapper.updateByPrimaryKey(info);
        // 3. 缓存应用信息
        info = appInfoMapper.selectByPrimaryKey(info.getId());
        flushAppInfoCache(info);
        // 4. 发送事件（修改)
        this.publishEvent(BusinessOperationEnum.UPDATE, SystemReturnCode.SUCCESS, old, info);
        return rows;
    }

    @Override
    public void updateState(AppInfoStateParam param) {
        // 1. 基础校验
        AppInfo old = appInfoMapper.selectByPrimaryKey(param.getId());
        if (old == null) {
            throw new ResourceNotFoundException("APP信息不存在");
        } else if (DictionaryCodeEnum.TEMPLATE_TYPE_INNER.getCode().equals(old.getTemplateType())) {
            throw new ForbiddenException("无权修改内置模板");
        } else if (DictionaryCodeEnum.TEMPLATE_TYPE_SYSTEM.getCode().equals(old.getTemplateType())) {
            throw new ForbiddenException("无权修改系统模板");
        }
        // 2. 修改状态
        CurrentUser.UserInfo currentUser = CurrentUser.getInfo();
        AppInfo info = new AppInfo();
        info.setId(param.getId());
        info.setStatus(param.getStatus());
        info.setStatusTime(new Date());
        info.setStatusMemo(param.getStatusMemo());
        info.setUpdateUser(currentUser.getUserId());
        info.setUpdateTime(new Date());
        appInfoMapper.updateByPrimaryKey(info);
        // 3. 缓存应用信息
        info = appInfoMapper.selectByPrimaryKey(info.getId());
        flushAppInfoCache(info);
        // 4. 发送事件（修改状态)
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
    public void updateTemplateType(String appId, String templateType) {
        // 1. 基础校验
        AppInfo old = appInfoMapper.selectByPrimaryKey(appId);
        if (old == null) {
            throw new ResourceNotFoundException("APP信息不存在");
        } else if (DictionaryCodeEnum.TEMPLATE_TYPE_INNER.getCode().equals(old.getTemplateType())) {
            throw new ForbiddenException("无权修改内置模板");
        } else if (DictionaryCodeEnum.TEMPLATE_TYPE_SYSTEM.getCode().equals(old.getTemplateType())) {
            throw new ForbiddenException("无权修改系统模板");
        }
        // 只要不是普通模板，都设置为非模板
        if (StringUtil.isNull(templateType)
                || !DictionaryCodeEnum.TEMPLATE_TYPE_NORMAL.getCode().equals(templateType)) {
            templateType = DictionaryCodeEnum.TEMPLATE_TYPE_NONE.getCode();
        }
        if (old.getTemplateType().equals(templateType)) {
            throw new SystemErrorException("模板类型没有变更，不需要更新");
        }
        // 如果要改为普通模板，则不能有继承的应用
        if (DictionaryCodeEnum.TEMPLATE_TYPE_NORMAL.getCode().equals(templateType)) {
            List<AppInherit> list = appInheritMapper.selectByAppId(appId);
            if (!list.isEmpty()) {
                throw new SystemErrorException("模板不能相互继承，请先去除继承关系，再设置为模板");
            }
        }
        // 2. 更新模板类型
        CurrentUser.UserInfo currentUser = CurrentUser.getInfo();
        AppInfo info = new AppInfo();
        info.setId(appId);
        info.setTemplateType(templateType);
        info.setUpdateUser(currentUser.getUserId());
        info.setUpdateTime(new Date());
        appInfoMapper.updateByPrimaryKey(info);
        // 3. 如果调整为非模板，则需要删除关联的授权
        if (DictionaryCodeEnum.TEMPLATE_TYPE_NORMAL.getCode().equals(old.getTemplateType())) {
            appInheritMapper.deleteByParentId(appId);
        }
        // 4. 发送事件
        AppInfo newInfo = appInfoMapper.selectByPrimaryKey(appId);
        if (DictionaryCodeEnum.TEMPLATE_TYPE_NONE.getCode().equals(newInfo.getTemplateType())) {
            // 删除模板
            this.publishEvent(BusinessOperationEnum.UNSET_TEMPLATE, SystemReturnCode.SUCCESS, old, newInfo);
        }else {
            // 设为模板
            this.publishEvent(BusinessOperationEnum.SET_TEMPLATE, SystemReturnCode.SUCCESS, old, newInfo);
        }
    }

    @Override
    public int deleteByPrimaryKey(String id) {
        // 1. 基础校验
        AppInfo old = appInfoMapper.selectByPrimaryKey(id);
        if (old == null) {
            throw new ResourceNotFoundException("信息不存在");
        } else if (DictionaryCodeEnum.TEMPLATE_TYPE_INNER.getCode().equals(old.getTemplateType())) {
            throw new ForbiddenException("无权删除内置模板");
        } else if (DictionaryCodeEnum.TEMPLATE_TYPE_SYSTEM.getCode().equals(old.getTemplateType())) {
            throw new ForbiddenException("无权删除系统模板");
        }
        // 2. 删除
        old.setDeleteState(System.currentTimeMillis());
        int rows = appInfoMapper.deleteByPrimaryKey(id, old.getDeleteState());
        // 3. 缓存应用信息
        flushAppInfoCache(old);
        // 4. 发布事件（删除）
        this.publishEvent(BusinessOperationEnum.DELETE, SystemReturnCode.SUCCESS, old, null);
        return rows;
    }

    @Override
    public AppInfoDto selectByPrimaryKey(String id) {
        AppInfo info = appInfoMapper.selectByPrimaryKey(id);
        if (info == null) {
            return null;
        }
        // 对象转换
        AppInfoDto dto = new AppInfoDto();
        BeanUtils.copyProperties(info, dto);
        return perfectDto(dto);
    }

    @Override
    public PageResult<AppInfoDto> listByPage(AppInfoListParam param) {
        // 分页查询
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("appName",  param.getAppName());
        paramMap.put("appkey",   param.getAppkey());
        paramMap.put("keyword",  param.getKeyword());
        paramMap.put("moduleId", param.getModuleId());
        paramMap.put("instituteId", param.getInstituteId());
        // 按模板查询时，目前只查询普通模板
        if (StringUtil.notNull(param.getTemplateType()) && DictionaryCodeEnum.TEMPLATE_TYPE_NORMAL.getCode().equals(param.getTemplateType())) {
            paramMap.put("templateType", param.getTemplateType());
        }
        PageHelper.startPage(param.getPageNum(), param.getPageSize());
        PageList<AppInfoDto> list = (PageList<AppInfoDto>) appInfoMapper.selectBySearch(paramMap);
        // 对象转换
        for (AppInfoDto dto : list.getResult()) {
            perfectDto(dto);
        }
        // 封装返回数据
        return PageResult.of(list);
    }

    @Override
    public List<AppInfoDto> listByTemplate(String appId) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("inheritAppId", appId);
        paramMap.put("templateType", DictionaryCodeEnum.TEMPLATE_TYPE_NORMAL.getCode());

        List<AppInfoDto> resultList = appInfoMapper.selectBySearch(paramMap);
        // 对象转换
        for (AppInfoDto dto : resultList) {
            perfectDto(dto);
        }
        return resultList;
    }

    @Override
    public List<AppInfoDto> listInheritByApp(String appId) {
        List<AppInfoDto> resultList = appInfoMapper.selectInheritByAppId(appId);
        // 对象转换
        for (AppInfoDto dto : resultList) {
            perfectDto(dto);
        }
        return resultList;
    }

    @Override
    public List<AppInfoDto> listInheritBySub(String appId) {
        List<AppInfoDto> resultList = appInfoMapper.selectInheritByParentId(appId);
        // 对象转换
        for (AppInfoDto dto : resultList) {
            perfectDto(dto);
        }
        return resultList;
    }

    /**
     * 完善DTO信息
     */
    private AppInfoDto perfectDto(AppInfoDto dto) {
        if (StringUtil.notNull(dto.getModuleId())) {
            ModuleInfo module = moduleInfoMapper.selectByPrimaryKey(dto.getModuleId());
            dto.setModuleName(module == null ? null : module.getModuleName());
        }
        return dto;
    }

    @Override
    public void flushAppInfoCache() {
        List<AppInfo> list = appInfoMapper.selectByAll();
        for (AppInfo info: list) {
            flushAppInfoCache(info);
        }
    }

    @Override
    @Async
    public void flushAppPermissionCache() {
        List<AppInfo> list = appInfoMapper.selectByAll();
        for (AppInfo info: list) {
            flushAppPermissionCache(info);
        }
    }

    @Override
    @Async
    public void flushAppPermissionCache(String appId) {
        AppInfo info = appInfoMapper.selectByPrimaryKey(appId);
        if (info == null) {
            logger.warn("角色[{}]不存在，无法刷新授权缓存信息", appId);
            return;
        }
        flushAppPermissionCache(info);
    }

    /**
     * 缓存APP授权
     */
    private void flushAppPermissionCache(AppInfo info) {
        // 1. 非内置模板，已经删除或禁用，则去除授权缓存
        if (!DictionaryCodeEnum.TEMPLATE_TYPE_INNER.getCode().equals(info.getTemplateType())) {
            if (!BaseConstant.NOT_DELETED.equals(info.getDeleteState()) || !DictionaryCodeEnum.STATE_NORMAL.getCode().equals(info.getStatus())) {
                logger.info("应用[{}][{}]已经删除（或禁用），删除授权缓存", info.getAppName(), info.getAppkey());
                RedisUtil.remove(BaseCacheKey.APPKEY_PERMISSION_PREFIX + info.getAppkey());
                return;
            }
        }
        // 2. 组装授权路径
        // 2.1 查询父级资源
        List<AppInherit> parentAppList = appInheritMapper.selectByAppId(info.getId());
        List<String> appIds = parentAppList.stream().map(AppInherit::getParentId).collect(Collectors.toList());
        appIds.add(info.getId()); // 添加自身
        // 2.2 组装授权路径
        Set<String> urls = new HashSet<>();
        for (String tempAppId : appIds) {
            List<ResourceInfoDto> list = resourceInfoMapper.listByAppId(tempAppId);
            Set<String> tempUrls = list.stream()
                    .map(ResourceInfoDto::getResourceUrl)
                    .filter(StringUtil::notNull).collect(Collectors.toSet());
            urls.addAll(tempUrls);
        }
        // 3. 缓存
        if (DictionaryCodeEnum.TEMPLATE_TYPE_INNER.getCode().equals(info.getTemplateType())) {
            RedisUtil.remove(BaseCacheKey.APPKEY_PERMISSION_PREFIX + info.getAppkey());
            RedisUtil.putSetAll(BaseCacheKey.APPKEY_PERMISSION_PREFIX + info.getAppkey(), urls, null);
            logger.info("普通应用[{}]-[{}]授权缓存刷新成功", info.getAppName(), info.getAppkey());
        } else {
            RedisUtil.remove(info.getAppkey());
            RedisUtil.putSetAll(info.getAppkey(), urls, null);
            logger.info("内置应用[{}]-[{}]授权缓存刷新成功", info.getAppName(), info.getAppkey());
        }
    }

    /**
     * 缓存APP信息
     */
    private void flushAppInfoCache(AppInfo info) {
        // 1. 删除现有的信息
        RedisUtil.remove(BaseCacheKey.APPKEY_SECRET_PREFIX + info.getAppkey());
        RedisUtil.remove(BaseCacheKey.APPKEY_INFO_PREFIX + info.getId());
        // 2. 跳过内置模板、禁用的应用
        if (DictionaryCodeEnum.TEMPLATE_TYPE_INNER.getCode().equals(info.getTemplateType())
                || !BaseConstant.NOT_DELETED.equals(info.getDeleteState())
                || !DictionaryCodeEnum.STATE_NORMAL.getCode().equals(info.getStatus())
        ) {
            logger.info("应用[{}][{}]已经删除（或禁用），跳过信息缓存", info.getAppName(), info.getAppkey());
            return;
        }
        // 3. 放入最新的信息
        CurrentUser.AppInfo appInfo = new CurrentUser.AppInfo();
        appInfo.setInstituteId(info.getInstituteId());
        appInfo.setAppkey(info.getAppkey());
        appInfo.setSecret(info.getSecret());
        appInfo.setType(info.getTemplateType());
        RedisUtil.put(BaseCacheKey.APPKEY_SECRET_PREFIX + info.getAppkey(), info.getSecret(), null);
        RedisUtil.put(BaseCacheKey.APPKEY_INFO_PREFIX + info.getId(), appInfo, null);
        logger.info("应用[{}]-[{}]信息缓存刷新成功", info.getAppName(), info.getAppkey());
    }

    private void publishEvent(BusinessOperationEnum type, BaseReturnCode status, AppInfo oldInfo, AppInfo newInfo) {
        RemoteEvent<AppInfo> event = new RemoteEvent<>(BusinessTypeEnum.ACCOUNT.getCode(), type.getCode(), status.getCode());
        event.setBusinessSubType("APP");
        event.setOldData(oldInfo);
        event.setNewData(newInfo);
        // 补充业务信息
        AppInfo info = (newInfo == null) ? oldInfo : newInfo;
        if (info != null) {
            event.setBusinessId(info.getId());
            event.setBusinessInstituteId(info.getInstituteId());
        }
        event.setRemarks(status.getText());
        eventPublisher.publishEvent(event);
    }

}
