package com.uoquo.platform.institute.service;

import com.uoquo.platform.institute.model.dto.DepartmentTreeDto;
import com.uoquo.platform.institute.model.param.DepartmentInfoParam;

import java.util.List;
import java.util.Map;


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
     * 根据部门ID列表查询名称（不关注部门状态），入参为空返回空Map
     *
     * @return key=部门ID，value=部门名称
     */
    Map<String, String> mapNameByIds(List<String> ids);

    /**
     * 部门列表：树状
     */
    List<DepartmentTreeDto> listDepartmentInfoByTree(String instituteId);

}
