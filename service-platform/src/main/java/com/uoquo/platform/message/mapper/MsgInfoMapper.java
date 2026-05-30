package com.uoquo.platform.message.mapper;

import com.uoquo.platform.message.model.pojo.MsgInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface MsgInfoMapper {

    /**
     * 新增
     * @mbg.generated generated automatically, do not modify!
     */
    int insert(MsgInfo row);

    /**
     * 删除（逻辑删）
     * @mbg.generated generated automatically, do not modify!
     */
    int deleteByPrimaryKey(@Param("id") String id, @Param("deleteState") Long deleteState);

    /**
     * 更新基本信息
     * @mbg.generated generated automatically, do not modify!
     */
    int updateByPrimaryKey(MsgInfo row);

    /**
     * 更新状态
     * @mbg.generated generated automatically, do not modify!
     */
    int updateStatus(MsgInfo row);

    /**
     * 更新发布状态
     * @mbg.generated generated automatically, do not modify!
     */
    int updatePublishStatus(MsgInfo row);

    /**
     * 查询单条
     * @mbg.generated generated automatically, do not modify!
     */
    MsgInfo selectByPrimaryKey(String id);

    /**
     * 列表查询
     * @mbg.generated generated automatically, do not modify!
     */
    List<MsgInfo> listBySearch(Map<String, Object> map);
}