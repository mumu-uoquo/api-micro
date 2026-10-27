package com.uoquo.scheduler.platform.listener;

import com.uoquo.cloud.events.RemoteEvent;
import com.uoquo.scheduler.common.BusinessOperationEnum;
import com.uoquo.scheduler.common.BusinessTypeEnum;
import com.uoquo.scheduler.platform.model.param.BizEventRecordParam;
import com.uoquo.scheduler.platform.model.pojo.SseMessage;
import com.uoquo.scheduler.platform.remote.LogsRemoteService;
import com.uoquo.utils.IDGenerator;
import com.uoquo.utils.ObjectUtil;
import com.uoquo.utils.StringUtil;
import com.uoquo.utils.json.JsonUtil;
import com.uoquo.web.events.AppEvent;
import com.uoquo.web.events.UoquoEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 事件监听器：所有事件（用于记录操作日志）<br>
 * operationType详见{@link BusinessOperationEnum BusinessOperationEnum}的定义 <br>
 * businessType详见{@link BusinessTypeEnum BusinessTypeEnum}的定义 <br>
 * 常见过滤条件
 * <pre>
 *     1. 属性过滤
 *     处理类型为新增的事件：@EventListener(condition = "#event.operationType == 'CREATE'")
 *     处理类型为新增的事件：@EventListener(condition = "#event.operationType.equals('CREATE')")
 *     处理类型不为空的事件：@EventListener(condition = "#event.operationType != null")
 *     2. 多条件组合
 *     处理用户的更新事件：@EventListener(condition = "#event.operationType == 'UPDATE' && #event.source == 'user'")
 *     3. 正则表达式
 *     处理名称以"user"开头的事件：@EventListener(condition = "#event.name.matches('^user.*')")
 *     处理名称以"user"开头的事件：@EventListener(condition = "#event.name matches '^user.*'")
 *     4. 集合数组
 *     处理名称包含"user"的事件：@EventListener(condition = "#event.tags.contains('user')")
 *     5. 自定义Bean处理
 *     处理有READ权限的事件：@EventListener(condition = "@securityService.hasPermission(#event, 'READ')")
 *     处理类型为指定常量的事件：@EventListener(condition = "#event.operationType == T(com.uoquo.scheduler.common.BusinessOperationEnum).LOGIN.getCode()")
 * </pre>
 * @author xuhz
 */
@Component
public class AllEventListener {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private LogsRemoteService logsRemoteService;

    @PostConstruct
    public void setProperties(){
        logger.debug("AllEventListener init ...");
    }

    /**
     * 监听事件：所有远程事件
     * 注：需要有与事件发送方同名的Event类定义，否则监听不到
     */
    @EventListener
    public void listenAllRemoteEvent(RemoteEvent<?> event) {
        if (event.isRetry()) {
            logger.info("重发的远程事件[{}]不记录日志：{eventId=\"{}\", businessType=\"{}\", businessId=\"{}\", operationType=\"{}\", operatorId=\"{}\"}",
                    event.getId(), event.getId(), event.getBusinessType(), event.getBusinessId(), event.getOperationType(), event.getOperatorId());
            return;
        } else if (StringUtil.notNull(event.getDataType()) && event.getDataType().endsWith(SseMessage.class.getSimpleName())) {
            logger.info("SSE消息[{}]不记录日志：{eventId=\"{}\", businessType=\"{}\", businessId=\"{}\", operationType=\"{}\", operatorId=\"{}\"}",
                    event.getId(), event.getId(), event.getBusinessType(), event.getBusinessId(), event.getOperationType(), event.getOperatorId());
            return;
        }
        // 1. 构建基本参数
        BizEventRecordParam param = buildLogBusinessOperationParam(event);
        // 2. 补充扩展信息
        param.setRemoteEvent(true);
        param.setDescription(event.getRemarks());
        // 变更内容
        List<Map<String  ,Object>> changeData = ObjectUtil.compare(event.getOldData(), event.getNewData());
        if (!changeData.isEmpty()) {
            List<Object> changeFields = changeData.stream().map(item -> item.get("field")).collect(Collectors.toList());
            param.addChangeData("change", changeData);
            param.setChangeFields(JsonUtil.serialize(changeFields));
        }
        // 3. 保存日志
        processOperationEvent(param);
    }

    /**
     * 监听事件：所有本地事件
     * 注：需要有与事件发送方同名的Event类定义，否则监听不到
     */
    @EventListener
    public void listenAllAppEvent(AppEvent<?> event) {
        if (event.isRetry()) {
            logger.info("重发的本地事件[{}]不记录日志：{eventId=\"{}\", businessType=\"{}\", businessId=\"{}\", operationType=\"{}\", operatorId=\"{}\"}",
                    event.getId(), event.getId(), event.getBusinessType(), event.getBusinessId(), event.getOperationType(), event.getOperatorId());
            return;
        }
        // 1. 构建基本参数
        BizEventRecordParam param = buildLogBusinessOperationParam(event);
        // 2. 补充扩展信息
        param.setRemoteEvent(false);
        param.setDescription(event.getRemarks());
        // 变更内容
        List<Map<String  ,Object>> changeData = ObjectUtil.compare(event.getOldData(), event.getNewData());
        if (!changeData.isEmpty()) {
            List<Object> changeFields = changeData.stream().map(item -> item.get("field")).collect(Collectors.toList());
            param.addChangeData("change", changeData);
            param.setChangeFields(JsonUtil.serialize(changeFields));
        }
        // 3. 保存日志
        processOperationEvent(param);
    }

    /**
     * 构建操作日志参数
     */
    private BizEventRecordParam buildLogBusinessOperationParam(UoquoEvent event){
        BizEventRecordParam param = new BizEventRecordParam();
//        BeanUtils.copyProperties(event, param);
        // 1. 基本信息
        if (StringUtil.isNull(event.getId())) {
            param.setId(IDGenerator.getNextULID());
        } else {
            param.setId(event.getId());
        }
        param.setToken(event.getToken());
        param.setTraceId(event.getTraceId());
        param.setRetryFlag(event.isRetry());
        // 2. 业务信息
        param.setBusinessType(event.getBusinessType());
        param.setBusinessSubType(event.getBusinessSubType());
        param.setBusinessTable(event.getBusinessTable());
        param.setBusinessId(event.getBusinessId());
        param.setBusinessInstituteId(event.getBusinessInstituteId());
        // 3. 操作人信息
        param.setOperatorId(event.getOperatorId());
        param.setOperatorName(event.getOperatorName());
        param.setOperationTime(event.getOperationTime());
        param.setOperatorInstituteId(event.getOperatorInstituteId());
        // 4. 操作类型及状态
        param.setOperationType(event.getOperationType());
        param.setOperationStatus(event.getOperationStatus());
        param.setOperationContent(event.getExtension());
        // 5. 事件原文（消息记录使用）
        param.setEventClass(event.getClass().getName());
        param.setEventContent(JsonUtil.serialize(event));
        return param;
    }

    /**
     * 处理操作日志
     */
    private void processOperationEvent(BizEventRecordParam param) {
        try {
            logsRemoteService.addEventRecord(param);
            if (logger.isDebugEnabled()) {
                logger.debug("处理远程事件[{}]的操作日志完毕：{}", param.getId(), param.getEventContent());
            } else {
                logger.info("保存远程事件[{}]的操作日志成功：{eventId=\"{}\", businessType=\"{}\", businessId=\"{}\", operationType=\"{}\"}",
                        param.getId(), param.getId(), param.getBusinessType(), param.getBusinessId(), param.getOperationType());
            }
        } catch (Exception e) {
            logger.error("处理远程事件[{}]的操作日志失败：{}", param.getId(), param.getEventContent(), e);
        }
    }
}
