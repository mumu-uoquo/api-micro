package com.uoquo.platform.logs.service;

import com.uoquo.platform.logs.model.dto.LogUserLoginDto;
import com.uoquo.platform.logs.model.param.LogUserLoginParam;
import com.uoquo.platform.logs.model.param.LogsAuthSearchParam;
import com.uoquo.platform.logs.model.param.LogUserLogoutParam;
import com.uoquo.mybatis.page.PageResult;

/**
 * 认证日志服务
 * @author xuhz
 */
public interface LogsAuthService {

    /**
     * 新增认证日志
     */
    String addLogsInfo(LogUserLoginParam param);

    /**
     * 更新登出信息
     */
    String updateLogoutInfo(LogUserLogoutParam param);

    /**
     * 查询登录日志
     */
    PageResult<LogUserLoginDto> listBySearch(LogsAuthSearchParam param);

    /**
     * 查询登录详情
     */
    LogUserLoginDto getInfoById(String id);
}
