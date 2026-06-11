package com.uoquo.platform.institute.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uoquo.cloud.events.RemoteEvent;
import com.uoquo.platform.common.BusinessOperationEnum;
import com.uoquo.platform.common.BusinessTypeEnum;
import com.uoquo.platform.institute.mapper.InstituteSettingMapper;
import com.uoquo.platform.institute.model.pojo.InstituteSetting;
import com.uoquo.platform.institute.service.InstituteSettingService;
import com.uoquo.platform.system.model.dto.SettingDto;
import com.uoquo.platform.system.model.param.SettingSaveParam;
import com.uoquo.platform.system.service.SysSettingService;
import com.uoquo.utils.CurrentUser;
import com.uoquo.utils.IDGenerator;
import com.uoquo.utils.StringUtil;
import com.uoquo.web.BaseReturnCode;
import com.uoquo.web.SystemReturnCode;
import com.uoquo.web.events.UoquoEventPublisher;
import com.uoquo.web.exception.ParamErrorException;
import com.uoquo.web.exception.ResourceNotFoundException;

/**
 * 机构配置服务实现
 */
@Service
public class InstituteSettingServiceImpl implements InstituteSettingService {

    @Autowired
    private InstituteSettingMapper instituteSettingMapper;

    @Autowired
    private SysSettingService sysSettingService;

    @Autowired
    private UoquoEventPublisher eventPublisher;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveSetting(String instituteId, List<SettingSaveParam> list) {
        CurrentUser.UserInfo currentUser = CurrentUser.getInfo();
        for (SettingSaveParam item : list) {
            // 对象组装
            InstituteSetting setting = new InstituteSetting();
            BeanUtils.copyProperties(item, setting);
            setting.setInstituteId(instituteId);
            setting.setUpdateUser(currentUser.getUserId());
            setting.setUpdateTime(new Date());
            // 存在时编辑，不存在时新增
            InstituteSetting old = instituteSettingMapper.selectByInstituteIdAndCode(instituteId, setting.getConfigCode());
            if (old == null) {
                setting.setId(IDGenerator.getNextULID());
                instituteSettingMapper.insert(setting);
                this.publishEvent(BusinessOperationEnum.CREATE, SystemReturnCode.SUCCESS, null, setting);
            } else {
                setting.setId(old.getId());
                instituteSettingMapper.updateByInstituteIdAndCode(setting);
                this.publishEvent(BusinessOperationEnum.UPDATE, SystemReturnCode.SUCCESS, old, setting);
            }
        }
    }

    @Override
    public void deleteByCode(String instituteId, String configCode) {
        if (StringUtil.isNull(configCode)) {
            throw new ParamErrorException("configCode");
        }
        // 获取机构ID
        if (StringUtil.isNull(instituteId)) {
            instituteId = CurrentUser.getInfo().getInstituteId();
        }
        // 检查配置是否存在
        InstituteSetting old = instituteSettingMapper.selectByInstituteIdAndCode(instituteId, configCode);
        if (old == null) {
            throw new ResourceNotFoundException("配置不存在");
        }
        // 执行删除
        instituteSettingMapper.deleteByInstituteIdAndCode(instituteId, configCode);
        this.publishEvent(BusinessOperationEnum.DELETE, SystemReturnCode.SUCCESS, old, null);
    }

    @Override
    public String getValueByCode(String instituteId, String configCode) {
        // 获取机构ID
        if (StringUtil.isNull(instituteId)) {
            instituteId = CurrentUser.getInfo().getInstituteId();
        }
        
        // 1. 先查机构配置
        InstituteSetting setting = instituteSettingMapper.selectByInstituteIdAndCode(instituteId, configCode);
        if (setting != null) {
            return setting.getConfigValue();
        }
        
        // 2. 再查系统配置
        SettingDto systemSetting = sysSettingService.getInfoByCode(configCode);
        return systemSetting == null ? null : systemSetting.getConfigValue();
    }

    @Override
    public List<SettingDto> listByPrefix(String instituteId, String prefix) {
        // 获取机构ID
        if (StringUtil.isNull(instituteId)) {
            instituteId = CurrentUser.getInfo().getInstituteId();
        }

        Map<String, SettingDto> configMap = new HashMap<>();
        // 1. 先查系统配置
        List<SettingDto> sysSettings = sysSettingService.listByPrefix(prefix);
        for (SettingDto item : sysSettings) {
            configMap.put(item.getConfigCode(), item);
        }
        // 2. 再查机构配置
        List<InstituteSetting> settings = instituteSettingMapper.selectByInstituteIdAndPrefix(instituteId, prefix);
        for (InstituteSetting item : settings) {
            SettingDto dto = this.convertToDto(item);
            // 若系统配置也存在时，将被覆盖（即：已机构的为准）
            configMap.put(dto.getConfigCode(), dto);
        }
        // 返回整合后的列表
        return new ArrayList<>(configMap.values());
    }

    private SettingDto convertToDto(InstituteSetting setting) {
        if (setting == null) {
            return null;
        }
        SettingDto dto = new SettingDto();
        dto.setConfigName(setting.getConfigName());
        dto.setConfigCode(setting.getConfigCode());
        dto.setConfigValue(setting.getConfigValue());
        dto.setDescription(setting.getDescription());
        dto.setSource("INSTITUTE");
        return dto;
    }

    private void publishEvent(BusinessOperationEnum type, BaseReturnCode status, InstituteSetting oldInfo, InstituteSetting newInfo) {
        RemoteEvent<InstituteSetting> event = new RemoteEvent<>(BusinessTypeEnum.INSTITUTE.getCode(), type.getCode(), status.getCode());
        event.setBusinessSubType("SETTINGS");
        event.setOldData(oldInfo);
        event.setNewData(newInfo);
        // 补充业务信息
        InstituteSetting info = (newInfo == null) ? oldInfo : newInfo;
        if (info != null) {
            event.setBusinessId(info.getId());
            event.setBusinessInstituteId(info.getInstituteId());
        }
        event.setRemarks(status.getText());
        eventPublisher.publishEvent(event);
    }
}
