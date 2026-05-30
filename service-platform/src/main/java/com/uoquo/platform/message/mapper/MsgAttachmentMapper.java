package com.uoquo.platform.message.mapper;

import com.uoquo.platform.message.model.pojo.MsgAttachment;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MsgAttachmentMapper {

    /**
     * 新增
     * @mbg.generated generated automatically, do not modify!
     */
    int insert(MsgAttachment row);

    /**
     * 删除
     * @mbg.generated generated automatically, do not modify!
     */
    int deleteByPrimaryKey(String id);

    /**
     * 批量删除
     * @mbg.generated generated automatically, do not modify!
     */
    int deleteByMessageId(String messageId);

    /**
     * 更新下载次数
     * @mbg.generated generated automatically, do not modify!
     */
    int updateDownloadCount(@Param("id") String id, @Param("downloadCount") int downloadCount);

    /**
     * 查询单条附件
     * @mbg.generated generated automatically, do not modify!
     */
    MsgAttachment selectByPrimaryKey(String id);

    /**
     * 查询消息的全部附件
     * @mbg.generated generated automatically, do not modify!
     */
    List<MsgAttachment> listByMessageId(String messageId);
}