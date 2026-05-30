package com.uoquo.platform.user.mapper;

import com.uoquo.platform.user.model.pojo.GroupInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface GroupInfoMapper {

    /**
     * 新增
     */
    int insert(GroupInfo row);

    /**
     * 删除（逻辑删）
     */
    int deleteByPrimaryKey(@Param("id") String id, @Param("deleteState") Long deleteState);

    /**
     * 修改
     */
    int updateByPrimaryKey(GroupInfo row);

    /**
     * 单条查询
     */
    GroupInfo selectByPrimaryKey(String id);

    /**
     * 列表查询：根据机构ID查询
     */
    List<GroupInfo> listByInstituteId(String instituteId);

    /**
     * 列表查询：根据部门ID查询
     */
    List<GroupInfo> listByDepartmentId(String departmentId);

    /**
     * 列表查询：根据用户ID查询
     */
    List<GroupInfo> listByUserId(String userId);
}