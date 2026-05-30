package com.uoquo.platform.system.mapper;

import com.uoquo.platform.system.model.dto.SettingDto;
import com.uoquo.platform.system.model.pojo.SysSetting;
import com.uoquo.mybatis.sensitive.SensitiveField;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Set;

public interface SysSettingMapper {

    /**
     * 单条新增
     */
    int insert(SysSetting row);

    /**
     * 根据编码删除
     */
    int deleteByCode(String code);

    /**
     * 单条更新
     */
    int updateByCode(SysSetting row);

    /**
     * 批量更新
     */
    int batchUpdate(List<SysSetting> list);

    /**
     * 根据编码查询
     */
    SysSetting selectByCode(String code);

    // === 自动解密查询 ===

    /**
     * 根据关键字查列表
     */
    List<SettingDto> selectEncryptedByCodePrefix(@Param("prefix") String prefix, @Param("types") Set<String> types);

    /**
     * 查询作用范围查询
     */
    List<SettingDto> selectEncryptedByPublicType(@Param("publicType") String publicType);

    // === 单条加密查询 ===

    /**
     * 根据 config_code 查询加密配置，返回 SettingDto（自动解密）
     */
    SettingDto selectEncryptedByCode(@Param("configCode") String configCode);

    // === 加密保存（@SensitiveField 在方法参数上）===

    /**
     * 插入加密配置记录（configValue 通过 @SensitiveField 参数注解自动加密）
     */
    int insertWithEncryptedValue(@Param("id") String id,
                                 @Param("configName") String configName,
                                 @Param("configCode") String configCode,
                                 @SensitiveField @Param("configValue") String configValue,
                                 @Param("description") String description,
                                 @Param("publicType") String publicType,
                                 @Param("updateUser") String updateUser,
                                 @Param("updateTime") Date updateTime);

    /**
     * 更新加密配置（configValue 通过 @SensitiveField 参数注解自动加密）
     */
    int updateConfigValueEncrypted(@Param("configCode") String configCode,
                                   @SensitiveField @Param("configValue") String configValue,
                                   @Param("updateUser") String updateUser,
                                   @Param("updateTime") Date updateTime);
}