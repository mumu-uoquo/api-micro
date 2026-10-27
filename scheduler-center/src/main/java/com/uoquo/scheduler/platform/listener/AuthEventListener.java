package com.uoquo.scheduler.platform.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.uoquo.cloud.events.RemoteEvent;
import com.uoquo.scheduler.common.BusinessOperationEnum;
import com.uoquo.scheduler.common.BusinessTypeEnum;
import com.uoquo.scheduler.common.DictionaryCodeEnum;
import com.uoquo.scheduler.platform.model.param.LogUserLoginParam;
import com.uoquo.scheduler.platform.model.param.LogUserLogoutParam;
import com.uoquo.scheduler.platform.model.pojo.AuthInfo;
import com.uoquo.scheduler.platform.model.pojo.SseMessage;
import com.uoquo.scheduler.platform.remote.LogsRemoteService;
import com.uoquo.utils.StringUtil;
import com.uoquo.utils.json.JsonUtil;
import com.uoquo.web.SystemReturnCode;
import com.uoquo.web.events.UoquoEventPublisher;

import jakarta.annotation.PostConstruct;

/**
 * 事件监听器：账户认证（009010） <br>
 * 只处理业务事件本身，记录操作日志等通用逻辑在{@link AllEventListener AllEventListener}中统一处理
 * @author xuhz
 */
@Component
public class AuthEventListener {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private UoquoEventPublisher eventPublisher;

    @Autowired
    private LogsRemoteService logsRemoteService;

    @PostConstruct
    public void setProperties(){
        logger.debug("AuthEventListener init ...");
    }

    /**
     * 监听事件：账户认证（009010）
     */
    @EventListener
    public void listenAuthEvent(RemoteEvent<AuthInfo> event) {
        // 重发消息需忽略（常见于运维重发）
        if (event.isRetry()) {
            logger.debug("重发的事件：{id:\"{}\", bizType:\"{}\", bizSub:\"{}\", bizId:\"{}\", opsType:\"{}\", opsId:\"{}\"}",
                    event.getId(), event.getBusinessType(), event.getBusinessSubType(), event.getBusinessId(), event.getOperationType(), event.getOperatorId());
            return;
        }
        // 正常消息处理
        AuthInfo info = event.getNewData();
        try {
            if (BusinessOperationEnum.LOGIN.getCode().equals(event.getOperationType())) {
                handleLoginEvent(event, info);
            } else if (BusinessOperationEnum.LOGOUT.getCode().equals(event.getOperationType())) {
                handleLogoutEvent(event, info);
            } else if (BusinessOperationEnum.REGISTER.getCode().equals(event.getOperationType())) {
                handleRegisterEvent(event, info);
            } else {
                handleOtherEvent(event, info);
            }
            if (logger.isDebugEnabled()) {
                logger.debug("处理账号[{}]事件[{}]完毕：{}", info.getAccount(), event.getOperationType(), JsonUtil.serialize(event));
            }
        } catch (Exception e) {
            logger.error("处理账号[{}]事件[{}]失败：{}\n", event.getOperatorId(), event.getOperationType(), JsonUtil.serialize(event), e);
        }
    }

    /**
     * 登录事件处理：记录登录日志
     */
    private void handleLoginEvent(RemoteEvent<AuthInfo> event, AuthInfo info) {
        LogUserLoginParam param = buildLogUserLoginParam(event, info);
        logsRemoteService.addLogs4Login(param);
        if (logger.isInfoEnabled()) {
            logger.info("处理账号[{}]登录事件：{}", info.getAccount(), JsonUtil.serialize(event));
        }
    }

    /**
     * 登出事件处理：
     * 1. 更新对应的登录记录，增加登出时间
     * 2. 如果是被踢下线的，还需要广播消息通知对应的客户端
     */
    private void handleLogoutEvent(RemoteEvent<AuthInfo> event, AuthInfo info) {
        // 1. 发送被踢下线（状态=02021）通知到在线设备（为了保证消息的及时性，所以优先执行发送逻辑，再记录登出日志）
        if (SystemReturnCode.ACCOUNT_KICK_OUT.getCode().equals(event.getOperationStatus())) {
            // 1.1 构建SSE事件信息
            RemoteEvent<SseMessage> sseEvent = new RemoteEvent<>(BusinessTypeEnum.SSE.getCode(), BusinessOperationEnum.MESSAGE_SEND.getCode(), SystemReturnCode.SUCCESS.getCode(), "service-platform");
            sseEvent.copy(event);
            // 1.2 具体的SSE信息
            SseMessage message = new SseMessage();
            // 基本信息
            message.setRecordId(null);
            message.setEventName("kickOut");
            message.setReceiverId(event.getBusinessId());
            message.setMessageId(null);
            message.setMessageType(DictionaryCodeEnum.MESSAGE_TYPE_NOTICE.getCode());
            message.setMessageLevel(DictionaryCodeEnum.LEVEL_IMPORTANT.getCode());
            // 发给指定客户端
            message.setTargetAppKey(event.getAppKey());
            // 被踢下线的会话
            message.addBusinessExtend("token", event.getToken());
            // 业务相关（与源事件一致）
            message.setBusinessId(event.getBusinessId());
            message.setBusinessType(event.getBusinessType());
            message.setBusinessSubType(event.getBusinessSubType());
            message.setOperationType(event.getOperationType());
            // 消息内容
            message.setMessageTitle("账号被踢下线");
            message.setMessageContent("您的账号在其他设备登录，当前会话将强制登出；若非本人操作，请立即重新登录修改密码。");
            // 1.3 发送消息（会根据sseEvent的appkey定向发送）
            sseEvent.setNewData(message);
            eventPublisher.publishEvent(sseEvent);
            logger.info("用户[{}]在设备[{}]上登录客户端[{}]，其他设备将被踢下线消息发送成功", event.getOperatorId(), event.getAppDeviceId(), event.getAppKey());
        }
        // 2. 记录退出日志
        LogUserLogoutParam param = buildLogUserLogoutParam(event);
        logsRemoteService.addLogs4Logout(param);
        if (logger.isInfoEnabled()) {
            logger.info("处理账号[{}]登出事件：{}", info.getAccount(), JsonUtil.serialize(event));
        }
    }

    /**
     * 注册事件处理：记录注册日志
     */
    private void handleRegisterEvent(RemoteEvent<AuthInfo> event, AuthInfo info) {
        if (logger.isInfoEnabled()) {
            logger.info("处理账号[{}]注册事件：{}", info.getAccount(), JsonUtil.serialize(event));
        }
    }

    /**
     * 其他事件处理：记录日志
     */
    private void handleOtherEvent(RemoteEvent<AuthInfo> event, AuthInfo info) {
        if (logger.isInfoEnabled()) {
            logger.info("处理账号[{}]事件[{}]：{}", info.getAccount(), event.getOperationType(), JsonUtil.serialize(event));
        }
    }

    /**
     * 构建参数：登录日志参数
     */
    private LogUserLoginParam buildLogUserLoginParam(RemoteEvent<AuthInfo> event, AuthInfo info){
        LogUserLoginParam param = new LogUserLoginParam();
//        BeanUtils.copyProperties(event, param);
        // 1. 基本信息
        param.setId(event.getId());
        param.setToken(event.getToken());
        param.setTraceId(event.getTraceId());
        param.setUserId(event.getBusinessId());
        param.setUserName(StringUtil.isNull(info.getAccount()) ? "unknown" : info.getAccount());
        param.setInstituteId(event.getOperatorInstituteId());
        param.setLoginIp(event.getAppIp());
//        // 根据IP转换为具体地址（放到应用层处理）
//        param.setLoginAddress();
        // 2. 登录设备
        param.setDeviceSn(event.getAppDeviceId());
        param.setDeviceOs(info.getDeviceOs());
        param.setDeviceUa(info.getDeviceUa());
        // 3. 授权信息（对应的模块由应用层补充）
        param.setAppKey(event.getAppKey());
        param.setAppVersion(event.getAppVersion());
//        param.setAppModuleId();
//        param.setAppModuleName();
        // 4. 登录状态
        param.setLoginStatus(event.getOperationStatus());
        param.setLoginTime(event.getOperationTime());
        param.setLoginParam(event.getExtension());
        param.setLoginMode(info.getLoginMode());
        param.setDescription(event.getRemarks());
        return param;
    }

    /**
     * 构建参数：登出日志参数
     */
    private LogUserLogoutParam buildLogUserLogoutParam(RemoteEvent<AuthInfo> event){
        LogUserLogoutParam param = new LogUserLogoutParam();
        param.setToken(event.getToken());
        param.setAppKey(event.getAppKey());
        param.setTraceId(event.getTraceId());
        param.setUserId(event.getBusinessId());
        param.setLogoutStatus(event.getOperationStatus());
        param.setLogoutTime(event.getOperationTime());
        param.setLogoutDesc(event.getRemarks());
        return param;
    }

}
