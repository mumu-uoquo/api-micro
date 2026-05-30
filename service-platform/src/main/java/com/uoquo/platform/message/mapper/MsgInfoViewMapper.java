package com.uoquo.platform.message.mapper;

import com.uoquo.platform.message.model.dto.MsgInfoViewDto;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface MsgInfoViewMapper {

    /**
     * 单条查询
     * @mbg.generated generated automatically, do not modify!
     */
    MsgInfoViewDto selectByPrimaryKey(String id);

    /**
     * 单条查询
     * @mbg.generated generated automatically, do not modify!
     */
    MsgInfoViewDto selectByMessageAndReceiver(@Param("messageId") String messageId, @Param("receiverId") String receiverId);

    /**
     * 列表查询
     * @mbg.generated generated automatically, do not modify!
     */
    List<MsgInfoViewDto> listBySearch(Map<String, Object> map);
}