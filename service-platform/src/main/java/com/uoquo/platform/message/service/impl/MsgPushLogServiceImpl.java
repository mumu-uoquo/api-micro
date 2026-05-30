package com.uoquo.platform.message.service.impl;

import com.uoquo.platform.common.DictionaryCodeEnum;
import com.uoquo.platform.message.mapper.MsgPushLogMapper;
import com.uoquo.platform.message.model.dto.MsgPushLogDto;
import com.uoquo.platform.message.model.param.MsgInfoListParam;
import com.uoquo.platform.message.model.param.MsgPushLogParam;
import com.uoquo.platform.message.model.pojo.MsgPushLog;
import com.uoquo.platform.message.service.MsgPushLogService;
import com.uoquo.utils.IDGenerator;
import com.uoquo.utils.StringUtil;
import com.uoquo.web.exception.ResourceNotFoundException;
import com.uoquo.web.mybatis.page.PageHelper;
import com.uoquo.mybatis.page.PageList;
import com.uoquo.mybatis.page.PageResult;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import jakarta.annotation.Nullable;

@Service
public class MsgPushLogServiceImpl implements MsgPushLogService {

    @Autowired
    private MsgPushLogMapper msgPushLogMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String addPushLog(MsgPushLogParam param) {
        MsgPushLog info = new MsgPushLog();
        // 1. 组装日志对象
        if (StringUtil.isNull(param.getOldLogId())) {
            // 1.1 首次推送的日志
            BeanUtils.copyProperties(param, info);
            if (info.getRetryCount() == null) {
                info.setRetryCount(0);
            }
        } else {
            // 1.2 重试推送的日志
            MsgPushLog old = msgPushLogMapper.selectByPrimaryKey(param.getOldLogId());
            if (old == null) {
                throw new ResourceNotFoundException("推送日志信息不存在");
            }
            BeanUtils.copyProperties(old, info);
            info.setRetryCount(old.getRetryCount() + 1);
            // 旧日志状态更新为“已重试”
            msgPushLogMapper.updatePushStatus(param.getOldLogId(), DictionaryCodeEnum.PUSH_STATUS_RETRY.getCode());
        }
        // 2. 插入新日志
        info.setId(IDGenerator.getNextULID());
        info.setPushStatus(param.getPushStatus());
        info.setPushResult(param.getPushResult());
        info.setDescription(param.getDescription());
        if (info.getPushTime() == null) {
            info.setPushTime(new Date());
        }
        msgPushLogMapper.insert(info);
        return info.getId();
    }

    @Override
    public PageResult<MsgPushLogDto> listPushLogByMessageId(MsgInfoListParam param) {
        if (StringUtil.isNull(param.getMessageId())) {
            return PageResult.empty();
        }
        Map<String, Object> map = new HashMap<>();
        map.put("messageId", param.getMessageId());
        map.put("receiverId", param.getReceiverId());
        PageHelper.startPage(param.getPageNum(), param.getPageSize());
        PageList<MsgPushLog> list =  (PageList<MsgPushLog>) msgPushLogMapper.listBySearch(map);
        // 对象转换
        List<MsgPushLogDto> resultList = new ArrayList<>();
        for (MsgPushLog item : list.getResult()) {
            resultList.add(this.convert2Dto(item));
        }
        return PageResult.of(list, resultList);
    }

    @Override
    public List<MsgPushLogDto> listPushLogByFailed(@Nullable String messageId, @Nullable Date startTime, @Nullable Date endTime) {
        Map<String, Object> map = new HashMap<>();
        map.put("messageId", messageId);
        map.put("pushStatus", DictionaryCodeEnum.PUSH_STATUS_FAILED.getCode());
        if (startTime != null) {
            map.put("startTime", startTime);
            map.put("endTime", endTime == null ? new Date() : endTime);
        }
        // 分页查，防止数据量过大
        PageHelper.startPage(1, 100);
        PageList<MsgPushLog> list = (PageList<MsgPushLog>)msgPushLogMapper.listBySearch(map);
        return list.stream().map(this::convert2Dto).collect(Collectors.toList());
    }

    @Override
    public MsgPushLogDto getPushLogDetail(String logId) {
        MsgPushLog info = msgPushLogMapper.selectByPrimaryKey(logId);
        if (info == null) {
            throw new ResourceNotFoundException("推送日志信息不存在");
        }
        return convert2Dto(info);
    }

    private MsgPushLogDto convert2Dto(MsgPushLog info) {
        MsgPushLogDto dto = new MsgPushLogDto();
        BeanUtils.copyProperties(info, dto);
        return dto;
    }
}
