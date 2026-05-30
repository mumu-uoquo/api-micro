package com.uoquo.platform.logs.service;

import com.uoquo.platform.logs.model.dto.BizEventRecordDto;
import com.uoquo.platform.logs.model.dto.BizEventRetryDto;
import com.uoquo.platform.logs.model.param.BizEventRecordParam;
import com.uoquo.platform.logs.model.param.BizEventRecordSearchParam;
import com.uoquo.mybatis.page.PageResult;

import java.util.List;

/**
 * 业务事件记录服务
 */
public interface BizEventRecordService {

    /**
     * 处理事件记录（首次或重试）
     * @param param 事件记录参数
     * @return 记录 ID
     */
    String saveEventRecord(BizEventRecordParam param);

    /**
     * 重试事件
     * @param id 记录 ID
     */
    void retryEvent(String id);

    /**
     * 分页查询事件记录
     * @param param 查询参数
     * @return 分页结果
     */
    PageResult<BizEventRecordDto> listRecords(BizEventRecordSearchParam param);

    /**
     * 查询事件记录详情
     * @param id 记录 ID
     * @return 记录详情
     */
    BizEventRecordDto getRecordById(String id);

    /**
     * 根据记录 ID 查询重试列表
     * @param recordId 记录 ID
     * @return 重试记录列表
     */
    List<BizEventRetryDto> listByRetry(String recordId);
}
