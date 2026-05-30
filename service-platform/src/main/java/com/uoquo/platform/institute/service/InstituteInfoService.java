package com.uoquo.platform.institute.service;

import com.uoquo.platform.institute.model.dto.InstituteInfoDto;
import com.uoquo.platform.institute.model.dto.InstituteTreeDto;
import com.uoquo.platform.institute.model.param.InstituteInfoParam;
import com.uoquo.platform.institute.model.param.InstituteListParam;
import com.uoquo.platform.institute.model.param.InstituteStateParam;
import com.uoquo.mybatis.page.PageResult;

import java.util.List;

public interface InstituteInfoService {

    /**
     * 新增机构
     */
    String addInstituteInfo(InstituteInfoParam param);

    /**
     * 更新机构
     */
    void updateInstituteInfo(InstituteInfoParam param);

    /**
     * 更新状态
     */
    void updateInstituteStatus(InstituteStateParam param);

    /**
     * 删除机构
     */
    void deleteInstituteInfo(String instituteId);

    /**
     * 机构详情：根据主键ID
     */
    InstituteInfoDto getInstituteInfo(String instituteId);

    /**
     * 检测是否自己管辖的机构
     */
    boolean checkSelfManageInstitute(String instituteId);

    /**
     * 企业列表：分页列表
     */
    PageResult<InstituteInfoDto> listInstituteInfoByPage(String rootInstituteId, InstituteListParam param);

    /**
     * 企业列表：树状
     */
    List<InstituteTreeDto> listInstituteInfoByTree(String rootInstituteId);

}
