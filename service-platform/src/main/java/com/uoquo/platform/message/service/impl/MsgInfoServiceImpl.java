package com.uoquo.platform.message.service.impl;

import com.uoquo.cloud.events.RemoteEvent;
import com.uoquo.platform.common.BusinessOperationEnum;
import com.uoquo.platform.common.BusinessTypeEnum;
import com.uoquo.platform.common.DictionaryCodeEnum;
import com.uoquo.platform.common.exception.MessageReturnCode;
import com.uoquo.platform.dfs.model.dto.UploadFileDto;
import com.uoquo.platform.dfs.model.param.DownloadConfigParam;
import com.uoquo.platform.dfs.service.FileDownloadService;
import com.uoquo.platform.dfs.service.FileUploadService;
import com.uoquo.platform.institute.service.InstituteInfoService;
import com.uoquo.platform.message.mapper.MsgAttachmentMapper;
import com.uoquo.platform.message.mapper.MsgInfoMapper;
import com.uoquo.platform.message.mapper.MsgInfoViewMapper;
import com.uoquo.platform.message.mapper.MsgReceiverMapper;
import com.uoquo.platform.message.model.dto.MsgAttachmentDto;
import com.uoquo.platform.message.model.dto.MsgFanoutDto;
import com.uoquo.platform.message.model.dto.MsgInfoDto;
import com.uoquo.platform.message.model.dto.MsgInfoViewDto;
import com.uoquo.platform.message.model.param.MsgAttachmentParam;
import com.uoquo.platform.message.model.param.MsgFanoutParam;
import com.uoquo.platform.message.model.param.MsgInfoListParam;
import com.uoquo.platform.message.model.param.MsgInfoParam;
import com.uoquo.platform.message.model.pojo.MsgAttachment;
import com.uoquo.platform.message.model.pojo.MsgInfo;
import com.uoquo.platform.message.model.pojo.MsgReceiver;
import com.uoquo.platform.message.service.MsgInfoService;
import com.uoquo.platform.user.service.UserInfoService;
import com.uoquo.utils.CurrentUser;
import com.uoquo.utils.DateUtil;
import com.uoquo.utils.IDGenerator;
import com.uoquo.utils.StringUtil;
import com.uoquo.utils.json.JsonUtil;
import com.uoquo.platform.common.BaseConstant;
import com.uoquo.web.BaseReturnCode;
import com.uoquo.web.SystemReturnCode;
import com.uoquo.web.events.UoquoEventPublisher;
import com.uoquo.web.exception.*;
import com.uoquo.web.mybatis.page.PageHelper;
import com.uoquo.mybatis.page.PageList;
import com.uoquo.mybatis.page.PageResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import jakarta.annotation.Nullable;

@Service
public class MsgInfoServiceImpl implements MsgInfoService {
    final Logger logger = LoggerFactory.getLogger(getClass());

    // 事务控制
    @Autowired
    private DataSourceTransactionManager transactionManager;

    @Autowired
    private UoquoEventPublisher eventPublisher;

    @Autowired
    private MsgInfoMapper msgInfoMapper;

    @Autowired
    private MsgInfoViewMapper msgInfoViewMapper;

    @Autowired
    private MsgAttachmentMapper msgAttachmentMapper;

    @Autowired
    private MsgReceiverMapper msgReceiverMapper;

    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    private InstituteInfoService instituteInfoService;

    @Autowired
    private FileUploadService fileUploadService;

    @Autowired
    private FileDownloadService fileDownloadService;

    /**
     * 静态资源前缀
     */
    @Value("${app.host.static:/}")
    private String staticHost;

    @Override
    public String addMessage(MsgInfoParam param) {
        // 1. 组装数据
        MsgInfo info = new MsgInfo();
        BeanUtils.copyProperties(param, info);
        info.setId(IDGenerator.getNextULID());
        // 发布时间（sender_time）只能在发布时设置（见 publishMessage），新增时不保存，避免未发布的草稿被当成定时发布而自动发布
        info.setSenderTime(null);
        // 默认待发布
        if (StringUtil.isNull(param.getStatus())) {
            info.setStatus(DictionaryCodeEnum.PUBLISH_STATUS_WAIT.getCode());
        }
        info.setStatusTime(new Date());
        // 默认所有人员
        if (StringUtil.isNull(param.getReceiverRange())) {
            info.setReceiverRange(DictionaryCodeEnum.PUBLISH_RANGE_ALL.getCode());
        }
        // 过期时间处理
        if (param.getExpireTime() != null) {
            info.setExpireTime(DateUtil.getDayEnd(param.getExpireTime()));
        }
        // 因为有索引，所以默认用空字符串填充
        info.setSenderId("");
        // 新增的消息只能是待发布
        info.setStatus(DictionaryCodeEnum.PUBLISH_STATUS_WAIT.getCode());
        // 其他信息
        info.setCreateTime(new Date());
        info.setCreateUser(CurrentUser.getInfo().getUserId());
        info.setDeleteState(BaseConstant.NOT_DELETED);
        // 2. 保存数据
        // 开启手动事务
        DefaultTransactionDefinition testTran = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        TransactionStatus testTranStatus = transactionManager.getTransaction(testTran);
        try {
            // 保存消息
            msgInfoMapper.insert(info);
            // 保存附件
            if (param.getAttachments() != null && !param.getAttachments().isEmpty()) {
                for (MsgAttachmentParam attachmentParam : param.getAttachments()) {
                    if (StringUtil.notNull(attachmentParam.getId())) {
                        continue;
                    }
                    MsgAttachment attachment = saveAttachment(info, attachmentParam);
                    msgAttachmentMapper.insert(attachment);
                }
            }
            // 提交事务
            transactionManager.commit(testTranStatus);
        } catch (Exception ex) {
            // 回滚事务
            transactionManager.rollback(testTranStatus);
            logger.error("新增消息失败:{}", JsonUtil.serialize( param), ex);
            throw new SystemErrorException("新增消息失败", ex);
        }
        // 3. 发布事件（新增）
        this.publishEvent(BusinessOperationEnum.CREATE, SystemReturnCode.SUCCESS, null, info, null);
        // 4. 如果入参为发布状态，则触发发布逻辑
        if (DictionaryCodeEnum.PUBLISH_STATUS_DONE.getCode().equals(param.getStatus())) {
            param.setId(info.getId());
            this.publishMessage(param);
        }
        return info.getId();
    }

    private MsgAttachment saveAttachment(MsgInfo info, MsgAttachmentParam param) throws IOException {
        UploadFileDto file = fileUploadService.finishByBase64(param.getUploadCode(), param.getFilePath());
        MsgAttachment attachment = new MsgAttachment();
        attachment.setId(IDGenerator.getNextULID());
        attachment.setMessageId(info.getId());
        attachment.setFileName(file.getFileName());
        attachment.setFileSize(file.getFileSize());
        attachment.setFileType(file.getFileType());
        attachment.setFilePath(file.getFilePath());
        attachment.setDownloadCount(0);
        attachment.setCreateTime(new Date());
        return attachment;
    }

    @Override
    public void deleteMessage(String messageId) {
        MsgInfo old = msgInfoMapper.selectByPrimaryKey(messageId);
        if (old == null) {
            throw new ResourceNotFoundException("消息信息不存在");
        }
        msgInfoMapper.deleteByPrimaryKey(messageId, System.currentTimeMillis());
        // 发布事件（删除）
        this.publishEvent(BusinessOperationEnum.DELETE, SystemReturnCode.SUCCESS, old, null, null);
    }

    @Override
    public void updateMessage(MsgInfoParam param) {
        // 1. 基本判断
        MsgInfo old = msgInfoMapper.selectByPrimaryKey(param.getId());
        if (old == null) {
            throw new ResourceNotFoundException("消息信息不存在");
        } else if (!DictionaryCodeEnum.PUBLISH_STATUS_WAIT.getCode().equals(old.getStatus())) {
            // 已发布的、已撤销的不可以再次编辑
            throw new UoquoException(MessageReturnCode.PUBLISH_ERROR);
        }
        // 2. 组装数据
        MsgInfo info = new MsgInfo();
        BeanUtils.copyProperties(param, info);
        // 过期时间处理
        if (param.getExpireTime() != null) {
            info.setExpireTime(DateUtil.getDayEnd(param.getExpireTime()));
        }
        // 不更新状态
        info.setStatus(null);
        info.setUpdateTime(new Date());
        info.setUpdateUser(CurrentUser.getInfo().getUserId());
        // 3. 保存数据
        // 开启手动事务
        DefaultTransactionDefinition testTran = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        TransactionStatus testTranStatus = transactionManager.getTransaction(testTran);
        try {
            // 保存消息
            msgInfoMapper.updateByPrimaryKey(info);
            // TODO 删除附件
            // 保存附件
            if (param.getAttachments() != null && !param.getAttachments().isEmpty()) {
                for (MsgAttachmentParam attachmentParam : param.getAttachments()) {
                    if (StringUtil.notNull(attachmentParam.getId())) {
                        continue;
                    }
                    MsgAttachment attachment = saveAttachment(info, attachmentParam);
                    msgAttachmentMapper.insert(attachment);
                }
            }
            // 提交事务
            transactionManager.commit(testTranStatus);
        } catch (Exception ex) {
            // 回滚事务
            transactionManager.rollback(testTranStatus);
            logger.error("更新消息失败:{}", JsonUtil.serialize( param), ex);
            throw new SystemErrorException("更新消息失败", ex);
        }
        // 4. 发布事件（编辑）
        info = msgInfoMapper.selectByPrimaryKey(param.getId());
        this.publishEvent(BusinessOperationEnum.UPDATE, SystemReturnCode.SUCCESS, old, info, null);
        // 5. 如果入参为发布状态，则触发发布逻辑
        if (DictionaryCodeEnum.PUBLISH_STATUS_DONE.getCode().equals(param.getStatus())) {
            this.publishMessage(param);
        }
    }

    @Override
    public void publishMessage(MsgInfoParam param) {
        // 1. 基本判断
        MsgInfo old = msgInfoMapper.selectByPrimaryKey(param.getId());
        if (old == null) {
            throw new ResourceNotFoundException("消息信息不存在");
        } else if (!DictionaryCodeEnum.PUBLISH_STATUS_WAIT.getCode().equals(old.getStatus())) {
            // 已发布的、已撤销的不可以再次发布
            throw new UoquoException(MessageReturnCode.PUBLISH_ERROR);
        }
        // 定时发布：发布时间晚于当前时间时，只保存发布信息，由 scheduler-center 到点后调用 publishDueMessages 真正发布
        if (param.getSenderTime() != null && param.getSenderTime().after(new Date())) {
            this.scheduleMessage(param, old);
            return;
        }
        // 2. 标记事件为已发布
        MsgInfo info = new MsgInfo();
        info.setId(param.getId());
        info.setSenderId(CurrentUser.getInfo().getUserId());
        info.setSenderName(CurrentUser.getInfo().getUserName());
        info.setSenderTime(new Date());
        // 发布范围不为空时，需要重置
        if (StringUtil.notNull(param.getReceiverRange())) {
            info.setReceiverRange(param.getReceiverRange());
            info.setReceiverInstituteId(param.getReceiverInstituteId());
            info.setReceiverIds(param.getReceiverIds());
        }
        info.setPushWay(param.getPushWay());
        // 过期时间处理
        if (param.getExpireTime() == null && old.getExpireTime() == null) {
            // 默认1个月过期
            info.setExpireTime(new Date(System.currentTimeMillis() + 30 * 24 * 60 * 60 * 1000L));
        } else if (param.getExpireTime() != null) {
            info.setExpireTime(param.getExpireTime());
        }
        info.setStatus(DictionaryCodeEnum.PUBLISH_STATUS_DONE.getCode());
        info.setStatusTime(new Date());
        info.setStatusMemo("");
        info.setUpdateUser(CurrentUser.getInfo().getUserId());
        info.setUpdateTime(new Date());
        // 保存数据（CAS：只有仍是待发布状态才能发布成功，避免与定时发布同时发布）
        int rows = msgInfoMapper.updatePublishStatus(info, DictionaryCodeEnum.PUBLISH_STATUS_WAIT.getCode());
        if (rows == 0) {
            throw new UoquoException(MessageReturnCode.PUBLISH_ERROR);
        }
        // 3. 发布事件（发布）
        info = msgInfoMapper.selectByPrimaryKey(param.getId());
        this.publishEvent(BusinessOperationEnum.MESSAGE_PUBLISH, SystemReturnCode.SUCCESS, old, info, null);
    }

    /**
     * 设置定时发布：保存发布信息（接收范围、推送方式、过期时间）和发布时间，状态仍是待发布
     */
    private void scheduleMessage(MsgInfoParam param, MsgInfo old) {
        if (param.getExpireTime() != null && !param.getExpireTime().after(param.getSenderTime())) {
            throw new ParamErrorException("过期时间必须晚于定时发布时间");
        }
        MsgInfo info = new MsgInfo();
        info.setId(param.getId());
        // 发布人是执行“发布”操作的人（不一定是创建人）；待发布状态下，发布时间（sender_time）不为空即表示定时发布
        info.setSenderId(CurrentUser.getInfo().getUserId());
        info.setSenderName(CurrentUser.getInfo().getUserName());
        info.setSenderTime(param.getSenderTime());
        // 发布范围不为空时，需要重置
        if (StringUtil.notNull(param.getReceiverRange())) {
            info.setReceiverRange(param.getReceiverRange());
            info.setReceiverInstituteId(param.getReceiverInstituteId());
            info.setReceiverIds(param.getReceiverIds());
        }
        info.setPushWay(param.getPushWay());
        // 过期时间：未指定时，到点发布时再按“发布时间 + 1个月”默认
        info.setExpireTime(param.getExpireTime());
        info.setUpdateUser(CurrentUser.getInfo().getUserId());
        info.setUpdateTime(new Date());
        int rows = msgInfoMapper.updateSchedule(info, DictionaryCodeEnum.PUBLISH_STATUS_WAIT.getCode());
        if (rows == 0) {
            throw new UoquoException(MessageReturnCode.PUBLISH_ERROR);
        }
        // 发布事件（编辑）
        info = msgInfoMapper.selectByPrimaryKey(param.getId());
        this.publishEvent(BusinessOperationEnum.UPDATE, SystemReturnCode.SUCCESS, old, info, null);
        logger.info("消息[{}]设置定时发布：{}", param.getId(), param.getSenderTime());
    }

    @Override
    public void cancelSchedule(String messageId) {
        MsgInfo old = msgInfoMapper.selectByPrimaryKey(messageId);
        if (old == null) {
            throw new ResourceNotFoundException("消息信息不存在");
        }
        int rows = msgInfoMapper.clearSchedule(messageId, CurrentUser.getInfo().getUserId(), new Date(),
                DictionaryCodeEnum.PUBLISH_STATUS_WAIT.getCode());
        if (rows == 0) {
            // 已发布、已撤回，或者没有设置定时发布
            throw new UoquoException(MessageReturnCode.PUBLISH_ERROR);
        }
        MsgInfo info = msgInfoMapper.selectByPrimaryKey(messageId);
        this.publishEvent(BusinessOperationEnum.UPDATE, SystemReturnCode.SUCCESS, old, info, null);
    }

    @Override
    public int publishDueMessages(int limit) {
        List<String> ids = msgInfoMapper.listDueIds(DictionaryCodeEnum.PUBLISH_STATUS_WAIT.getCode(), new Date(), clamp(limit, 50, 500));
        int count = 0;
        for (String id : ids) {
            try {
                if (this.publishDueMessage(id)) {
                    count++;
                }
            } catch (Exception e) {
                // 单条失败不影响其他消息；失败的消息仍然是待发布状态，下次继续尝试
                logger.error("定时发布消息[{}]失败：", id, e);
            }
        }
        if (count > 0) {
            logger.info("定时发布消息完成：待发布[{}]条，已发布[{}]条", ids.size(), count);
        }
        return count;
    }

    /**
     * 发布一条到点的消息：发送人为消息的创建人，发布范围、推送方式沿用设置定时发布时保存的内容
     * @return true 表示由本次调用发布；false 表示已被其他节点发布、已撤回、已取消定时或时间未到
     */
    private boolean publishDueMessage(String id) {
        MsgInfo old = msgInfoMapper.selectByPrimaryKey(id);
        Date now = new Date();
        if (old == null || old.getDeleteState() == null || old.getDeleteState() != 0
                || !DictionaryCodeEnum.PUBLISH_STATUS_WAIT.getCode().equals(old.getStatus())
                || old.getSenderTime() == null || old.getSenderTime().after(now)) {
            return false;
        }
        MsgInfo info = new MsgInfo();
        info.setId(id);
        // 发布人沿用设置定时发布时保存的发布人（执行“发布”操作的人）；没有时（异常数据）才退回到创建人
        String senderId = old.getSenderId();
        String senderName = old.getSenderName();
        if (StringUtil.isNull(senderId)) {
            senderId = old.getCreateUser();
            senderName = userInfoService.mapNameByIds(Collections.singletonList(senderId)).get(senderId);
        }
        info.setSenderId(senderId);
        info.setSenderName(senderName);
        info.setSenderTime(now);
        if (old.getExpireTime() == null) {
            // 默认1个月过期（从实际发布时间开始计算）
            info.setExpireTime(new Date(now.getTime() + 30 * 24 * 60 * 60 * 1000L));
        }
        info.setStatus(DictionaryCodeEnum.PUBLISH_STATUS_DONE.getCode());
        info.setStatusTime(now);
        info.setStatusMemo("定时发布");
        info.setDescription(old.getDescription());
        info.setUpdateUser(senderId);
        info.setUpdateTime(now);
        int rows = msgInfoMapper.updatePublishStatus(info, DictionaryCodeEnum.PUBLISH_STATUS_WAIT.getCode());
        if (rows == 0) {
            return false;
        }
        info = msgInfoMapper.selectByPrimaryKey(id);
        this.publishEvent(BusinessOperationEnum.MESSAGE_PUBLISH, SystemReturnCode.SUCCESS, old, info, null);
        return true;
    }

    @Override
    public int clearExpiredMessages(int retainDays, int limit, int maxSeconds) {
        long now = System.currentTimeMillis();
        Date before = new Date(now - clamp(retainDays, 30, 3650) * 24 * 60 * 60 * 1000L);
        long deadline = now + clamp(maxSeconds, 20, 600) * 1000L;
        List<String> statuses = Arrays.asList(DictionaryCodeEnum.PUBLISH_STATUS_DONE.getCode(), DictionaryCodeEnum.PUBLISH_STATUS_UNDO.getCode());
        List<String> ids = msgInfoMapper.listExpiredIds(statuses, before, clamp(limit, 20, 200));
        int count = 0;
        // 是否因为超时提前结束（还有没清理完的）
        boolean more = false;
        for (String id : ids) {
            if (System.currentTimeMillis() > deadline) {
                more = true;
                break;
            }
            try {
                long deleteState = System.currentTimeMillis();
                // 1. 先分批删除接收记录（一条消息可能有几万、几十万条），没删完就先结束，下次调用时该消息仍会被查到，继续删除
                boolean finished = false;
                while (System.currentTimeMillis() <= deadline) {
                    int deleted = msgReceiverMapper.deleteByMessageLimit(id, deleteState, CLEAR_RECEIVER_BATCH);
                    if (deleted < CLEAR_RECEIVER_BATCH) {
                        finished = true;
                        break;
                    }
                }
                if (!finished) {
                    more = true;
                    break;
                }
                // 2. 再删除消息本身（不逐条发布事件，避免大量事件）
                msgInfoMapper.deleteByPrimaryKey(id, deleteState);
                count++;
            } catch (Exception e) {
                logger.error("清理过期消息[{}]失败：", id, e);
            }
        }
        if (count > 0) {
            logger.info("清理过期消息完成：过期时间早于[{}]的消息，待清理[{}]条，已清理[{}]条", before, ids.size(), count);
        }
        // 因超时提前结束时，至少返回1，提示调用方还有没清理完的，可以继续调用
        return more ? Math.max(count, 1) : count;
    }

    @Override
    public MsgFanoutDto claimFanout(MsgFanoutParam param) {
        Date now = new Date();
        int staleSeconds = Math.max(MsgFanoutParam.MIN_STALE_SECONDS, positive(param.getStaleSeconds(), MsgFanoutParam.DEFAULT_STALE_SECONDS));
        int maxRetry = positive(param.getMaxRetry(), MsgFanoutParam.DEFAULT_MAX_RETRY);
        MsgFanoutDto dto = new MsgFanoutDto();
        int rows = msgInfoMapper.claimFanout(param.getMessageId(), DictionaryCodeEnum.PUBLISH_STATUS_DONE.getCode(), now,
                new Date(now.getTime() - staleSeconds * 1000L), maxRetry);
        if (rows == 0) {
            return dto;
        }
        MsgInfo info = msgInfoMapper.selectByPrimaryKey(param.getMessageId());
        if (info == null) {
            return dto;
        }
        dto.setClaimed(true);
        dto.setRetry(info.getFanoutRetry() == null ? 0 : info.getFanoutRetry());
        dto.setCursor(info.getFanoutCursor());
        dto.setTotal(info.getFanoutTotal() == null ? 0 : info.getFanoutTotal());
        return dto;
    }

    @Override
    public boolean updateFanoutProgress(MsgFanoutParam param) {
        int rows = msgInfoMapper.updateFanoutProgress(param.getMessageId(), DictionaryCodeEnum.PUBLISH_STATUS_DONE.getCode(),
                param.getRetry() == null ? 0 : param.getRetry(), param.getCursor(),
                param.getTotal() == null ? 0 : param.getTotal(),
                Boolean.TRUE.equals(param.getFinished()), new Date());
        return rows > 0;
    }

    @Override
    public boolean heartbeatFanout(MsgFanoutParam param) {
        int rows = msgInfoMapper.heartbeatFanout(param.getMessageId(), DictionaryCodeEnum.PUBLISH_STATUS_DONE.getCode(),
                param.getRetry() == null ? 0 : param.getRetry(), new Date());
        return rows > 0;
    }

    @Override
    public List<MsgInfoDto> listStalledFanout(int staleSeconds, int maxRetry, int limit) {
        Date now = new Date();
        int stale = Math.max(MsgFanoutParam.MIN_STALE_SECONDS, positive(staleSeconds, MsgFanoutParam.DEFAULT_STALE_SECONDS));
        int retry = positive(maxRetry, MsgFanoutParam.DEFAULT_MAX_RETRY);
        Date staleTime = new Date(now.getTime() - stale * 1000L);
        // 接管次数已用完的任务标记为失败，不再继续
        String doneStatus = DictionaryCodeEnum.PUBLISH_STATUS_DONE.getCode();
        int failed = msgInfoMapper.failExhaustedFanout(doneStatus, staleTime, retry);
        if (failed > 0) {
            logger.error("有[{}]条消息的群发多次中断，接管次数已用完，已标记为失败，请人工处理", failed);
        }
        List<MsgInfo> list = msgInfoMapper.listStalledFanout(doneStatus, now, staleTime, retry, clamp(limit, 10, 100));
        List<MsgInfoDto> result = new ArrayList<>(list.size());
        for (MsgInfo item : list) {
            result.add(this.convertInfo2Dto(item));
        }
        return result;
    }

    /** 过期清理时，每批删除的接收记录条数 */
    private static final int CLEAR_RECEIVER_BATCH = 5000;

    /** 入参为空或不合法（小于等于0）时使用默认值，并限制最大值 */
    private static int clamp(int value, int defaultValue, int max) {
        return value <= 0 ? defaultValue : Math.min(value, max);
    }

    private static int positive(Integer value, int defaultValue) {
        return (value == null || value <= 0) ? defaultValue : value;
    }

    @Override
    public void retryPushMessage(String logId, String messageId, String receiverId) {
        MsgInfo info = msgInfoMapper.selectByPrimaryKey(messageId);
        if (info == null) {
            throw new ResourceNotFoundException("消息信息不存在");
        }
        // TODO 重新推送
    }

    @Override
    public void withdrawMessage(String messageId) {
        MsgInfo old = msgInfoMapper.selectByPrimaryKey(messageId);
        if (old == null) {
            throw new ResourceNotFoundException("消息信息不存在");
        }
        // 撤销，已发布
        MsgInfo info = new MsgInfo();
        info.setId(messageId);
        info.setStatus(DictionaryCodeEnum.PUBLISH_STATUS_UNDO.getCode());
        info.setStatusTime(new Date());
        info.setStatusMemo("");
        info.setUpdateUser(CurrentUser.getInfo().getUserId());
        info.setUpdateTime(new Date());
        msgInfoMapper.updateStatus(info);
        // 发布事件（撤销）
        info = msgInfoMapper.selectByPrimaryKey(messageId);
        this.publishEvent(BusinessOperationEnum.MESSAGE_WITHDRAW, SystemReturnCode.SUCCESS, old, info, null);
    }

    @Override
    public PageResult<MsgInfoDto> listMessage(MsgInfoListParam param) {
        // 拼接条件
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("createUser",   param.getCreateUser());
        paramMap.put("receiverId",   param.getReceiverId());
        paramMap.put("messageTitle", param.getMessageTitle());
        paramMap.put("messageType",  param.getMessageType());
        paramMap.put("messageLevel", param.getMessageLevel());
        paramMap.put("businessType", param.getBusinessType());
        paramMap.put("status", param.getStatus());
        if (param.getCreateTimeStart() != null) {
            paramMap.put("createTimeStart", param.getCreateTimeStart());
            paramMap.put("createTimeEnd",   param.getCreateTimeEnd() == null ? new Date() : param.getCreateTimeEnd());
        }
        if (param.getSenderTimeStart() != null) {
            paramMap.put("senderTimeStart", param.getSenderTimeStart());
            paramMap.put("senderTimeEnd",   param.getSenderTimeEnd() == null ? new Date() : param.getSenderTimeEnd());
        }
        if (param.getExpireTimeStart() != null) {
            paramMap.put("expireTimeStart", param.getExpireTimeStart());
            paramMap.put("expireTimeEnd",   param.getExpireTimeEnd() == null ? new Date() : param.getExpireTimeEnd());
        }
        // 分页查询
        PageHelper.startPage(param.getPageNum(), param.getPageSize());
        PageList<MsgInfo> list = (PageList<MsgInfo>) msgInfoMapper.listBySearch(paramMap);
        // 批量查询创建人、更新人、目标机构名称
        List<String> userIds = new ArrayList<>();
        List<String> instituteIds = new ArrayList<>();
        for (MsgInfo item : list.getResult()) {
            userIds.add(item.getCreateUser());
            userIds.add(item.getUpdateUser());
            instituteIds.add(item.getReceiverInstituteId());
        }
        Map<String, String> userNames = userInfoService.mapNameByIds(userIds);
        Map<String, String> instituteNames = instituteInfoService.mapNameByIds(instituteIds);
        // 对象转换
        List<MsgInfoDto> resultList = new ArrayList<>();
        for (MsgInfo item : list.getResult()) {
            MsgInfoDto dto = this.convertInfo2Dto(item);
            dto.setCreateUserName(userNames.get(item.getCreateUser()));
            dto.setUpdateUserName(userNames.get(item.getUpdateUser()));
            dto.setReceiverInstituteName(instituteNames.get(item.getReceiverInstituteId()));
            resultList.add(dto);
        }
        return PageResult.of(list, resultList);
    }

    @Override
    public PageResult<MsgInfoViewDto> listMessage4View(MsgInfoListParam param) {
        // 1. 拼接条件
        Map<String, Object> paramMap = new HashMap<>();
        // 我的消息
        paramMap.put("receiverId",   CurrentUser.getInfo().getUserId());
        paramMap.put("messageType",  param.getMessageType());
        paramMap.put("messageLevel", param.getMessageLevel());
        paramMap.put("businessType", param.getBusinessType());
        paramMap.put("readState",    param.getReadState());
        paramMap.put("processedState", param.getProcessedState());
        // 已发布
        paramMap.put("status", DictionaryCodeEnum.PUBLISH_STATUS_DONE.getCode());
        if (param.getSenderTimeStart() != null) {
            paramMap.put("senderTimeStart", param.getSenderTimeStart());
            paramMap.put("senderTimeEnd",   param.getSenderTimeEnd() == null ? new Date() : param.getSenderTimeEnd());
        }
        // 2. 分页查询
        PageHelper.startPage(param.getPageNum(), param.getPageSize());
        PageList<MsgInfoViewDto> list = (PageList<MsgInfoViewDto>) msgInfoViewMapper.listBySearch(paramMap);
        return PageResult.of(list);
    }

    @Override
    public List<MsgInfoViewDto> listUnreadMessage(@Nullable Date startTime, @Nullable Date endTime) {
        // 1. 拼接条件
        Map<String, Object> paramMap = new HashMap<>();
        // 我的消息
        paramMap.put("receiverId",   CurrentUser.getInfo().getUserId());
        // 未阅读
        paramMap.put("readState",    false);
        // 已发布
        paramMap.put("status",       DictionaryCodeEnum.PUBLISH_STATUS_DONE.getCode());
        // 未过期
        paramMap.put("expireTime",   DateUtil.getDayStart(new Date()));
        // 2. 分页查询（防止过多导致溢出）
        PageHelper.startPage(1, 100);
        PageList<MsgInfoViewDto> list = (PageList<MsgInfoViewDto>) msgInfoViewMapper.listBySearch(paramMap);
        return list.getResult();
    }

    @Override
    public MsgInfoDto getMessageDetail(String messageId) {
        MsgInfo info = msgInfoMapper.selectByPrimaryKey(messageId);
        if (info == null) {
            throw new ResourceNotFoundException("消息信息不存在");
        }
        MsgInfoDto dto = this.convertInfo2Dto(info);
        // 补充创建人、更新人、目标所属机构名称（目标机构主要用于编辑时的回显）
        Map<String, String> userNames = userInfoService.mapNameByIds(Arrays.asList(info.getCreateUser(), info.getUpdateUser()));
        dto.setCreateUserName(userNames.get(info.getCreateUser()));
        dto.setUpdateUserName(userNames.get(info.getUpdateUser()));
        dto.setReceiverInstituteName(instituteInfoService.mapNameByIds(
                Collections.singletonList(info.getReceiverInstituteId())).get(info.getReceiverInstituteId()));
        // 补充附件信息
        List<MsgAttachmentDto> result = this.getAttachments(messageId);
        dto.setAttachments(result);
        return dto;
    }

    @Override
    public MsgInfoViewDto getMessage4View(String messageId) {
        MsgInfoViewDto dto = msgInfoViewMapper.selectByMessageAndReceiver(messageId, CurrentUser.getInfo().getUserId());
        if (dto == null) {
            throw new ResourceNotFoundException("消息信息不存在");
        }
        // 标记为已读
        if (dto.getReadState() == null || !dto.getReadState()) {
            MsgReceiver info = new MsgReceiver();
            info.setReadState(true);
            info.setReadTime(new Date());
            info.setDescription("");
            CurrentUser.UserInfo loginUser = CurrentUser.getInfo();
            info.setUpdateUser(loginUser.getUserId());
            info.setUpdateTime(new Date());
            info.setId(dto.getRecordId());
            msgReceiverMapper.updateReadStatus(info);
        }
        // 补充附件信息
        List<MsgAttachmentDto> result = this.getAttachments(messageId);
        dto.setAttachments(result);
        return dto;
    }

    @Override
    public void downloadAttachment(String attachmentId, boolean checkAuth, HttpServletRequest request, HttpServletResponse response) {
        MsgAttachment attachment = msgAttachmentMapper.selectByPrimaryKey(attachmentId);
        if (attachment == null) {
            throw new ResourceNotFoundException("附件信息不存在");
        }
        // 1. 权限校验
        MsgInfo old = null;
        if (checkAuth) {
            MsgReceiver receiver = msgReceiverMapper.selectByMessageAndReceiver(attachment.getMessageId(), CurrentUser.getInfo().getUserId());
            if (receiver == null) {
                old = msgInfoMapper.selectByPrimaryKey(attachment.getMessageId());
                if (old == null) {
                    throw new ResourceNotFoundException("消息信息不存在");
                } else {
                    // 不是接收人时，只有消息的发布人或创建人可以下载（二者不一定是同一个人）
                    String userId = CurrentUser.getInfo().getUserId();
                    boolean isSender = StringUtil.notNull(userId) && userId.equals(old.getSenderId());
                    boolean isCreator = StringUtil.notNull(userId) && userId.equals(old.getCreateUser());
                    if (!isSender && !isCreator) {
                        throw new ForbiddenException("您无权查看该附件");
                    }
                }
            }
        }
        // 2. 下载（输出）文件
        try {
            DownloadConfigParam param = new DownloadConfigParam();
            param.setFilePath(attachment.getFilePath());
            param.setFileName(attachment.getFileName());
            fileDownloadService.downloadByStream(param, response);
        } catch (AbstractBaseException e) {
            throw e;
        } catch (Exception e) {
            logger.warn("下载附件[{}]出错：{}", attachmentId, e.getMessage());
            throw new UoquoException(MessageReturnCode.DOWNLOAD_ATTACHMENT_ERROR);
        }
        // 3. 更新下载次数
        try {
            if (old == null) {
                old = msgInfoMapper.selectByPrimaryKey(attachment.getMessageId());
            }
            msgAttachmentMapper.updateDownloadCount(attachmentId, attachment.getDownloadCount() + 1);
            // 事件发布（附件下载）
            MsgInfo info = msgInfoMapper.selectByPrimaryKey(attachment.getMessageId());
            Map<String, String> map = new HashMap<>();
            map.put("attachmentId", attachment.getId());
            map.put("fileName", attachment.getFileName());
            map.put("filePath", attachment.getFilePath());
            this.publishEvent(BusinessOperationEnum.DOWNLOAD, SystemReturnCode.SUCCESS, old, info, map);
        } catch (Exception e) {
            // 文输出流在文件下载后，已经关闭，所以忽略此处的异常
        }
    }

    /**
     * 将info转换为dto
     */
    private MsgInfoDto convertInfo2Dto(MsgInfo info) {
        MsgInfoDto dto = new MsgInfoDto();
        BeanUtils.copyProperties(info, dto);
        return dto;
    }

    /**
     * 获取附件列表
     */
    private List<MsgAttachmentDto> getAttachments(String messageId) {
        List<MsgAttachment> list = msgAttachmentMapper.listByMessageId(messageId);
        List<MsgAttachmentDto> result = new ArrayList<>();
        list.forEach(item -> {
            MsgAttachmentDto dto = new MsgAttachmentDto();
            BeanUtils.copyProperties(item, dto);
            dto.setShowPath(staticHost + dto.getFilePath());
            result.add(dto);
        });
        return result;
    }

    /**
     * 发布消息事件
     */
    private void publishEvent(BusinessOperationEnum type, BaseReturnCode status, MsgInfo oldInfo, MsgInfo newInfo, Map<String, String> map) {
        RemoteEvent<MsgInfo> event = new RemoteEvent<>(BusinessTypeEnum.MESSAGE.getCode(), type.getCode(), status.getCode());
        event.setBusinessSubType("MESSAGE");
        event.setOldData(oldInfo);
        event.setNewData(newInfo);
        // 补充业务信息
        MsgInfo info = (newInfo == null) ? oldInfo : newInfo;
        if (info != null) {
            event.setBusinessId(info.getId());
            event.setBusinessInstituteId(info.getReceiverInstituteId());
        }
        event.setRemarks(status.getText());
        event.setExtension(map);
        eventPublisher.publishEvent(event);
    }
}
