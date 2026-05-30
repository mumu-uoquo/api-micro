package com.uoquo.platform.system.mapper;

import com.uoquo.platform.system.model.dto.AppInfoDto;
import com.uoquo.platform.system.model.pojo.AppInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface AppInfoMapper {

    /**
     * 新增
     */
    int insert(AppInfo row);

    /**
     * 删除（逻辑删）
     */
    int deleteByPrimaryKey(@Param("id") String id, @Param("deleteState") Long deleteState);

    /**
     * 修改
     */
    int updateByPrimaryKey(AppInfo row);

    /**
     * 单条：根据ID
     */
    AppInfo selectByPrimaryKey(String id);

    /**
     * 单条：根据AppKey
     */
    AppInfo selectByAppkey(String appkey);

    /**
     * 查询：根据关联ID
     */
    AppInfo selectByPermissionRelateId(String relateId);

    /**
     * 查询：根据继承ID
     */
    AppInfo selectByInheritRelateId(String relateId);

    /**
     * 查询：所有
     * 注：仅内部缓存权限时使用
     */
    List<AppInfo> selectByAll();

    /**
     * 列表查询
     */
    List<AppInfoDto> selectBySearch(Map<String, Object> map);

    /**
     * 列表查询：继承的列表
     */
    List<AppInfoDto> selectInheritByAppId(String appId);

    /**
     * 列表查询：被继承的列表
     */
    List<AppInfoDto> selectInheritByParentId(String parentId);
}