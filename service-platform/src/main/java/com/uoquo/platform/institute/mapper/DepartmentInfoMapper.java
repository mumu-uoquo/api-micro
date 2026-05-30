package com.uoquo.platform.institute.mapper;

import com.uoquo.platform.institute.model.dto.DepartmentTreeDto;
import com.uoquo.platform.institute.model.pojo.DepartmentInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface DepartmentInfoMapper {

    /**
     * @mbg.generated generated automatically, do not modify!
     */
    int insert(DepartmentInfo row);

    /**
     * @mbg.generated generated automatically, do not modify!
     */
    int deleteByPrimaryKey(@Param("id") String id, @Param("deleteState") Long deleteState);

    /**
     * @mbg.generated generated automatically, do not modify!
     */
    int updateByPrimaryKey(DepartmentInfo row);

    /**
     * 批量修改所属分区
     */
    int batchUpdateArea(@Param("oldAreaId") String oldAreaId, @Param("newAreaId") String newAreaId);

    /**
     * 批量修改父路径
     */
    int batchUpdateParentPath(@Param("oldPath") String oldPath, @Param("newPath") String newPath);

    /**
     * @mbg.generated generated automatically, do not modify!
     */
    DepartmentInfo selectByPrimaryKey(String id);

    /**
     * @mbg.generated generated automatically, do not modify!
     */
    DepartmentInfo selectByDefault(String instituteId);

    /**
     * 单条查询：根据第三方ID查询（机构下唯一）
     */
    DepartmentInfo selectByThirdId(@Param("instituteId") String instituteId, @Param("thirdId") String thirdId);

    /**
     * 单条查询：根据编码查询（机构下唯一）
     */
    DepartmentInfo selectByCode(@Param("instituteId") String instituteId, @Param("deptCode") String deptCode);

    /**
     * 单条查询：根据名称查询（同级下唯一）
     */
    DepartmentInfo selectByName(@Param("instituteId") String instituteId, @Param("parentId") String parentId, @Param("deptName") String deptName);

    /**
     * 列表查询：按条件搜索（机构ID、区域ID）
     */
    List<DepartmentTreeDto> listBySearch(Map<String, Object> map);
}