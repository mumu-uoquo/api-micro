package com.uoquo.platform.logs.mapper;

import com.uoquo.platform.logs.model.pojo.BizEventRetry;

import java.util.List;

public interface BizEventRetryMapper {

    /**
     * 新增
     * @mbg.generated generated automatically, do not modify!
     */
    int insert(BizEventRetry row);

    /**
     * 单条查询
     * @mbg.generated generated automatically, do not modify!
     */
    BizEventRetry selectByPrimaryKey(String id);

    /**
     * 根据记录 ID 查询列表
     */
    List<BizEventRetry> listByRecordId(String recordId);
}
