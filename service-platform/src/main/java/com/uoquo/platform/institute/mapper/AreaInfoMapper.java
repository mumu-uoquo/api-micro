package com.uoquo.platform.institute.mapper;

import com.uoquo.platform.institute.model.pojo.AreaInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AreaInfoMapper {

    /**
     * @mbg.generated generated automatically, do not modify!
     * 新增
     */
    int insert(AreaInfo row);

    /**
     * @mbg.generated generated automatically, do not modify!
     * 修改
     */
    int updateByPrimaryKey(AreaInfo row);

    /**
     * @mbg.generated generated automatically, do not modify!
     * 删除（逻辑删）
     */
    int deleteByPrimaryKey(@Param("id") String id, @Param("deleteState") Long deleteState);

    /**
     * @mbg.generated generated automatically, do not modify!
     * 单条查询：主键ID
     */
    AreaInfo selectByPrimaryKey(String id);

    /**
     * @mbg.generated generated automatically, do not modify!
     * 单条查询：机构默认
     */
    AreaInfo selectByDefault(String instituteId);

    /**
     * 单条查询：根据第三方ID查询（机构下唯一）
     */
    AreaInfo selectByThirdId(@Param("instituteId") String instituteId, @Param("thirdId") String thirdId);

    /**
     * 单条查询：根据编码查询（机构下唯一）
     */
    AreaInfo selectByCode(@Param("instituteId") String instituteId, @Param("areaCode") String deptCode);

    /**
     * 单条查询：根据名称查询（机构下唯一）
     */
    AreaInfo selectByName(@Param("instituteId") String instituteId, @Param("areaName") String deptName);


    /**
     * 列表查询：根据机构ID查询
     */
    List<AreaInfo> listByInstituteId(String instituteId);
}