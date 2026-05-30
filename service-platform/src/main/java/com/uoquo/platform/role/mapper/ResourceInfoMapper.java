package com.uoquo.platform.role.mapper;

import com.uoquo.platform.role.model.dto.ResourceInfoDto;
import com.uoquo.platform.role.model.pojo.ResourceInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface ResourceInfoMapper {

    /**
     * 新增
     */
    int insert(ResourceInfo row);

    /**
     * 删除（物理删除）
     */
    int deleteByPrimaryKey(String id);

    /**
     * 修改
     */
    int updateByPrimaryKey(ResourceInfo row);

    /**
     * 单条：根据ID
     */
    ResourceInfo selectByPrimaryKey(String id);

    /**
     * 查询：根据URL
     */
    ResourceInfo selectByResourceUrl(String url);

    /**
     * 校验：编码是否存在
     */
    int checkUrlIsExist(@Param("id") String id, @Param("resourceUrl") String resourceUrl);

    /**
     * 查询：根据关联ID
     */
    ResourceInfo selectByAppPermissionRelateId(String relateId);

    /**
     * 查询：根据父级APP关联ID
     */
    List<ResourceInfo> selectByAppInheritRelateId(String relateId);

    /**
     * 列表：常规
     */
    List<ResourceInfo> listBySearch(Map<String, Object> map);

    /**
     * 列表：根据角色ID
     */
    List<ResourceInfoDto> listByRoleId(String roleId);

    /**
     * 列表：根据AppId
     */
    List<ResourceInfoDto> listByAppId(String appId);

    /**
     * 列表：未关联AppId
     */
    List<ResourceInfoDto> listNotRelateAppId(@Param("appId") String appId, @Param("keyword") String keyword);

    /**
     * 列表：根据模块ID
     */
    List<ResourceInfoDto> listByModuleId(String moduleId);

    /**
     * 列表：未关联模块ID
     */
    List<ResourceInfoDto> listNotRelateModuleId(@Param("moduleId") String moduleId, @Param("keyword") String keyword);
}