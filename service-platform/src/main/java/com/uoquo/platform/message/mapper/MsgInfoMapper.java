package com.uoquo.platform.message.mapper;

import com.uoquo.platform.message.model.pojo.MsgInfo;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
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
     * 更新发布状态（CAS：只有当前状态为 fromStatus 时才会更新，同时重置群发进度）
     * @return 更新的行数；0 表示消息不存在、已删除或状态已经变化（如已被其他节点发布）
     */
    int updatePublishStatus(@Param("row") MsgInfo row, @Param("fromStatus") String fromStatus);

    /**
     * 设置定时发布（只有待发布的消息才能设置）
     * @return 更新的行数
     */
    int updateSchedule(@Param("row") MsgInfo row, @Param("waitStatus") String waitStatus);

    /**
     * 取消定时发布（清空发布时间，只对待发布且已设置发布时间的消息有效）
     * @return 更新的行数
     */
    int clearSchedule(@Param("id") String id, @Param("updateUser") String updateUser,
                      @Param("updateTime") Date updateTime, @Param("waitStatus") String waitStatus);

    /**
     * 到点需要发布的消息ID
     */
    List<String> listDueIds(@Param("waitStatus") String waitStatus, @Param("now") Date now, @Param("limit") int limit);

    /**
     * 需要清理的过期消息ID
     */
    List<String> listExpiredIds(@Param("statuses") List<String> statuses, @Param("before") Date before, @Param("limit") int limit);

    /**
     * 领取群发任务（CAS）
     * @return 1 表示领取成功；0 表示任务不存在、已完成、正在被其他节点执行，或接管次数已用完
     */
    int claimFanout(@Param("id") String id, @Param("doneStatus") String doneStatus, @Param("now") Date now,
                    @Param("staleTime") Date staleTime, @Param("maxRetry") int maxRetry);

    /**
     * 更新群发进度
     * @return 1 表示更新成功；0 表示消息已被撤回/删除，或任务已被其他节点接管，应停止群发
     */
    int updateFanoutProgress(@Param("id") String id, @Param("doneStatus") String doneStatus, @Param("retry") int retry,
                             @Param("cursor") String cursor, @Param("total") int total,
                             @Param("finished") boolean finished, @Param("now") Date now);

    /**
     * 群发心跳（只更新进度时间）
     * @return 1 表示更新成功；0 表示消息已被撤回/删除，或任务已被其他节点接管，应停止群发
     */
    int heartbeatFanout(@Param("id") String id, @Param("doneStatus") String doneStatus,
                        @Param("retry") int retry, @Param("now") Date now);

    /**
     * 将中断后接管次数已用完的群发任务标记为失败
     */
    int failExhaustedFanout(@Param("doneStatus") String doneStatus, @Param("staleTime") Date staleTime, @Param("maxRetry") int maxRetry);

    /**
     * 需要继续群发的消息（含正文）
     */
    List<MsgInfo> listStalledFanout(@Param("doneStatus") String doneStatus, @Param("now") Date now,
                                    @Param("staleTime") Date staleTime, @Param("maxRetry") int maxRetry, @Param("limit") int limit);

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