package com.uoquo.platform.logs.service.impl;

import com.uoquo.cloud.events.RemoteEvent;
import com.uoquo.platform.common.exception.PlatformReturnCode;
import com.uoquo.platform.logs.mapper.BizEventRecordMapper;
import com.uoquo.platform.logs.mapper.BizEventRetryMapper;
import com.uoquo.platform.logs.model.dto.BizEventRecordDto;
import com.uoquo.platform.logs.model.dto.BizEventRetryDto;
import com.uoquo.platform.logs.model.param.BizEventRecordParam;
import com.uoquo.platform.logs.model.param.BizEventRecordSearchParam;
import com.uoquo.platform.logs.model.pojo.BizEventRecord;
import com.uoquo.platform.logs.model.pojo.BizEventRetry;
import com.uoquo.platform.logs.service.BizEventRecordService;
import com.uoquo.utils.CompressUtil;
import com.uoquo.utils.CurrentUser;
import com.uoquo.utils.IDGenerator;
import com.uoquo.utils.StringUtil;
import com.uoquo.utils.json.JsonUtil;
import com.uoquo.web.SystemReturnCode;
import com.uoquo.web.events.AppEvent;
import com.uoquo.web.events.UoquoEventPublisher;
import com.uoquo.web.events.deserializer.DataTypeResolver;
import com.uoquo.web.exception.ResourceNotFoundException;
import com.uoquo.web.exception.UoquoException;
import com.uoquo.web.mybatis.page.PageHelper;
import com.uoquo.mybatis.page.PageList;
import com.uoquo.mybatis.page.PageResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 业务事件记录服务实现
 */
@Service
public class BizEventRecordServiceImpl implements BizEventRecordService {

    private static final Logger logger = LoggerFactory.getLogger(BizEventRecordServiceImpl.class);

    @Autowired
    private BizEventRecordMapper bizEventRecordMapper;

    @Autowired
    private BizEventRetryMapper bizEventRetryMapper;

    @Autowired
    private UoquoEventPublisher eventPublisher;

    @Autowired
    private DataTypeResolver dataTypeResolver;

    @Override
    public String saveEventRecord(BizEventRecordParam param) {
        // 重发的消息咋点击“重发”时已经入库，此处不处理
        if (param.getRetryFlag() != null && param.getRetryFlag()) {
            logger.info("业务[{}]数据[{}]的操作[{}]消息重发（不入库）：{}", param.getBusinessType(), param.getBusinessId(), param.getOperationType(), JsonUtil.serialize(param));
            return param.getBusinessId();
        }
        // 保存入库
        BizEventRecord record = new BizEventRecord();
        BeanUtils.copyProperties(param, record);
        if (StringUtil.isNull(record.getId())) {
            record.setId(IDGenerator.getNextULID());
        }
        record.setToken(this.formatToken(record.getToken()));
        record.setRetryCount(0);
        // 在某些场景下会为空，为了后续兼容分表策略，因此赋予默认值
        // 场景1：在登录时如果账号不存在，则userId为空
        // 场景2：批量处理数据时，业务ID记录在扩展字段中
        if (StringUtil.isNull(record.getBusinessId())) {
            record.setBusinessId("unknown");
        }
        if (StringUtil.isNull(record.getBusinessInstituteId())) {
            record.setBusinessInstituteId("unknown");
        }
        if (StringUtil.isNull(record.getOperatorId())) {
            record.setOperatorId("unknown");
        }
        // 对 eventContent 进行 gzip 压缩后转 Base64 再存储，节省数据库空间
        if (StringUtil.notNull(record.getEventContent())) {
            try {
                byte[] compressed = CompressUtil.gzip(record.getEventContent().getBytes(StandardCharsets.UTF_8));
                record.setEventContent(Base64.getEncoder().encodeToString(compressed));
            } catch (Exception e) {
                logger.warn("eventContent 压缩失败，将以原始内容存储：id={}", record.getId(), e);
            }
        }
        try {
            bizEventRecordMapper.insert(record);
            logger.info("新增事件记录：id={}, businessType={}, businessId={}, operationType={}",
                    record.getId(), param.getBusinessType(), param.getBusinessId(), param.getOperationType());
        } catch (Exception e) {
            // 捕获异常，仅记录日志，防止因为此处异常导致其他业务无法处理
            logger.warn("新增事件记录失败：{}", JsonUtil.serialize(record), e);
        }
        return record.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void retryEvent(String id) {
        // 1. 查询记录详情
        BizEventRecord record = bizEventRecordMapper.selectByPrimaryKey(id);
        if (record == null || StringUtil.isNull(record.getEventContent())) {
            throw new ResourceNotFoundException("消息不存在");
        }

        try {
            // 2. 将 event_content 内容重新放入 spring-cloud-bus 队列
            // 对 eventContent 进行 Base64 解码 + gzip 解压还原，兼容未压缩的历史数据
            String eventContent = decompressEventContent(record.getEventContent());
            Map<String, Object> map = JsonUtil.deserialize(eventContent);
            // 20260603：显示转换为对象再发送
//            map.put("retry", true);
//            if (record.getRemoteEvent() == null || record.getRemoteEvent()) {
//                streamBridge.send(BusConstants.OUTPUT, MessageBuilder.withPayload(map).build());
//            } else {
//                delegate.publishEvent(map);
//            }
            String type = (String) map.get("type");
            if (StringUtil.notNull(type) && "RemoteEvent".equals(type)) {
                String dataType = (String) map.get("dataType");
                Class<?> resolvedClass = dataTypeResolver.resolve(dataType, "RemoteEvent");
                RemoteEvent<?> event = JsonUtil.deserialize(eventContent, RemoteEvent.class, resolvedClass);
                event.setRetry(true);
                eventPublisher.publishEvent(event);
            } else if (StringUtil.notNull(type) && "AppEvent".equals(type)) {
                String dataType = (String) map.get("dataType");
                Class<?> resolvedClass = dataTypeResolver.resolve(dataType, "AppEvent");
                AppEvent<?> event = JsonUtil.deserialize(eventContent, AppEvent.class, resolvedClass);
                event.setRetry(true);
                eventPublisher.publishEvent(event);
            } else {
                map.put("retry", true);
                eventPublisher.publishEvent(map);
            }


            // 3. 更新 biz_event_record 的重试信息
            BizEventRecord updateRecord = new BizEventRecord();
            if (record.getRetryCount() == null) {
                updateRecord.setRetryCount(1);
            } else {
                updateRecord.setRetryCount(record.getRetryCount() + 1);
            }
            updateRecord.setId(record.getId());
            updateRecord.setRetryOperatorId(CurrentUser.getInfo().getUserId());
            updateRecord.setRetryOperatorName(CurrentUser.getInfo().getUserName());
            updateRecord.setRetryTime(new Date());
            bizEventRecordMapper.updateRetryInfo(updateRecord);

            // 4. 插入 biz_event_retry 表
            this.addRetryRecord(record);

            logger.info("手动重试事件成功：recordId={}, businessId={}", id, record.getBusinessId());
        } catch (Exception e) {
            logger.error("手动重试事件失败：recordId={}", id, e);
            throw new UoquoException(PlatformReturnCode.KAFKA_SEND_FAILED);
        }
    }

    @Override
    public PageResult<BizEventRecordDto> listRecords(BizEventRecordSearchParam param) {
        // 条件组装
        Map<String, Object> paramMap = new HashMap<>();
        if (StringUtil.notNull(param.getBusinessType())) {
            paramMap.put("businessType", param.getBusinessType());
        }
        if (StringUtil.notNull(param.getBusinessSubType())) {
            paramMap.put("businessSubType", param.getBusinessSubType());
        }
        if (StringUtil.notNull(param.getBusinessId())) {
            paramMap.put("businessId", param.getBusinessId());
        }
        if (StringUtil.notNull(param.getBusinessInstituteId())) {
            paramMap.put("businessInstituteId", param.getBusinessInstituteId());
        }
        if (StringUtil.notNull(param.getOperatorId())) {
            paramMap.put("operatorId", param.getOperatorId());
        }
        if (StringUtil.notNull(param.getOperatorInstituteId())) {
            paramMap.put("operatorInstituteId", param.getOperatorInstituteId());
        }
        if (StringUtil.notNull(param.getToken())) {
            paramMap.put("token", param.getToken());
        }
        if (StringUtil.notNull(param.getOperationType())) {
            paramMap.put("operationType", param.getOperationType());
        }
        if (param.getOperationTimeStart() != null) {
            paramMap.put("operationTimeStart", param.getOperationTimeStart());
            paramMap.put("operationTimeEnd", param.getOperationTimeEnd() == null ? new Date() : param.getOperationTimeEnd());
        }
        // 分页查询
        PageHelper.startPage(param.getPageNum(), param.getPageSize());
        PageList<BizEventRecord> list = (PageList<BizEventRecord>) bizEventRecordMapper.listBySearch(paramMap);
        // 对象转换
        List<BizEventRecordDto> resultList = new ArrayList<>();
        for (BizEventRecord item : list.getResult()) {
            BizEventRecordDto dto = this.convert2Dto(item);
            // 这两个字段内容较多，仅详情接口返回
            dto.setChangeData(null);
            dto.setChangeFields(null);
            // TODO 补充机构信息
            resultList.add(dto);
        }
        return PageResult.of(list, resultList);
    }

    @Override
    public BizEventRecordDto getRecordById(String id) {
        BizEventRecord record = bizEventRecordMapper.selectByPrimaryKey(id);
        if (record == null) {
            throw new ResourceNotFoundException("记录信息不存在");
        }
        return convert2Dto(record);
    }

    @Override
    public List<BizEventRetryDto> listByRetry(String recordId) {
        List<BizEventRetry> list = bizEventRetryMapper.listByRecordId(recordId);
        List<BizEventRetryDto> resultList = new ArrayList<>();
        for (BizEventRetry item : list) {
            resultList.add(convert2Dto(item));
        }
        return resultList;
    }

    /**
     * 保存重试记录
     */
    private void addRetryRecord(BizEventRecord record) {
        BizEventRetry retry = new BizEventRetry();
        retry.setId(IDGenerator.getNextULID());
        // 原信息内容
        retry.setRecordId(record.getId());
        retry.setBusinessId(record.getBusinessId());
        retry.setBusinessInstituteId(record.getBusinessInstituteId());
        retry.setOperationType(record.getOperationType());
        retry.setOperationStatus(SystemReturnCode.SUCCESS.getCode());
        // 重试时的token
        retry.setToken(this.formatToken(CurrentUser.getToken()));
        retry.setTraceId(CurrentUser.getTraceId());
        // 重试操作人
        retry.setOperatorId(CurrentUser.getInfo().getUserId());
        retry.setOperatorName(CurrentUser.getInfo().getUserName());
        retry.setOperatorInstituteId(CurrentUser.getInfo().getInstituteId());
        retry.setOperationTime(new Date());
        // 保存
        bizEventRetryMapper.insert(retry);
        logger.info("新增重试记录：id={}, recordId={}", retry.getId(), retry.getRecordId());
    }

    /**
     * 对 eventContent 进行解压还原。<br>
     * 存储时已做 gzip 压缩 + Base64 编码；读取时先 Base64 解码再 gzip 解压。<br>
     * 兼容未压缩的历史数据：若解压失败则直接返回原始字符串。
     */
    private String decompressEventContent(String eventContent) {
        if (StringUtil.isNull(eventContent)) {
            return eventContent;
        }
        try {
            byte[] decoded = Base64.getDecoder().decode(eventContent);
            if (CompressUtil.isGzip(decoded)) {
                return new String(CompressUtil.unGzip(decoded), StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            logger.warn("eventContent 解压失败，使用原始内容：{}", e.getMessage());
        }
        // 兼容历史未压缩数据
        return eventContent;
    }

    /**
     * 格式化处理 token，防止长度过长
     */
    private String formatToken(String token) {
        if (StringUtil.isNull(token)) {
            return "";
        } else if (token.length() <= 16) {
            return token;
        } else {
            return token.substring(0, 16);
        }
    }

    private BizEventRecordDto convert2Dto(BizEventRecord info) {
        BizEventRecordDto dto = new BizEventRecordDto();
        BeanUtils.copyProperties(info, dto);
        return dto;
    }

    private BizEventRetryDto convert2Dto(BizEventRetry info) {
        BizEventRetryDto dto = new BizEventRetryDto();
        BeanUtils.copyProperties(info, dto);
        return dto;
    }
}
