package com.uoquo.scheduler.platform.listener;

import com.uoquo.cloud.events.RemoteEvent;
import com.uoquo.scheduler.common.BusinessOperationEnum;
import com.uoquo.scheduler.common.BusinessTypeEnum;
import com.uoquo.scheduler.common.DictionaryCodeEnum;
import com.uoquo.scheduler.platform.model.dto.MsgFanoutDto;
import com.uoquo.scheduler.platform.model.dto.UserInfoDto;
import com.uoquo.scheduler.platform.model.param.MsgFanoutParam;
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
            // 1. 领取群发任务（CAS）：已完成、正在被其他节点执行、已撤回/删除的消息领取失败，直接结束
            MsgFanoutParam claimParam = new MsgFanoutParam();
            claimParam.setMessageId(message.getId());
            claimParam.setStaleSeconds(fanoutStaleSeconds());
            claimParam.setMaxRetry(fanoutMaxRetry());
            MsgFanoutDto claim = messageRemoteService.claimFanout(claimParam);
            if (claim == null || !claim.isClaimed()) {
                logger.info("消息[{}]的群发任务无需执行（已完成、正在执行、已撤回或接管次数已用完）", message.getId());
                return;
            }
            // 2. 从上次处理的位置（游标）继续，按接收人ID排序，每批 PAGE_SIZE 个
            String cursor = claim.getCursor() == null ? "" : claim.getCursor();
            int total = claim.getTotal();
            if (StringUtil.notNull(cursor)) {
                logger.info("消息[{}]的群发任务从中断处继续：已处理[{}]人，游标[{}]", message.getId(), total, cursor);
            }
            UserListByRangeParam param = new UserListByRangeParam();
            param.setPageSize(PAGE_SIZE);
            param.setReceiverRange(message.getReceiverRange());
            param.setReceiverInstituteId(message.getReceiverInstituteId());
            if (StringUtil.notNull(message.getReceiverIds())) {
                param.setReceiverIds(List.of(message.getReceiverIds().split(",")));
            }
            while (true) {
                param.setAfterId(cursor);
                PageResult<UserInfoDto> result = userRemoteService.listUserByRange(param);
                List<UserInfoDto> users = result == null ? null : result.getResult();
                if (users == null || users.isEmpty()) {
                    // 没有更多接收人，群发结束
                    if (this.updateFanoutProgress(message, claim, cursor, total, true)) {
                        this.notifyAllStaff(event, message);
                    }
                    break;
                }
                if (!this.pushMessage2Users(event, message, claim, users)) {
                    // 本批失败：不推进游标，等待定时任务发现进度停滞后接管，从这一批重新开始（接收记录幂等，不会重复）
                    logger.error("消息[{}]的群发在游标[{}]之后的一批失败，已暂停，稍后由定时任务接管继续", message.getId(), cursor);
                    return;
                }
                cursor = users.get(users.size() - 1).getId();
                total += users.size();
                boolean finished = users.size() < PAGE_SIZE;
                // 每批处理完更新进度；同时也是心跳，并检查消息是否已被撤回/删除、任务是否已被其他节点接管
                if (!this.updateFanoutProgress(message, claim, cursor, total, finished)) {
                    logger.warn("消息[{}]的群发已停止：消息已被撤回/删除，或任务已被其他节点接管，已处理[{}]人", message.getId(), total);
                    return;
                }
                if (finished) {
                    this.notifyAllStaff(event, message);
                    break;
                }
            }
            logger.info("账号[{}]发布消息[{}]完毕，接收人数[{}]", event.getOperatorId(), event.getBusinessId(), total);
            if (logger.isDebugEnabled()) {
                logger.debug("账号[{}]发布消息[{}]完毕：{}", event.getOperatorId(), event.getBusinessId(), JsonUtil.serialize(event));
            }
        } catch (FanoutStoppedException e) {
            logger.warn("{}：消息已被撤回/删除，或任务已被其他节点接管", e.getMessage());
        } catch (Exception e) {
            // 异常时进度停留在上一批，由定时任务接管继续
            logger.error("账号[{}]发布消息[{}]失败（稍后由定时任务接管继续）：", event.getOperatorId(), event.getBusinessId(), e);
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
     * 继续群发（由定时任务调用）：处理已发布，但群发没有开始或中断的消息。<br>
     * 没有原始事件，这里构造一个等价的事件（站内推送事件需要复制其业务信息）；是否真正执行由领取任务（CAS）决定。
     */
    public void resume(MsgInfo message) {
        RemoteEvent<MsgInfo> event = new RemoteEvent<>(BusinessTypeEnum.MESSAGE.getCode(),
                BusinessOperationEnum.MESSAGE_PUBLISH.getCode(), SystemReturnCode.SUCCESS.getCode());
        event.setBusinessSubType("MESSAGE");
        event.setBusinessId(message.getId());
        event.setOperatorId(message.getSenderId());
        event.setOperatorName(message.getSenderName());
        event.setNewData(message);
        fanoutExecutor.execute(() -> fanOut(event, MDC.get("requestId")));
    }

    /**
     * 中断判定时间（秒）：群发进度超过该时间没有更新，视为已中断，由定时任务接管。<br>
     * 可通过 app.message.fanout.stale-seconds 配置（默认300，最小120：设置过小容易把执行较慢的群发误判为中断，造成重复推送）。
     */
    public static int fanoutStaleSeconds() {
        return Math.max(120, Config.getInt("app.message.fanout.stale-seconds", 300));
    }

    /**
     * 群发被主动停止（消息已被撤回/删除，或任务已被其他节点接管），用于从批次处理的中途退出
     */
    private static class FanoutStoppedException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        FanoutStoppedException(String message) {
            super(message);
        }
    }

    /**
     * 群发心跳：处理一批的过程中定期更新进度时间，避免耗时较长的批次被误判为中断。<br>
     * 只有明确返回“已停止”才返回 false；网络等异常只记录日志（下次心跳或批次结束时再更新）。
     */
    private boolean heartbeatFanout(MsgInfo message, MsgFanoutDto claim) {
        try {
            MsgFanoutParam param = new MsgFanoutParam();
            param.setMessageId(message.getId());
            param.setRetry(claim.getRetry());
            return Boolean.TRUE.equals(messageRemoteService.heartbeatFanout(param));
        } catch (Exception e) {
            logger.warn("消息[{}]的群发心跳失败：{}", message.getId(), e.getMessage());
            return true;
        }
    }

    /** 群发中断后的最大接管次数 */
    public static int fanoutMaxRetry() {
        return Math.max(1, Config.getInt("app.message.fanout.max-retry", 5));
    }

    /**
     * 更新群发进度
     * @return false 表示消息已被撤回/删除，或任务已被其他节点接管，应停止群发
     */
    private boolean updateFanoutProgress(MsgInfo message, MsgFanoutDto claim, String cursor, int total, boolean finished) {
        MsgFanoutParam param = new MsgFanoutParam();
        param.setMessageId(message.getId());
        param.setRetry(claim.getRetry());
        param.setCursor(cursor);
        param.setTotal(total);
        param.setFinished(finished);
        return Boolean.TRUE.equals(messageRemoteService.updateFanoutProgress(param));
    }

    /**
     * 处理一页用户：批量保存接收记录 → 渠道推送 → 站内推送（1条SSE事件）→ 批量保存推送日志。<br>
     * 接收记录是幂等的，因此整页可以安全重放。
     * @return true 表示本页已处理（可以推进游标）；false 表示本页失败（不能推进游标，需要重试）
     * @throws FanoutStoppedException 心跳时发现群发已被停止（撤回、删除或被其他节点接管）
     */
    private boolean pushMessage2Users(RemoteEvent<MsgInfo> event, MsgInfo message, MsgFanoutDto claim, List<UserInfoDto> users) {
        try {
            // 1. 批量添加接收记录（幂等）：receiverId → recordId
            Map<String, String> recordIds = this.saveReceivers4Message(message.getId(), users);
            // 2. 按指定方式推送（接收人已删除记录的用户不推送）
            Map<String, String> failures = new HashMap<>();
            List<UserInfoDto> receivers = new ArrayList<>(users.size());
            // 渠道推送是逐个用户调用的，一批可能耗时较长，所以处理过程中定期发送心跳（间隔为中断判定时间的1/5）
            long heartbeatInterval = fanoutStaleSeconds() * 1000L / 5;
            long lastHeartbeat = System.currentTimeMillis();
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
                if (System.currentTimeMillis() - lastHeartbeat >= heartbeatInterval) {
                    if (!this.heartbeatFanout(message, claim)) {
                        throw new FanoutStoppedException("消息[" + message.getId() + "]的群发已被停止");
                    }
                    lastHeartbeat = System.currentTimeMillis();
                }
            }
            if (receivers.isEmpty()) {
                return true;
            }
            // 3. 推送站内消息：普通消息每页一条定向SSE事件；全员消息不按页推送，群发结束后统一广播一次（见 notifyAllStaff）
            boolean allStaff = isAllStaff(message);
            String webFailure = allStaff ? null : this.sendMessage2Web(event, message, recordIds);
            // 4. 批量记录推送日志（日志只是辅助信息，保存失败不影响群发进度）。全员消息只记录失败的用户，成功的不逐条记录
            try {
                this.savePushLogs4Message(message, receivers, failures, webFailure, allStaff);
            } catch (Exception e) {
                logger.warn("保存消息[{}]的推送日志失败（[{}]个用户）：{}", message.getId(), receivers.size(), e.getMessage());
            }
            if (allStaff) {
                logger.info("全员消息[{}]本批接收人[{}]个，渠道推送失败[{}]个", message.getId(), receivers.size(), failures.size());
            }
            logger.debug("将消息[{}]以方式[{}]给[{}]个用户推送完毕", message.getId(), message.getPushWay(), receivers.size());
            return true;
        } catch (FanoutStoppedException e) {
            // 群发已被停止，交给调用方结束整个群发
            throw e;
        } catch (Exception e) {
            logger.error("将消息[{}]以方式[{}]给[{}]个用户推送失败：", message.getId(), message.getPushWay(), users.size(), e);
            return false;
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
     * @param failures      渠道推送失败的用户：receiverId → 失败原因
     * @param webFailure    站内推送的失败原因（为空表示成功）
     * @param onlyFailures  为 true 时只记录失败的用户（全员消息使用，成功的用户不逐条记录）
     */
    private void savePushLogs4Message(MsgInfo message, List<UserInfoDto> receivers, Map<String, String> failures, String webFailure, boolean onlyFailures) {
        Date pushTime = new Date();
        MsgPushLogBatchParam param = new MsgPushLogBatchParam();
        for (UserInfoDto user : receivers) {
            // 推送结果：为空表示成功，否则为失败原因（渠道失败 + 站内失败）
            String failure = this.joinFailure(failures.get(user.getId()), webFailure);
            if (onlyFailures && failure == null) {
                continue;
            }
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
        // 没有需要记录的日志时不调用（批量接口要求至少一条）
        if (param.getLogs().isEmpty()) {
            return;
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
     * @param recordIds receiverId → 接收记录ID；为 null 时表示全员广播（不带接收人）
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
            if (recordIds == null) {
                // 全员广播：不带接收人，由 service-platform 发送给所有在线连接
                message.setBroadcast(true);
            } else {
                message.setReceiverRecords(new LinkedHashMap<>(recordIds));
            }
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
            logger.debug("消息[{}]的站内推送事件发布成功，接收人数[{}]", info.getId(), recordIds == null ? "全员" : recordIds.size());
            return null;
        } catch (Exception e) {
            logger.error("消息[{}]的站内推送事件发布失败：", info.getId(), e);
            return StringUtil.isNull(e.getMessage()) ? e.getClass().getSimpleName() : e.getMessage();
        }
    }

    /**
     * 是否为全员消息（发布范围为所有人员）：这类消息不按页推送站内消息，群发结束后统一广播一次
     */
    private static boolean isAllStaff(MsgInfo message) {
        return DictionaryCodeEnum.PUBLISH_RANGE_ALL.getCode().equals(message.getReceiverRange());
    }

    /**
     * 全员消息群发结束后发布一次全员广播。<br>
     * 只在群发状态更新为“已完成”之后调用：若任务已被撤回或被其他节点接管，则不会广播。
     * 注意：广播与“已完成”之间若服务异常中断，广播可能丢失（不会重放），这是为换取不重复广播所做的取舍。
     */
    private void notifyAllStaff(RemoteEvent<MsgInfo> event, MsgInfo message) {
        if (!isAllStaff(message)) {
            return;
        }
        String failure = sendMessage2Web(event, message, null);
        logger.info("全员消息[{}]群发完毕，站内广播{}", message.getId(), failure == null ? "已发布" : "发布失败：" + failure);
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
