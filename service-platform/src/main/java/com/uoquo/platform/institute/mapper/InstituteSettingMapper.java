package com.uoquo.platform.institute.mapper;

import com.uoquo.platform.institute.model.pojo.InstituteSetting;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface InstituteSettingMapper {
    
    /**
     * 插入机构配置
     */
    int insert(InstituteSetting setting);
    
    /**
     * 根据机构ID和配置标识更新
     */
    int updateByInstituteIdAndCode(InstituteSetting setting);
    
    /**
     * 根据机构ID和配置标识删除
     */
    int deleteByInstituteIdAndCode(@Param("instituteId") String instituteId, @Param("configCode") String configCode);
    
    /**
     * 根据机构ID和配置标识查询
     */
    InstituteSetting selectByInstituteIdAndCode(@Param("instituteId") String instituteId, @Param("configCode") String configCode);
    
    /**
     * 根据机构ID和前缀查询列表
     */
    List<InstituteSetting> selectByInstituteIdAndPrefix(@Param("instituteId") String instituteId, @Param("prefix") String prefix);
}