package com.uoquo.platform.logs.service;

import com.uoquo.platform.logs.model.dto.LogBusinessAccessDto;
import com.uoquo.platform.logs.model.dto.LogBusinessChangeDto;
import com.uoquo.platform.logs.model.param.LogsBusinessSearchParam;
import com.uoquo.mybatis.page.PageResult;

/**
 * 业务日志服务
 * @author uoquo
 */
public interface LogsBusinessService {

    /**
     * 查询：变更日志
     */
    PageResult<LogBusinessChangeDto> listChangeLogs(LogsBusinessSearchParam param);

    /**
     * 查询：访问日志
     */
    PageResult<LogBusinessAccessDto> listAccessLogs(LogsBusinessSearchParam param);
}
