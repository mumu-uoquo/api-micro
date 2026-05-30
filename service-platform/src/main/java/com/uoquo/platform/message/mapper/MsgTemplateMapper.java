package com.uoquo.platform.message.mapper;

import com.uoquo.platform.message.model.pojo.MsgTemplate;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface MsgTemplateMapper {

    /**
     * 新增
     * @mbg.generated generated automatically, do not modify!
     */
    int insert(MsgTemplate row);

    /**
     * 删除（逻辑删）
     * @mbg.generated generated automatically, do not modify!
     */
    int deleteByPrimaryKey(@Param("id") String id, @Param("deleteState") Long deleteState);

    /**
     * 更新基本信息
     * @mbg.generated generated automatically, do not modify!
     */
    int updateByPrimaryKey(MsgTemplate row);

    /**
     * 更新状态
     * @mbg.generated generated automatically, do not modify!
     */
    int updateStatus(MsgTemplate row);

    /**
     * 取消默认标识
     * @mbg.generated generated automatically, do not modify!
     */
    int updateClearDefault(@Param("messageType") String messageType, @Param("pushWay") String pushWay);

    /**
     * 设置默认标识
     * @mbg.generated generated automatically, do not modify!
     */
    int updateToDefault(MsgTemplate row);

    /**
     * 单条查询
     * @mbg.generated generated automatically, do not modify!
     */
    MsgTemplate selectByPrimaryKey(String id);

    /**
     * 单条查询
     * @mbg.generated generated automatically, do not modify!
     */
    MsgTemplate selectByCode(String templateCode);

    /**
     * 单条查询
     * @mbg.generated generated automatically, do not modify!
     */
    MsgTemplate selectByDefault(@Param("messageType") String messageType, @Param("pushWay") String pushWay);

    /**
     * 列表查询
     * @mbg.generated generated automatically, do not modify!
     */
    List<MsgTemplate> listBySearch(Map<String, Object> map);
}