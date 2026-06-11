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
import com.uoquo.utils.crypto.AES;
import com.uoquo.utils.crypto.RSA;
import com.uoquo.utils.spring.RedisUtil;
import com.uoquo.web.BaseCacheKey;
import com.uoquo.web.BaseReturnCode;
import com.uoquo.web.SystemReturnCode;
import com.uoquo.web.events.UoquoEventPublisher;
import com.uoquo.web.exception.ResourceNotFoundException;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@Service
public class SysSettingServiceImpl implements SysSettingService {
    final Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    private SysSettingMapper sysSettingMapper;

    @Autowired
    private UoquoEventPublisher eventPublisher;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void checkInitialization() throws Exception {
        SysSetting info;
        String currentUser = "SYSTEM";

        // 1. 检查 RSA 密钥对
        boolean hasRsaPublicKey  = sysSettingMapper.selectByCode(SettingsCode.RSA_PUBLIC_KEY) != null;
        boolean hasRsaPrivateKey = sysSettingMapper.selectByCode(SettingsCode.RSA_PRIVATE_KEY) != null;
        if (!hasRsaPublicKey || !hasRsaPrivateKey) {
            RSA.KeyPair rsaKeyPair = RSA.generateKeyPair();
            // 公钥
            SettingSaveParam pubParam = new SettingSaveParam();
            pubParam.setConfigName("RSA公钥");
            pubParam.setConfigCode(SettingsCode.RSA_PUBLIC_KEY);
            pubParam.setConfigValue(rsaKeyPair.getPublicKey());
            pubParam.setPublicType(SettingsCode.getPublicType(SettingsCode.RSA_PUBLIC_KEY));
            this.saveSetting(pubParam, currentUser);
            // 私钥
            SettingSaveParam priParam = new SettingSaveParam();
            priParam.setConfigName("RSA私钥");
            priParam.setConfigCode(SettingsCode.RSA_PRIVATE_KEY);
            priParam.setConfigValue(rsaKeyPair.getPrivateKey());
            pubParam.setPublicType(SettingsCode.getPublicType(SettingsCode.RSA_PRIVATE_KEY));
            this.saveSetting(priParam, currentUser);
        }

        // 2. 检查 AES 密钥
        info = sysSettingMapper.selectByCode(SettingsCode.AES_KEY);
        if (info == null) {
            SettingSaveParam param = new SettingSaveParam();
            param.setConfigName("AES密钥");
            param.setConfigCode(SettingsCode.AES_KEY);
            param.setConfigValue(AES.generateKey());
            param.setPublicType(SettingsCode.getPublicType(SettingsCode.AES_KEY));
            this.saveSetting(param, currentUser);
        }

        // 3. 检查网关通信密钥
        info = sysSettingMapper.selectByCode(SettingsCode.GLOBAL_GATEWAY_KEY);
        if (info == null) {
            SettingSaveParam param = new SettingSaveParam();
            param.setConfigName("网关通信密钥");
            param.setConfigCode(SettingsCode.GLOBAL_GATEWAY_KEY);
            param.setConfigValue(StringUtil.getRandomString(32));
            param.setPublicType(SettingsCode.getPublicType(SettingsCode.GLOBAL_GATEWAY_KEY));
            this.saveSetting(param, currentUser);
        }
    }

    @Override
    public void cache2Redis() {
        SysSetting info;
        // 网关通信密钥
        info = sysSettingMapper.selectByCode(SettingsCode.GLOBAL_GATEWAY_KEY);
        if (info != null) {
            RedisUtil.put(BaseCacheKey.GLOBAL_SECRET, info.getConfigValue(), null);
        }
        // 缓存RSA私钥
        info = sysSettingMapper.selectByCode(SettingsCode.RSA_PRIVATE_KEY);
        if (info != null) {
            RedisUtil.put("security.rsa.private-key", info.getConfigValue(), null);
        }
        // 缓存AES秘钥
        info = sysSettingMapper.selectByCode(SettingsCode.AES_KEY);
        if (info != null) {
            RedisUtil.put("security.aes.key", info.getConfigValue(), null);
        }
        // 缓存TOTP时间分片
        info = sysSettingMapper.selectByCode(SettingsCode.AES_TOTP_STEP);
        if (info != null) {
            try {
                RedisUtil.put("security.aes.time-step", Integer.parseInt(info.getConfigValue()), null);
            } catch (Exception e) {
                logger.error("缓存TOTP时间分片【{}】失败", info.getConfigValue(), e);
            }
        }
        // 缓存超时时间
        info = sysSettingMapper.selectByCode(SettingsCode.SESSION_TIMEOUT);
        if (info != null) {
            try {
                RedisUtil.put(BaseCacheKey.GLOBAL_TIMEOUT, Integer.parseInt(info.getConfigValue()), null);
            } catch (Exception e) {
                logger.error("缓存超时时间【{}】失败", info.getConfigValue(), e);
            }
        }

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveSetting(List<SettingSaveParam> list) {
        CurrentUser.UserInfo currentUser = CurrentUser.getInfo();
        for (SettingSaveParam item : list) {
            // 默认为内部配置（003001，需登录后获取）
            item.setPublicType(DictionaryCodeEnum.ROLE_TYPE_INNER.getCode());
            this.saveSetting(item, currentUser.getUserId());
        }
    }

    private void saveSetting(SettingSaveParam param, String userId) {
        SysSetting info = new SysSetting();
        BeanUtils.copyProperties(param, info);
        info.setUpdateUser(userId);
        info.setUpdateTime(new Date());

        SysSetting old = sysSettingMapper.selectByCode(param.getConfigCode());
        if (old == null) {
            info.setId(IDGenerator.getNextULID());
            sysSettingMapper.insert(info);
        } else {
            // 不更新公开范围，防止冲掉默认配置的公开范围
            info.setPublicType(null);
            sysSettingMapper.updateByCode(info);
        }

        // 发布事件
        SysSetting newInfo = sysSettingMapper.selectByCode(param.getConfigCode());
        if (DictionaryCodeEnum.ROLE_TYPE_PRIVATE.getCode().equals(newInfo.getPublicType())) {
            newInfo.setConfigValue(null);
        }
        if (old == null) {
            this.publishEvent(BusinessOperationEnum.CREATE, SystemReturnCode.SUCCESS, null, newInfo);
        } else {
            if (DictionaryCodeEnum.ROLE_TYPE_PRIVATE.getCode().equals(old.getPublicType())) {
                old.setConfigValue(null);
            }
            this.publishEvent(BusinessOperationEnum.UPDATE, SystemReturnCode.SUCCESS, old, newInfo);
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
        SysSetting info = sysSettingMapper.selectByCode(code);
        return info == null ? null : info.getConfigValue();
    }

    @Override
    public SettingDto getInfoByCode(String code) {
        SysSetting info = sysSettingMapper.selectByCode(code);
        if (info == null) {
            return null;
        }
        // 过滤私有配置
        if (DictionaryCodeEnum.ROLE_TYPE_PRIVATE.getCode().equals(info.getPublicType())) {
            logger.warn("用户[{}]查询私有配置[{}]", CurrentUser.getInfo().getUserId(), code);
            return null;
        }
        return this.convertToDto(info);
    }

    @Override
    public List<SettingDto> listByPrefix(String prefix) {
        if (StringUtil.isNull(prefix)) {
            return Collections.emptyList();
        }
        // 只查公开和内置的，不查私有（私有配置不对外公开）
        Set<String> types = new HashSet<>();
        types.add(DictionaryCodeEnum.ROLE_TYPE_NORMAL.getCode()); // 公开
        types.add(DictionaryCodeEnum.ROLE_TYPE_INNER.getCode());  // 内置
        List<SysSetting> list = sysSettingMapper.listByCodePrefix(prefix, types);
        // 对象转换
        List<SettingDto> result = new ArrayList<>();
        for (SysSetting item : list) {
            result.add(this.convertToDto(item));
        }
        // 按 config_code 排序
        result.sort(Comparator.comparing(SettingDto::getConfigCode));
        return result;
    }

    @Override
    public List<SettingDto> listPublicSettings() {
        // mybatis会对加密的内容自动解密
        List<SysSetting> list = sysSettingMapper.listByPublicType(DictionaryCodeEnum.ROLE_TYPE_NORMAL.getCode());
        // 对象转换
        List<SettingDto> result = new ArrayList<>();
        for (SysSetting item : list) {
            result.add(this.convertToDto(item));
        }
        // 手动添加当前服务器时间
        SettingDto time = new SettingDto();
        time.setConfigCode("server.time");
        time.setConfigValue(System.currentTimeMillis() + "");
        time.setSource("SYSTEM");
        result.add(time);
        // 按 config_code 排序
        result.sort(Comparator.comparing(SettingDto::getConfigCode));
        return result;
    }

    @Override
    public List<SettingDto> listPrivateSettings() {
        // mybatis会对加密的内容自动解密
        List<SysSetting> list = sysSettingMapper.listByPublicType(DictionaryCodeEnum.ROLE_TYPE_PRIVATE.getCode());
        // 对象转换
        List<SettingDto> result = new ArrayList<>();
        for (SysSetting item : list) {
            result.add(this.convertToDto(item));
        }
        // 按 config_code 排序
        result.sort(Comparator.comparing(SettingDto::getConfigCode));
        return result;
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
