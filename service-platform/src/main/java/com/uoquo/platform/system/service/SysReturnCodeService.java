package com.uoquo.platform.system.service;

import com.uoquo.platform.system.model.dto.SysReturnCodeDto;
import com.uoquo.platform.system.model.param.SysReturnCodeParam;
import com.uoquo.platform.system.model.param.SysReturnCodeSearchParam;
import com.uoquo.mybatis.page.PageResult;

import java.util.List;

public interface SysReturnCodeService {

    /**
     * 新增系统响应码
     */
    String saveReturnCode(SysReturnCodeParam param);

    /**
     * 删除系统响应码
     */
    void deleteReturnCode(String id);

    /**
     * 查询系统响应码详情（按ID）
     */
    SysReturnCodeDto getReturnCodeById(String id);

    /**
     * 查询系统响应码详情（按响应码）
     */
    SysReturnCodeDto getReturnCodeByCode(String returnCode);

    /**
     * 分页查询系统响应码
     */
    PageResult<SysReturnCodeDto> listReturnCodeByPage(SysReturnCodeSearchParam param);

    /**
     * 查询所有系统响应码<br>
     * 注：此方法仅返回code和value，不返回其他信息
     */
    List<SysReturnCodeDto> listAllReturnCodes();
}
