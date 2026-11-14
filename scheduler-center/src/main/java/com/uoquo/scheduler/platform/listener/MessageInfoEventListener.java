package com.uoquo.scheduler.platform.listener;

import com.uoquo.cloud.events.RemoteEvent;
import com.uoquo.scheduler.common.BusinessOperationEnum;
import com.uoquo.scheduler.common.BusinessTypeEnum;
import com.uoquo.scheduler.common.DictionaryCodeEnum;
import com.uoquo.scheduler.platform.model.dto.UserInfoDto;
import com.uoquo.scheduler.platform.model.param.MsgInfoReceiveBatchParam;
import com.uoquo.scheduler.platform.model.param.MsgPushLogBatchParam;
import com.uoquo.scheduler.platform.model.param.MsgPushLogParam;
import com.uoquo.scheduler.platform.model.param.UserListByRangeParam;
import com.uoquo.scheduler.platform.model.pojo.MsgInfo;
import com.uoquo.scheduler.platform.model.pojo.SseMessage;
import com.uoquo.scheduler.platform.remote.MessageRemoteService;
import com.uoquo.scheduler.platform.remote.UserRemoteService;
import com.uoquo.utils.Config;
import com.uoquo.utils.StringUtil;
import com.uoquo.utils.json.JsonUtil;
import com.uoquo.web.SystemReturnCode;
import com.uoquo.web.events.EventContext;
import com.uoquo.web.events.UoquoEventPublisher;
import com.uoquo.web.param.IdParam;
import com.uoquo.mybatis.page.PageResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 事件监听器：消息发布（009030） <br>
 * 只处理业务事件本身，记录操作日志等通用逻辑在{@link AllEventListener AllEventListener}中统一处理 <br>
 * 处理方式：按页（每页最多1000个用户）批量处理，每页的远程调用次数固定，与用户数无关：
 * <ol>
 *   <li>批量添加接收记录（1次，幂等）</li>
 *   <li>按指定方式推送（微信、APP、短信，仍是逐个用户调用，三方渠道实现后再改为批量）</li>
 *   <li>推送站内消息：整页只发布1条SSE事件，由各节点自行匹配本地连接</li>
 *   <li>批量添加推送日志（1次）</li>
 * </ol>
 * @author xuhz
 */
@Component
public class MessageInfoEventListener {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    /** 推送结果（push_result）字段的最大长度 */
    private static final int PUSH_RESULT_MAX_LENGTH = 500;

    /** 每页查询的用户数（同时是批量接口的单次上限） */
    private static final int PAGE_SIZE = 1000;

    @Autowired
    private UoquoEventPublisher eventPublisher;

    @Autowired
    private MessageRemoteService messageRemoteService;

    @Autowired
    private UserRemoteService userRemoteService;

    /**
     * 群发专用线程池：线程数由 app.message.fanout.pool-size 配置（默认2），队列长度由 app.message.fanout.queue-capacity 配置（默认100）。
     * 队列满时由提交线程自己执行（背压，不丢消息）。
     */
    private ThreadPoolExecutor fanoutExecutor;

    @PostConstruct
    public void setProperties(){
        int size = Math.max(1, Config.getInt("app.message.fanout.pool-size", 2));
        int queue = Math.max(1, Config.getInt("app.message.fanout.queue-capacity", 100));
        AtomicInteger index = new AtomicInteger();
        fanoutExecutor = new ThreadPoolExecutor(size, size, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(queue),
                r -> new Thread(r, "message-fanout-" + index.incrementAndGet()),
                new ThreadPoolExecutor.CallerRunsPolicy());
        fanoutExecutor.allowCoreThreadTimeOut(true);
        logger.debug("MessageInfoEventListener init ... fanout pool size={}, queue={}", size, queue);
    }

    @PreDestroy
    public void shutdown() {
        if (fanoutExecutor == null) {
            return;
        }
        // 等待正在执行的群发（最长10秒），超时则中断，未处理完的接收人不会继续推送
        fanoutExecutor.shutdown();
        try {
            if (!fanoutExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                logger.warn("群发线程池未能在规定时间内结束，丢弃未执行的任务数[{}]", fanoutExecutor.shutdownNow().size());
            }
        } catch (InterruptedException e) {
            fanoutExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 监听事件：消息发布（009030）
     */
    @EventListener
    public void listenMessageInfoEvent(RemoteEvent<MsgInfo> event) {
        // 重发消息需忽略（常见于运维重发）
        if (event.isRetry()) {
            logger.debug("重发的事件：{eventId:\"{}\", bizType:\"{}\", bizSub:\"{}\", bizId:\"{}\", opsType:\"{}\", opsId:\"{}\"}",
                    event.getEventId(), event.getBusinessType(), event.getBusinessSubType(), event.getBusinessId(), event.getOperationType(), event.getOperatorId());
            return;
        }
        // 只需要处理发布事件，其他操作仅需记日志
        if (!BusinessOperationEnum.MESSAGE_PUBLISH.getCode().equals(event.getOperationType())) {
            return;
        }
        // 群发可能耗时较长（接收人多时按页循环），交给专用线程池处理，立即释放事件线程池的线程，
        // 避免与踢人、日志等实时事件抢占同一个线程池（队列满时由当前线程执行，形成背压）
        String requestId = MDC.get("requestId");
        fanoutExecutor.execute(() -> fanOut(event, requestId));
    }

    /**
     * 群发：按页查询接收人并推送。在专用线程池中执行，需自行恢复事件的用户上下文。
     */
    private void fanOut(RemoteEvent<MsgInfo> event, String requestId) {
        String oldRequestId = MDC.get("requestId");
        EventContext.Snapshot snapshot = EventContext.capture();
        try {
            if (StringUtil.notNull(requestId)) {
                MDC.put("requestId", requestId);
            }
            // 用事件携带的发起人信息填充用户上下文（Feign 请求头需要）
            EventContext.apply(event);
            MsgInfo message = event.getNewData();
            // 查询指定范围的用户
            UserListByRangeParam param = new UserListByRangeParam();
            param.setPageNum(1);
            param.setPageSize(PAGE_SIZE);
            param.setReceiverRange(message.getReceiverRange());
            param.setReceiverInstituteId(message.getReceiverInstituteId());
            if (StringUtil.notNull(message.getReceiverIds())) {
                param.setReceiverIds(List.of(message.getReceiverIds().split(",")));
            }
            int total = 0;
            PageResult<UserInfoDto> result = userRemoteService.listUserByRange(param);
            while (result.getSize() > 0) {
                total += result.getResult().size();
                this.pushMessage2Users(event, message, result.getResult());
                // 若还有下一页，则继续请求
                if (result.isNextPage()) {
                    param.setPageNum(param.getPageNum() + 1);
                    result = userRemoteService.listUserByRange(param);
                } else {
                    break;
                }
            }
            logger.info("账号[{}]发布消息[{}]完毕，接收人数[{}]", event.getOperatorId(), event.getBusinessId(), total);
            if (logger.isDebugEnabled()) {
                logger.debug("账号[{}]发布消息[{}]完毕：{}", event.getOperatorId(), event.getBusinessId(), JsonUtil.serialize(event));
            }
        } catch (Exception e) {
            logger.error("账号[{}]发布消息[{}]失败：", event.getOperatorId(), event.getBusinessId(), e);
        } finally {
            snapshot.restore();
            if (StringUtil.notNull(oldRequestId)) {
                MDC.put("requestId", oldRequestId);
            } else {
                MDC.remove("requestId");
            }
        }
    }

    /**
     * 处理一页用户：批量保存接收记录 → 渠道推送 → 站内推送（1条SSE事件）→ 批量保存推送日志。<br>
     * 单页失败只记录日志，不影响后续页。
     */
    private void pushMessage2Users(RemoteEvent<MsgInfo> event, MsgInfo message, List<UserInfoDto> users) {
        try {
            // 1. 批量添加接收记录（幂等）：receiverId → recordId
            Map<String, String> recordIds = this.saveReceivers4Message(message.getId(), users);
            // 2. 按指定方式推送（接收人已删除记录的用户不推送）
            Map<String, String> failures = new HashMap<>();
            List<UserInfoDto> receivers = new ArrayList<>(users.size());
            for (UserInfoDto user : users) {
                String recordId = recordIds.get(user.getId());
                if (recordId == null) {
                    continue;
                }
                receivers.add(user);
                String failure = this.pushByChannel(message, recordId);
                if (failure != null) {
                    failures.put(user.getId(), failure);
                }
            }
            if (receivers.isEmpty()) {
                return;
            }
            // 3. 推送站内消息（整页一条SSE事件）
            String webFailure = this.sendMessage2Web(event, message, recordIds);
            // 4. 批量记录推送日志
            this.savePushLogs4Message(message, receivers, failures, webFailure);
            logger.debug("将消息[{}]以方式[{}]给[{}]个用户推送完毕", message.getId(), message.getPushWay(), receivers.size());
        } catch (Exception e) {
            logger.error("将消息[{}]以方式[{}]给[{}]个用户推送失败：", message.getId(), message.getPushWay(), users.size(), e);
        }
    }

    /**
     * 批量添加记录：接收信息
     * @return receiverId → 接收记录ID
     */
    private Map<String, String> saveReceivers4Message(String messageId, List<UserInfoDto> users) {
        MsgInfoReceiveBatchParam param = new MsgInfoReceiveBatchParam();
        param.setMessageId(messageId);
        for (UserInfoDto user : users) {
            param.addReceiver(user.getId(), user.getUserName());
        }
        Map<String, String> recordIds = messageRemoteService.addReceivers4Message(param);
        return recordIds == null ? new HashMap<>() : recordIds;
    }

    /**
     * 批量添加记录：推送日志
     * @param failures   渠道推送失败的用户：receiverId → 失败原因
     * @param webFailure 站内推送的失败原因（为空表示成功）
     */
    private void savePushLogs4Message(MsgInfo message, List<UserInfoDto> receivers, Map<String, String> failures, String webFailure) {
        Date pushTime = new Date();
        MsgPushLogBatchParam param = new MsgPushLogBatchParam();
        for (UserInfoDto user : receivers) {
            // 推送结果：为空表示成功，否则为失败原因（渠道失败 + 站内失败）
            String failure = this.joinFailure(failures.get(user.getId()), webFailure);
            MsgPushLogParam log = new MsgPushLogParam();
            // 消息信息
            log.setMessageId(message.getId());
            log.setPushWay(message.getPushWay());
            // 接收信息
            log.setReceiverId(user.getId());
            log.setReceiverName(user.getUserName());
            // 推送情况
            log.setPushStatus(failure == null ? DictionaryCodeEnum.PUSH_STATUS_PUSHED.getCode() : DictionaryCodeEnum.PUSH_STATUS_FAILED.getCode());
            log.setPushResult(failure);
            log.setPushTime(pushTime);
            param.getLogs().add(log);
        }
        messageRemoteService.addPushLogs4Message(param);
    }

    /**
     * 按指定渠道（微信、APP、短信）推送，站内消息不在此处理。
     * @param recordId 接收记录ID
     * @return 失败原因；为空表示成功（或无需渠道推送）
     */
    private String pushByChannel(MsgInfo message, String recordId) {
        String pushWay = message.getPushWay();
        try {
            if (DictionaryCodeEnum.PUSH_TYPE_WX.getCode().equals(pushWay)) {
                // TODO 微信推送（实现后应改为批量）
                messageRemoteService.send2Weixin(new IdParam(recordId));
            } else if (DictionaryCodeEnum.PUSH_TYPE_APP.getCode().equals(pushWay)) {
                // TODO APP推送（实现后应改为批量）
                messageRemoteService.send2App(new IdParam(recordId));
            } else if (DictionaryCodeEnum.PUSH_TYPE_SMS.getCode().equals(pushWay)) {
                // TODO 短信推送（实现后应改为批量）
                messageRemoteService.send2Sms(new IdParam(recordId));
            }
            return null;
        } catch (Exception e) {
            logger.warn("将消息[{}]以方式[{}]推送给接收记录[{}]失败：{}", message.getId(), pushWay, recordId, e.getMessage());
            return StringUtil.isNull(e.getMessage()) ? e.getClass().getSimpleName() : e.getMessage();
        }
    }

    /**
     * 消息推送：站内（整页只发布1条SSE事件，由service-platform的各节点匹配本地连接后推送）
     * @param recordIds receiverId → 接收记录ID
     * @return 失败原因；为空表示成功
     */
    private String sendMessage2Web(RemoteEvent<MsgInfo> event, MsgInfo info, Map<String, String> recordIds) {
        try {
            // 1.1 构建SSE事件信息
            RemoteEvent<SseMessage> sseEvent = new RemoteEvent<>(BusinessTypeEnum.SSE.getCode(), BusinessOperationEnum.MESSAGE_SEND.getCode(), SystemReturnCode.SUCCESS.getCode(), null, "service-platform");
            sseEvent.copy(event);
            // 由于是内部发送的，不需要token和traceId
            sseEvent.setToken(null);
            sseEvent.setTraceId(null);
            // 1.2 具体的SSE信息
            SseMessage message = new SseMessage();
            // 基本信息：接收人由 receiverRecords 指定，每个用户的 recordId 在投递时由 service-platform 填充
            message.setReceiverRecords(new LinkedHashMap<>(recordIds));
            message.setMessageId(info.getId());
            message.setMessageType(info.getMessageType());
            message.setMessageLevel(info.getMessageLevel());
            // 发给所有客户端
            message.setTargetAppKey(null);
            // 业务相关（与源事件一致）
            message.setBusinessId(event.getBusinessId());
            message.setBusinessType(event.getBusinessType());
            message.setBusinessSubType(event.getBusinessSubType());
            message.setOperationType(event.getOperationType());
            // 消息内容
            message.setMessageTitle(info.getMessageTitle());
            message.setMessageContent(info.getMessageContent());
            message.setBusinessExtend(info.getBusinessExtend());
            // 1.3 发送消息
            sseEvent.setNewData(message);
            eventPublisher.publishEvent(sseEvent);
            logger.debug("消息[{}]的站内推送事件发布成功，接收人数[{}]", info.getId(), recordIds.size());
            return null;
        } catch (Exception e) {
            logger.error("消息[{}]的站内推送事件发布失败：", info.getId(), e);
            return StringUtil.isNull(e.getMessage()) ? e.getClass().getSimpleName() : e.getMessage();
        }
    }

    /**
     * 合并渠道、站内的失败原因（都成功时返回null），并限制长度
     */
    private String joinFailure(String channelFailure, String webFailure) {
        String failure;
        if (channelFailure == null) {
            failure = webFailure;
        } else if (webFailure == null) {
            failure = channelFailure;
        } else {
            failure = channelFailure + "；" + webFailure;
        }
        if (failure != null && failure.length() > PUSH_RESULT_MAX_LENGTH) {
            failure = failure.substring(0, PUSH_RESULT_MAX_LENGTH);
        }
        return failure;
    }

}
