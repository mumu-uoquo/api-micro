package com.uoquo.platform.logs.service.impl;

import com.uoquo.platform.common.exception.AccountReturnCode;
import com.uoquo.platform.common.utils.UserUtils;
import com.uoquo.platform.logs.mapper.LogUserLoginMapper;
import com.uoquo.platform.logs.model.dto.LogUserLoginDto;
import com.uoquo.platform.logs.model.param.LogUserLoginParam;
import com.uoquo.platform.logs.model.param.LogsAuthSearchParam;
import com.uoquo.platform.logs.model.param.LogUserLogoutParam;
import com.uoquo.platform.logs.model.pojo.LogUserLogin;
import com.uoquo.platform.logs.service.LogsAuthService;
import com.uoquo.platform.logs.service.OnlineUserService;
import com.uoquo.platform.role.mapper.ModuleInfoMapper;
import com.uoquo.platform.role.model.pojo.ModuleInfo;
import com.uoquo.platform.system.mapper.AppInfoMapper;
import com.uoquo.platform.system.model.pojo.AppInfo;
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

import java.util.*;

@Service
public class LogsAuthServiceImpl implements LogsAuthService {
    final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private LogUserLoginMapper logUserLoginMapper;

    @Autowired
    private OnlineUserService onlineUserService;

    @Autowired
    private AppInfoMapper appInfoMapper;

    @Autowired
    private ModuleInfoMapper moduleInfoMapper;

    @Override
    public String addLogsInfo(LogUserLoginParam param) {
        // 1. 登录成功时，需对之前未“正常退出”的记录标记为“异常退出”
        if (SystemReturnCode.SUCCESS.getCode().equals(param.getLoginStatus())) {
            LogUserLogin info = new LogUserLogin();
            info.setUserId(param.getUserId());
            info.setLoginStatus(SystemReturnCode.SUCCESS.getCode());
            info.setLogoutStatus(AccountReturnCode.ABNORMAL_LOGOUT.getCode());
            info.setLogoutTime(new Date());
            info.setLogoutDesc("自动标记");
            logUserLoginMapper.updateLogout4User(info);
        }
        // 2. 记录登录日志
        LogUserLogin info = new LogUserLogin();
        BeanUtils.copyProperties(param, info);
        if (StringUtil.isNull(info.getId())) {
            info.setId(IDGenerator.getNextULID());
        }
        info.setToken(UserUtils.formatToken(param.getToken()));
        info.setLoginAddress(this.formatAddress(param.getLoginIp()));
        // 在登录不成功的情况下，userId为空，为了后续兼容分表策略，因此赋予默认值
        if (StringUtil.isNull(info.getUserId())) {
            info.setUserId("unknown");
        }
        if (StringUtil.isNull(info.getInstituteId())) {
            info.setInstituteId("unknown");
        }
        // 根据 appkey 补充 appModuleId 和 appModuleName
        if (StringUtil.notNull(info.getAppKey())) {
            AppInfo appInfo = appInfoMapper.selectByAppkey(info.getAppKey());
            if (appInfo != null) {
                info.setAppName(appInfo.getAppName());
                info.setAppModuleId(appInfo.getModuleId());
                // 根据 moduleId 查询 sys_module 表，获取模块名称
                if (StringUtil.notNull(appInfo.getModuleId())) {
                    ModuleInfo moduleInfo = moduleInfoMapper.selectByPrimaryKey(appInfo.getModuleId());
                    info.setAppModuleName((moduleInfo != null) ? moduleInfo.getModuleName() : null);
                }
            }
        }
        logUserLoginMapper.insert(info);
        // 3. 记录在线用户信息
        try {
            onlineUserService.upsertOnlineUser(info, param.getToken());
        } catch (Exception e) {
            logger.error("upsert 在线用户记录失败，userId={}, appKey={}", param.getUserId(), param.getAppKey(), e);
        }

        return info.getId();
    }

    @Override
    public String updateLogoutInfo(LogUserLogoutParam param) {
        // 1. 记录登出日志
        LogUserLogin info = new LogUserLogin();
        info.setToken(UserUtils.formatToken(param.getToken()));
        info.setLogoutStatus(param.getLogoutStatus());
        info.setLogoutTime(param.getLogoutTime());
        info.setLogoutDesc(param.getLogoutDesc());
        // 仅当传入了token才能更新
        int count = 0;
        if (StringUtil.notNull(info.getToken())) {
            count = logUserLoginMapper.updateLogout4Token(info);
        }
        if (count <= 0) {
            logger.warn("token[{}]对应的认证日志不存在，本次登出日志：{}", info.getToken(), JsonUtil.serialize(param));
        }
        // 2. 删除在线用户信息
        try {
            onlineUserService.removeOnlineUser(param.getUserId(), param.getAppKey());
        } catch (Exception e) {
            logger.error("删除在线用户记录失败，userId={}, appKey={}, token={}", param.getUserId(), param.getAppKey(), param.getToken(), e);
        }
        return info.getId();
    }

    @Override
    public PageResult<LogUserLoginDto> listBySearch(LogsAuthSearchParam param) {
        // 分页查询
        Map<String, Object> paramMap = new HashMap<>();
        if (StringUtil.notNull(param.getUserId())) {
            paramMap.put("userId", param.getUserId());
        }
        if (StringUtil.notNull(param.getToken())) {
            paramMap.put("token", param.getToken());
        }
        if (StringUtil.notNull(param.getUserName())) {
            paramMap.put("userName", param.getUserName());
        }
        if (StringUtil.notNull(param.getLoginIp())) {
            paramMap.put("loginIp", param.getLoginIp());
        }
        if (StringUtil.notNull(param.getLoginStatus())) {
            paramMap.put("loginStatus", param.getLoginStatus());
        }
        if (StringUtil.notNull(param.getInstituteId())) {
            paramMap.put("instituteId", param.getInstituteId());
        }
        if (param.getLoginTimeStart() != null) {
            paramMap.put("loginTimeStart", param.getLoginTimeStart());
            paramMap.put("loginTimeEnd", param.getLoginTimeEnd() == null ? new Date() : param.getLoginTimeEnd());
        }
        PageHelper.startPage(param.getPageNum(), param.getPageSize());
        PageList<LogUserLogin> list = (PageList<LogUserLogin>) logUserLoginMapper.listBySearch(paramMap);
        // 对象转换
        List<LogUserLoginDto> resultList = new ArrayList<>();
        for (LogUserLogin item : list.getResult()) {
            // TODO 补充机构信息
            resultList.add(this.convert2Dto(item));
        }
        return PageResult.of(list, resultList);
    }

    @Override
    public LogUserLoginDto getInfoById(String id) {
        LogUserLogin info = logUserLoginMapper.selectByPrimaryKey(id);
        if (info == null ) {
            return null;
        }
        LogUserLoginDto dto = new LogUserLoginDto();
        BeanUtils.copyProperties(info, dto);
        return dto;
    }

    /**
     *  格式化处理IP对应的地理位置
     */
    private String formatAddress(String ip) {
        if (StringUtil.isNull(ip)) {
            return "";
        }
        // TODO 根据IP转换为地理位置
        return ip;
    }

    private LogUserLoginDto convert2Dto(LogUserLogin info) {
        LogUserLoginDto dto = new LogUserLoginDto();
        BeanUtils.copyProperties(info, dto);
        return dto;
    }
}
