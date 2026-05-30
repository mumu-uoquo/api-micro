package com.uoquo.platform.logs.service;

import com.uoquo.platform.logs.model.dto.LogUserOnlineDto;
import com.uoquo.platform.logs.model.param.OnlineUserSearchParam;
import com.uoquo.platform.logs.model.pojo.LogUserLogin;
import com.uoquo.mybatis.page.PageResult;

/**
 * 在线用户管理服务
 * @author uoquo
 */
public interface OnlineUserService {

    /**
     * 登录时 upsert 在线记录（userId 为空或 "unknown" 时跳过）
     */
    void upsertOnlineUser(LogUserLogin info, String fullToken);

    /**
     * 登出时删除在线记录（userId 为空时跳过；appKey 为空时仅按 userId 删除）
     */
    void removeOnlineUser(String userId, String appKey);

    /**
     * 分页查询在线用户列表
     */
    PageResult<LogUserOnlineDto> listOnlineUsers(OnlineUserSearchParam param);

    /**
     * 踢出用户（按主键 id 删除），记录不存在时返回 false
     */
    boolean kickOutUser(String id);
}
