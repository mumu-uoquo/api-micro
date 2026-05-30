package com.uoquo.scheduler.platform.remote;

import com.uoquo.scheduler.platform.model.param.MsgInfoReceiveParam;
import com.uoquo.scheduler.platform.model.param.MsgPushLogParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 消息远程服务
 * @author xuhz
 */
@FeignClient(name = "service-platform", path = "/health/api/platform", contextId = "MessageRemoteService")
public interface MessageRemoteService {

    /**
     * 添加消息接收信息
     */
    @RequestMapping(value = "/v1/message/manage/info/receivers/add")
    String addReceiver4Message(@RequestBody MsgInfoReceiveParam param);

    /**
     * 添加消息推送日志
     */
    @RequestMapping(value = "/v1/message/manage/info/push/logs/add")
    String addPushLog4Message(@RequestBody MsgPushLogParam param);

    /**
     * 下推消息：微信
     */
    @RequestMapping(value = "/v1/message/send/weixin")
    String send2Weixin();

    /**
     * 下推消息：APP
     */
    @RequestMapping(value = "/v1/message/send/app")
    String send2App();

    /**
     * 下推消息：短信
     */
    @RequestMapping(value = "/v1/message/send/sms")
    String send2Sms();
}
