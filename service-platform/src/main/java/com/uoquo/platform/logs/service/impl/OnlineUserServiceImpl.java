package com.uoquo.platform.logs.service.impl;

import com.uoquo.platform.auth.service.AuthService;
import com.uoquo.platform.logs.mapper.LogUserOnlineMapper;
import com.uoquo.platform.logs.model.dto.LogUserOnlineDto;
import com.uoquo.platform.logs.model.param.OnlineUserSearchParam;
import com.uoquo.platform.logs.model.pojo.LogUserLogin;
import com.uoquo.platform.logs.model.pojo.LogUserOnline;
import com.uoquo.platform.logs.service.OnlineUserService;
import com.uoquo.utils.IDGenerator;
import com.uoquo.utils.StringUtil;
import com.uoquo.utils.json.JsonUtil;
import com.uoquo.web.SystemReturnCode;
import com.uoquo.web.mybatis.page.PageHelper;
import com.uoquo.mybatis.page.PageList;
import com.uoquo.mybatis.page.PageResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OnlineUserServiceImpl implements OnlineUserService {

    private static final Logger logger = LoggerFactory.getLogger(OnlineUserServiceImpl.class);

    @Autowired
    private LogUserOnlineMapper logUserOnlineMapper;

    @Autowired
    private AuthService authService;

    @Override
    public void upsertOnlineUser(LogUserLogin info, String fullToken) {
        String userId = info.getUserId();
        if (StringUtil.isNull(userId) || "unknown".equals(userId.trim())) {
            logger.info("用户ID为空，不加入在线信息表：{}", JsonUtil.serialize(info));
            return;
        } else if (StringUtil.isNull(info.getAppKey())) {
            logger.info("用户APPKEY为空，不加入在线信息表：{}", JsonUtil.serialize(info));
            return;
        } else if (StringUtil.isNull(info.getToken())) {
            logger.info("用户TOKEN为空，不加入在线信息表：{}", JsonUtil.serialize(info));
            return;
        }
        LogUserOnline online = new LogUserOnline();
        BeanUtils.copyProperties(info, online);
        online.setId(IDGenerator.getNextULID());
        online.setToken(fullToken);
        online.setCreateTime(new Date());
        online.setLoginTime(info.getLoginTime() != null ? info.getLoginTime() : new Date());
        logUserOnlineMapper.upsert(online);
    }

    @Override
    public void removeOnlineUser(String userId, String appKey) {
        if (userId == null || userId.trim().isEmpty()) {
            return;
        }
        if (StringUtil.notNull(appKey)) {
            logUserOnlineMapper.deleteByUserIdAndAppKey(userId, appKey);
        } else {
            logUserOnlineMapper.deleteByUserId(userId);
        }
    }

    @Override
    public PageResult<LogUserOnlineDto> listOnlineUsers(OnlineUserSearchParam param) {
        Map<String, Object> paramMap = new HashMap<>();
        if (StringUtil.notNull(param.getUserName())) {
            paramMap.put("userName", param.getUserName());
        }
        if (StringUtil.notNull(param.getInstituteId())) {
            paramMap.put("instituteId", param.getInstituteId());
        }
        if (StringUtil.notNull(param.getLoginIp())) {
            paramMap.put("loginIp", param.getLoginIp());
        }
        if (StringUtil.notNull(param.getAppModuleId())) {
            paramMap.put("appModuleId", param.getAppModuleId());
        }
        PageHelper.startPage(param.getPageNum(), param.getPageSize());
        PageList<LogUserOnline> onlineList = (PageList<LogUserOnline>) logUserOnlineMapper.listBySearch(paramMap);
        List<LogUserOnlineDto> dtoList = new ArrayList<>();
        for (LogUserOnline item : onlineList.getResult()) {
            LogUserOnlineDto dto = new LogUserOnlineDto();
            BeanUtils.copyProperties(item, dto);
            // 返回简短token，防止泄露
            dto.setToken(this.formatToken(item.getToken()));
            // TODO 补充机构信息
            dtoList.add(dto);
        }
        return PageResult.of(onlineList, dtoList);
    }

    @Override
    public boolean kickOutUser(String id) {
        LogUserOnline info = logUserOnlineMapper.selectById(id);
        if (info == null) {
            return false;
        }
        // 调用认证服务的退出方法，清空缓存，发布被踢下线消息
        // 当前在线记录的删除，通过下线事件的回调来触发
        authService.logout(info.getToken(), info.getAppKey(), SystemReturnCode.ACCOUNT_KICK_OUT);
        return true;
    }

    private String formatToken(String token) {
        if (StringUtil.isNull(token)) {
            return "";
        } else if (token.length() <= 16) {
            return token;
        } else {
            return token.substring(0, 16);
        }
    }
}
