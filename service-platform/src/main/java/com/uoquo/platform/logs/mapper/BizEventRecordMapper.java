package com.uoquo.platform.logs.mapper;

import com.uoquo.platform.logs.model.pojo.BizEventRecord;

import java.util.List;
import java.util.Map;

public interface BizEventRecordMapper {

    /**
     * 新增
     * @mbg.generated generated automatically, do not modify!
     */
    int insert(BizEventRecord row);

    /**
     * 单条查询
     * @mbg.generated generated automatically, do not modify!
     */
    BizEventRecord selectByPrimaryKey(String id);

    /**
     * 列表查询
     * @mbg.generated generated automatically, do not modify!
     */
    List<BizEventRecord> listBySearch(Map<String, Object> map);

    /**
     * 更新重试信息
     */
    int updateRetryInfo(BizEventRecord row);
}
