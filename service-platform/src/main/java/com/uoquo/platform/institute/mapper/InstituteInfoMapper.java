package com.uoquo.platform.institute.mapper;

import com.uoquo.platform.institute.model.pojo.InstituteInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface InstituteInfoMapper {

    /**
     * 新增
     */
    int insert(InstituteInfo row);
    /**
     * 删除（逻辑删）
     */
    int deleteByPrimaryKey(@Param("id") String id, @Param("deleteState") Long deleteState);

    /**
     * 修改
     */
    int updateByPrimaryKey(InstituteInfo row);

    /**
     * 批量修改父路径
     */
    int batchUpdateParentPath(@Param("oldPath") String oldPath, @Param("newPath") String newPath);

    /**
     * 单条查询：主键ID
     */
    InstituteInfo selectByPrimaryKey(String id);

    /**
     * 单条查询：角色ID
     */
    InstituteInfo selectByRoleId(String roleId);

    /**
     * 单条查询：根据唯一标识（ID、名称等）
     */
    InstituteInfo selectByUnique(Map<String, Object> map);

    /**
     * 列表查询
     */
    List<InstituteInfo> selectBySearch(Map<String, Object> map);
}