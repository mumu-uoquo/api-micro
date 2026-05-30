package com.uoquo.scheduler.platform.remote;

import com.uoquo.scheduler.platform.model.dto.UserInfoDto;
import com.uoquo.scheduler.platform.model.param.UserListByRangeParam;
import com.uoquo.mybatis.page.PageResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 用户远程服务
 * @author xuhz
 */
@FeignClient(name = "service-platform", path = "/health/api/platform", contextId = "UserRemoteService")
public interface UserRemoteService {

    /**
     * 获取指定范围的用户列表
     */
    @RequestMapping(value = "/admin/v1/user/list/range")
    PageResult<UserInfoDto> listUserByRange(@RequestBody UserListByRangeParam param);
}
