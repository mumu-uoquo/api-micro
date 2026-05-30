package com.uoquo.platform.institute.service;

import com.uoquo.platform.institute.model.dto.DepartmentTreeDto;
import com.uoquo.platform.institute.model.param.DepartmentInfoParam;

import java.util.List;


public interface DepartmentInfoService {

    /**
     * 新增部门
     */
    String addDepartmentInfo(DepartmentInfoParam param);

    /**
     * 更新部门
     */
    void updateDepartmentInfo(DepartmentInfoParam param);

    /**
     * 更新部门的所在区域到默认区域下
     */
    void updateDepartmentArea2Default(String departmentId);

    /**
     * 删除部门<br>
     * 同时删除用户、区域的对应关系
     */
    void deleteDepartmentInfo(String departmentId);

    /**
     * 部门详情：根据主键ID
     */
    DepartmentTreeDto getDepartmentInfo(String departmentId);

    /**
     * 部门列表：树状
     */
    List<DepartmentTreeDto> listDepartmentInfoByTree(String instituteId);

}
