package com.uoquo.scheduler.platform.remote;

import java.util.Map;

import com.uoquo.scheduler.platform.model.param.MsgInfoReceiveBatchParam;
import com.uoquo.scheduler.platform.model.param.MsgInfoReceiveParam;
import com.uoquo.scheduler.platform.model.param.MsgPushLogBatchParam;
import com.uoquo.scheduler.platform.model.param.MsgPushLogParam;
import com.uoquo.web.param.IdParam;
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
     * 批量添加消息接收信息（幂等，单次最多1000个接收人）
     * @return receiverId → 接收记录ID（已被接收人删除的记录不返回）
     */
    @RequestMapping(value = "/v1/message/manage/info/receivers/batch/add")
    Map<String, String> addReceivers4Message(@RequestBody MsgInfoReceiveBatchParam param);

    /**
     * 添加消息推送日志
     */
    @RequestMapping(value = "/v1/message/manage/info/push/logs/add")
    String addPushLog4Message(@RequestBody MsgPushLogParam param);

    /**
     * 批量添加消息推送日志（仅首次推送，单次最多1000条）
     * @return 新增条数
     */
    @RequestMapping(value = "/v1/message/manage/info/push/logs/batch/add")
    Integer addPushLogs4Message(@RequestBody MsgPushLogBatchParam param);

    /**
     * 下推消息：微信
     * @param param 接收记录ID
     */
    @RequestMapping(value = "/v1/message/send/weixin")
    String send2Weixin(@RequestBody IdParam param);

    /**
     * 下推消息：APP
     * @param param 接收记录ID
     */
    @RequestMapping(value = "/v1/message/send/app")
    String send2App(@RequestBody IdParam param);

    /**
     * 下推消息：短信
     * @param param 接收记录ID
     */
    @RequestMapping(value = "/v1/message/send/sms")
    String send2Sms(@RequestBody IdParam param);
}
