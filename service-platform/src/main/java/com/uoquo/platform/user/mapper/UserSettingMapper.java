package com.uoquo.platform.user.mapper;

import com.uoquo.platform.user.model.pojo.UserSetting;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户配置数据访问接口
 */
public interface UserSettingMapper {
    
    /**
     * 插入用户配置
     */
    int insert(UserSetting setting);
    
    /**
     * 根据用户ID和配置标识更新
     */
    int updateByUserIdAndCode(UserSetting setting);
    
    /**
     * 根据用户ID和配置标识删除
     */
    int deleteByUserIdAndCode(@Param("userId") String userId, @Param("configCode") String configCode);
    
    /**
     * 根据用户ID和配置标识查询
     */
    UserSetting selectByUserIdAndCode(@Param("userId") String userId, @Param("configCode") String configCode);
    
    /**
     * 根据用户ID和前缀查询列表
     */
    List<UserSetting> selectByUserIdAndPrefix(@Param("userId") String userId, @Param("prefix") String prefix);
}