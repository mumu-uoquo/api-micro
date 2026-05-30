package com.uoquo.scheduler.platform.remote;

import com.uoquo.scheduler.platform.model.param.BizEventRecordParam;
import com.uoquo.scheduler.platform.model.param.LogUserLoginParam;
import com.uoquo.scheduler.platform.model.param.LogUserLogoutParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 日志远程服务
 * @author xuhz
 */
@FeignClient(name = "service-platform", path = "/health/api/platform", contextId = "LogsRemoteService")
public interface LogsRemoteService {

    /**
     * 日志新增：认证日志
     */
    @RequestMapping(value = "/admin/v1/logs/login/save")
    String addLogs4Login(@RequestBody LogUserLoginParam param);

    /**
     * 日志新增：登出日志
     */
    @RequestMapping(value = "/admin/v1/logs/logout/save")
    String addLogs4Logout(@RequestBody LogUserLogoutParam param);

    /**
     * 日志新增：操作日志（消息事件）
     */
    @RequestMapping(value = "/admin/v1/logs/event/save")
    String addEventRecord(@RequestBody BizEventRecordParam param);

}
