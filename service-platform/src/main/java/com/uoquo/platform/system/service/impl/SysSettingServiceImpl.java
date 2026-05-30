package com.uoquo.platform.system.service.impl;

import com.uoquo.cloud.events.RemoteEvent;
import com.uoquo.platform.common.BusinessOperationEnum;
import com.uoquo.platform.common.BusinessTypeEnum;
import com.uoquo.platform.common.DictionaryCodeEnum;
import com.uoquo.platform.common.SettingsCode;
import com.uoquo.platform.system.mapper.SysSettingMapper;
import com.uoquo.platform.system.model.dto.SettingDto;
import com.uoquo.platform.system.model.param.SettingSaveParam;
import com.uoquo.platform.system.model.pojo.SysSetting;
import com.uoquo.platform.system.service.SysSettingService;
import com.uoquo.utils.CurrentUser;
import com.uoquo.utils.IDGenerator;
import com.uoquo.utils.StringUtil;
import com.uoquo.web.BaseReturnCode;
import com.uoquo.web.SystemReturnCode;
import com.uoquo.web.events.UoquoEventPublisher;
import com.uoquo.web.exception.ResourceNotFoundException;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;

import java.util.*;

@Service
public class SysSettingServiceImpl implements SysSettingService {

    @Resource
    private SysSettingMapper sysSettingMapper;

    @Autowired
    private UoquoEventPublisher eventPublisher;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveSetting(List<SettingSaveParam> list) {
        CurrentUser.UserInfo currentUser = CurrentUser.getInfo();
        for (SettingSaveParam item : list) {
            SysSetting info = sysSettingMapper.selectByCode(item.getConfigCode());

            if (SettingsCode.ENCRYPTED_KEYS.contains(item.getConfigCode())) {
                // === 加密配置路径（通过 @SensitiveField 参数注解自动加密）===
                if (info == null) {
                    // 新增
                    sysSettingMapper.insertWithEncryptedValue(
                        IDGenerator.getNextULID(),
                        item.getConfigName(),
                        item.getConfigCode(),
                        item.getConfigValue(),  // 明文，拦截器通过 @SensitiveField 自动加密
                        item.getDescription(),
                        DictionaryCodeEnum.ROLE_TYPE_INNER.getCode(), // 强制 003001
                        currentUser.getUserId(),
                        new Date()
                    );
                } else {
                    // 更新（仅更新 configValue）
                    sysSettingMapper.updateConfigValueEncrypted(
                        item.getConfigCode(),
                        item.getConfigValue(),  // 明文，拦截器通过 @SensitiveField 自动加密
                        currentUser.getUserId(),
                        new Date()
                    );
                }
            } else {
                // === 非加密配置路径（原有逻辑）===
                SysSetting param = new SysSetting();
                BeanUtils.copyProperties(item, param);
                param.setUpdateUser(currentUser.getUserId());
                param.setUpdateTime(new Date());

                if (info == null) {
                    param.setId(IDGenerator.getNextULID());
                    param.setPublicType(DictionaryCodeEnum.ROLE_TYPE_INNER.getCode());
                    sysSettingMapper.insert(param);
                } else {
                    // update 路径不设置 publicType（保持 null → 动态 SQL 跳过）
                    param.setPublicType(null);
                    sysSettingMapper.updateByCode(param);
                }
            }

            // 发布事件
            SysSetting newInfo = sysSettingMapper.selectByCode(item.getConfigCode());
            if (info == null) {
                this.publishEvent(BusinessOperationEnum.CREATE, SystemReturnCode.SUCCESS, null, newInfo);
            } else {
                this.publishEvent(BusinessOperationEnum.UPDATE, SystemReturnCode.SUCCESS, info, newInfo);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByCode(String code) {
        // 检查配置是否存在
        SysSetting old = sysSettingMapper.selectByCode(code);
        if (old == null) {
            throw new ResourceNotFoundException("配置不存在");
        }
        
        // TODO: 检查是否有机构或用户依赖此配置
        // 可以在后续版本中添加依赖检查
        
        sysSettingMapper.deleteByCode(code);
        this.publishEvent(BusinessOperationEnum.DELETE, SystemReturnCode.SUCCESS, old, null);
    }

    @Override
    public String getValueByCode(String code) {
        if (SettingsCode.ENCRYPTED_KEYS.contains(code)) {
            // 走加密查询（映射到 SettingDto，拦截器自动解密）
            SettingDto enc = sysSettingMapper.selectEncryptedByCode(code);
            return enc == null ? null : enc.getConfigValue();
        }
        // 非加密配置走原逻辑
        SysSetting info = sysSettingMapper.selectByCode(code);
        return info == null ? null : info.getConfigValue();
    }

    @Override
    public List<SettingDto> listByPrefix(String prefix) {
        if (StringUtil.isNull(prefix)) {
            return Collections.emptyList();
        }
        // 只查内置和通用的，不查私有（私有配置不对外公开）
        Set<String> types = new HashSet<>();
        types.add(DictionaryCodeEnum.ROLE_TYPE_NORMAL.getCode());
        types.add(DictionaryCodeEnum.ROLE_TYPE_INNER.getCode());
        List<SettingDto> result = sysSettingMapper.selectEncryptedByCodePrefix(prefix, types);
        // 转换
        for (SettingDto item : result) {
            item.setSource("SYSTEM");
        }
        return result;
    }

    @Override
    public List<SettingDto> listPublicSettings() {
        // mybatis会对加密的内容自动解密
        List<SettingDto> list = sysSettingMapper.selectEncryptedByPublicType(DictionaryCodeEnum.ROLE_TYPE_NORMAL.getCode());
        for (SettingDto item : list) {
            item.setSource("SYSTEM");
        }
        // 按 config_code 排序
        list.sort(Comparator.comparing(SettingDto::getConfigCode));
        return list;
    }

    @Override
    public List<SettingDto> listPrivateSettings() {
        // mybatis会对加密的内容自动解密
        List<SettingDto> list = sysSettingMapper.selectEncryptedByPublicType(DictionaryCodeEnum.ROLE_TYPE_PRIVATE.getCode());
        for (SettingDto item : list) {
            item.setSource("SYSTEM");
        }
        // 按 config_code 排序
        list.sort(Comparator.comparing(SettingDto::getConfigCode));
        return list;
    }

    private SettingDto convertToDto(SysSetting setting) {
        if (setting == null) {
            return null;
        }
        SettingDto dto = new SettingDto();
        dto.setConfigName(setting.getConfigName());
        dto.setConfigCode(setting.getConfigCode());
        dto.setConfigValue(setting.getConfigValue());
        dto.setDescription(setting.getDescription());
        dto.setSource("SYSTEM");
        return dto;
    }

    private void publishEvent(BusinessOperationEnum type, BaseReturnCode status, SysSetting oldInfo, SysSetting newInfo) {
        RemoteEvent<SysSetting> event = new RemoteEvent<>(BusinessTypeEnum.SYSTEM.getCode(), type.getCode(), status.getCode());
        event.setBusinessSubType("SETTINGS");
        event.setOldData(oldInfo);
        event.setNewData(newInfo);
        // 补充业务信息
        SysSetting info = (newInfo == null) ? oldInfo : newInfo;
        if (info != null) {
            event.setBusinessId(info.getId());
        }
        event.setRemarks(status.getText());
        eventPublisher.publishEvent(event);
    }
}
