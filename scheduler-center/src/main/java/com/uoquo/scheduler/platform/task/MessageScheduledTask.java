package com.uoquo.scheduler.platform.task;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.uoquo.scheduler.platform.listener.MessageInfoEventListener;
import com.uoquo.scheduler.platform.model.param.MsgMaintainParam;
import com.uoquo.scheduler.platform.model.pojo.MsgInfo;
import com.uoquo.scheduler.platform.remote.MessageRemoteService;
import com.uoquo.utils.Config;

/**
 * 消息的定时任务：
 * <ol>
 *   <li>定时发布：到点发布设置了定时发布时间的消息</li>
 *   <li>群发接管：继续群发中断（服务重启、异常等）的消息，从上次处理的位置继续</li>
 *   <li>过期清理：清理过期超过保留天数的消息及其接收记录</li>
 * </ol>
 * 多个节点会同时执行，每个任务都是幂等的：具体的互斥（CAS）由 service-platform 保证，所以这里不需要分布式锁。
 * 与站内消息的群发相同，所有 service-platform 调用失败都只记录日志，等下一个周期重试。<br>
 * 定时器本身由 app-core 提供（{@code TaskSchedulerConfig}）。消息相关的定时任务统一由
 * {@code app.schedule.message.enabled} 控制（默认开启），设置为 false 时本类不会被注册，三个任务都不会执行。
 * @author xuhz
 */
@Component
@ConditionalOnProperty(name = "app.schedule.message.enabled", havingValue = "true", matchIfMissing = true)
public class MessageScheduledTask {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    /** 过期清理：单次远程调用最长执行时间（秒），必须小于 Feign 的读超时（默认10秒） */
    private static final int CLEAR_MAX_SECONDS = 8;

    /** 过期清理：单次定时执行最多调用的次数，防止异常情况下无限循环 */
    private static final int CLEAR_MAX_ROUNDS = 60;

    @Autowired
    private MessageRemoteService messageRemoteService;

    @Autowired
    private MessageInfoEventListener messageInfoEventListener;

    /**
     * 定时发布：每30秒检查一次，发布已到发布时间的消息
     */
    @Scheduled(initialDelayString = "${app.schedule.message.publish-initial-delay:60000}",
            fixedDelayString = "${app.schedule.message.publish-interval:30000}")
    public void publishDueMessages() {
        try {
            MsgMaintainParam param = new MsgMaintainParam();
            param.setLimit(50);
            Integer count = messageRemoteService.publishDueMessages(param);
            if (count != null && count > 0) {
                logger.info("定时发布消息[{}]条", count);
            }
        } catch (Exception e) {
            logger.warn("定时发布消息失败（下个周期重试）：{}", e.getMessage());
        }
    }

    /**
     * 群发接管：每60秒检查一次，继续群发未开始或中断的消息
     */
    @Scheduled(initialDelayString = "${app.schedule.message.fanout-initial-delay:90000}",
            fixedDelayString = "${app.schedule.message.fanout-interval:60000}")
    public void resumeStalledFanout() {
        try {
            MsgMaintainParam param = new MsgMaintainParam();
            param.setLimit(10);
            param.setStaleSeconds(MessageInfoEventListener.fanoutStaleSeconds());
            param.setMaxRetry(MessageInfoEventListener.fanoutMaxRetry());
            List<MsgInfo> list = messageRemoteService.listStalledFanout(param);
            if (list == null || list.isEmpty()) {
                return;
            }
            logger.info("发现[{}]条群发未开始或已中断的消息，开始接管", list.size());
            for (MsgInfo message : list) {
                // 是否真正执行由领取任务（CAS）决定，多个节点同时接管同一条消息也只有一个会执行
                messageInfoEventListener.resume(message);
            }
        } catch (Exception e) {
            logger.warn("查询需要继续群发的消息失败（下个周期重试）：{}", e.getMessage());
        }
    }

    /**
     * 过期清理：默认每天凌晨3:30执行（可通过 app.schedule.message.clear-cron 调整）。<br>
     * 过期后保留天数由 app.message.expired.retain-days 配置（默认30天）。
     */
    @Scheduled(cron = "${app.schedule.message.clear-cron:0 30 3 * * ?}")
    public void clearExpiredMessages() {
        int total = 0;
        try {
            int retainDays = Math.max(1, Config.getInt("app.message.expired.retain-days", 30));
            for (int i = 0; i < CLEAR_MAX_ROUNDS; i++) {
                MsgMaintainParam param = new MsgMaintainParam();
                param.setRetainDays(retainDays);
                param.setLimit(20);
                param.setMaxSeconds(CLEAR_MAX_SECONDS);
                Integer count = messageRemoteService.clearExpiredMessages(param);
                if (count == null || count <= 0) {
                    break;
                }
                total += count;
            }
            logger.info("清理过期消息完成：共清理[{}]条（过期超过[{}]天）", total, retainDays);
        } catch (Exception e) {
            logger.warn("清理过期消息失败（已清理[{}]条，下次继续）：{}", total, e.getMessage());
        }
    }
}
