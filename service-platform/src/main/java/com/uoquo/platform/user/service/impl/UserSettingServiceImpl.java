package com.uoquo.platform.user.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uoquo.cloud.events.RemoteEvent;
import com.uoquo.platform.common.BusinessOperationEnum;
import com.uoquo.platform.common.BusinessTypeEnum;
import com.uoquo.platform.institute.service.InstituteSettingService;
import com.uoquo.platform.system.model.dto.SettingDto;
import com.uoquo.platform.system.model.param.SettingSaveParam;
import com.uoquo.platform.user.mapper.UserInfoMapper;
import com.uoquo.platform.user.mapper.UserSettingMapper;
import com.uoquo.platform.user.model.pojo.UserInfo;
import com.uoquo.platform.user.model.pojo.UserSetting;
import com.uoquo.platform.user.service.UserSettingService;
import com.uoquo.utils.CurrentUser;
import com.uoquo.utils.IDGenerator;
import com.uoquo.utils.StringUtil;
import com.uoquo.web.BaseReturnCode;
import com.uoquo.web.SystemReturnCode;
import com.uoquo.web.events.UoquoEventPublisher;
import com.uoquo.web.exception.ParamErrorException;
import com.uoquo.web.exception.ResourceNotFoundException;

/**
 * 用户配置服务实现
 */
@Service
public class UserSettingServiceImpl implements UserSettingService {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Autowired
    private UserSettingMapper userSettingMapper;

    @Autowired
    private InstituteSettingService instituteSettingService;

    @Autowired
    private UoquoEventPublisher eventPublisher;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveSetting(String userId, List<SettingSaveParam> list) {
        CurrentUser.UserInfo currentUser = CurrentUser.getInfo();
        // 用户对应的机构
        UserInfo user = userInfoMapper.selectByPrimaryKey(userId);
        if (user == null) {
            throw new ResourceNotFoundException();
        }
        for (SettingSaveParam item : list) {
            // 组装对象
            UserSetting setting = new UserSetting();
            BeanUtils.copyProperties(item, setting);
            setting.setUserId(userId);
            setting.setUpdateUser(currentUser.getUserId());
            setting.setUpdateTime(new Date());
            // 存在时编辑，不存在时新增
            UserSetting old = userSettingMapper.selectByUserIdAndCode(userId, setting.getConfigCode());
            if (old == null) {
                setting.setId(IDGenerator.getNextULID());
                userSettingMapper.insert(setting);
                this.publishEvent(BusinessOperationEnum.CREATE, SystemReturnCode.SUCCESS, null, setting, user.getInstituteId());
            } else if (Objects.equals(old.getConfigValue(), setting.getConfigValue())) {
                logger.info("用户[{}]的配置[{}]没有变化不修改. 旧值:{}, 新值:{}", userId, setting.getConfigCode(), old.getConfigValue(), setting.getConfigValue());
            } else {
                setting.setId(old.getId());
                userSettingMapper.updateByUserIdAndCode(setting);
                this.publishEvent(BusinessOperationEnum.UPDATE, SystemReturnCode.SUCCESS, old, setting, user.getInstituteId());
            }
        }
    }

    @Override
    public void deleteByCode(String userId, String configCode) {
        if (StringUtil.isNull(configCode)) {
            throw new ParamErrorException("configCode");
        }
        // 用户对应的机构
        UserInfo user = userInfoMapper.selectByPrimaryKey(userId);
        if (user == null) {
            throw new ResourceNotFoundException();
        }
        // 检查配置是否存在
        UserSetting old = userSettingMapper.selectByUserIdAndCode(userId, configCode);
        if (old == null) {
            throw new ResourceNotFoundException("配置不存在");
        }
        // 执行删除
        userSettingMapper.deleteByUserIdAndCode(userId, configCode);
        this.publishEvent(BusinessOperationEnum.DELETE, SystemReturnCode.SUCCESS, old, null, user.getInstituteId());
    }

    @Override
    public String getValueByCode(String userId, String configCode) {
        // 1. 先查用户配置
        UserSetting setting = userSettingMapper.selectByUserIdAndCode(userId, configCode);
        if (setting != null) {
            return setting.getConfigValue();
        }
        
        // 2. 再查机构配置（内部已实现机构配置不存在时，获取系统配置）
        // 用户对应的机构
        UserInfo user = userInfoMapper.selectByPrimaryKey(userId);
        if (user == null) {
            throw new ResourceNotFoundException();
        }
        return instituteSettingService.getValueByCode(user.getInstituteId(), configCode);
    }

    @Override
    public List<SettingDto> listByPrefix(String userId, String prefix) {
        // 用户对应的机构
        UserInfo user = userInfoMapper.selectByPrimaryKey(userId);
        if (user == null) {
            throw new ResourceNotFoundException();
        }
        
        Map<String, SettingDto> configMap = new HashMap<>();
        // 1. 先查机构和系统配置
        List<SettingDto> instituteSettings = instituteSettingService.listByPrefix(user.getInstituteId(), prefix);
        for (SettingDto item : instituteSettings) {
            configMap.put(item.getConfigCode(), item);
        }
        // 2. 再查用户配置
        List<UserSetting> settings = userSettingMapper.selectByUserIdAndPrefix(userId, prefix);
        for (UserSetting item : settings) {
            SettingDto dto = this.convertToDto(item);
            configMap.put(dto.getConfigCode(), dto);
        }
        // 返回整合后的列表
        return new ArrayList<>(configMap.values());
    }

    private SettingDto convertToDto(UserSetting setting) {
        if (setting == null) {
            return null;
        }
        SettingDto dto = new SettingDto();
        dto.setConfigName(setting.getConfigName());
        dto.setConfigCode(setting.getConfigCode());
        dto.setConfigValue(setting.getConfigValue());
        dto.setDescription(setting.getDescription());
        dto.setSource("USER");
        return dto;
    }

    private void publishEvent(BusinessOperationEnum type, BaseReturnCode status, UserSetting oldInfo, UserSetting newInfo, String instituteId) {
        RemoteEvent<UserSetting> event = new RemoteEvent<>(BusinessTypeEnum.ACCOUNT.getCode(), type.getCode(), status.getCode());
        event.setBusinessSubType("SETTINGS");
        event.setOldData(oldInfo);
        event.setNewData(newInfo);
        // 补充业务信息
        UserSetting info = (newInfo == null) ? oldInfo : newInfo;
        if (info != null) {
            event.setBusinessId(info.getId());
            event.setBusinessInstituteId(instituteId);
        }
        event.setRemarks(status.getText());
        eventPublisher.publishEvent(event);
    }
}
