package com.uoquo.platform.system.mapper;

import com.uoquo.platform.system.model.pojo.SysHoliday;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface SysHolidayMapper {

    /**
     * @mbg.generated generated automatically, do not modify!
     * 新增
     */
    int insert(SysHoliday row);

    /**
     * 新增：批量
     */
    int batchInsert(List<SysHoliday> list);

    /**
     * @mbg.generated generated automatically, do not modify!
     * 按ID删除
     */
    int deleteByPrimaryKey(String id);

    /**
     * @mbg.generated generated automatically, do not modify!
     * 按日期删除
     */
    int deleteByDate(Date dateValue);

    /**
     * @mbg.generated generated automatically, do not modify!
     * 修改
     */
    int updateByPrimaryKey(SysHoliday row);

    /**
     * @mbg.generated generated automatically, do not modify!
     * 查询：单条
     */
    SysHoliday selectByPrimaryKey(String id);

    /**
     * @mbg.generated generated automatically, do not modify!
     * 查询：单条
     */
    SysHoliday selectByDate(Date dateValue);

    /**
     * 按范围查找
     */
    List<SysHoliday> listByDateRang(@Param("startDate") Date startDate, @Param("endDate") Date endDate);
}