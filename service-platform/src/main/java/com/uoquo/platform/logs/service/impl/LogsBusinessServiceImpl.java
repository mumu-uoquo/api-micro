package com.uoquo.platform.logs.service.impl;

import com.uoquo.platform.logs.mapper.LogBusinessAccessMapper;
import com.uoquo.platform.logs.mapper.LogBusinessChangeMapper;
import com.uoquo.platform.logs.model.dto.LogBusinessAccessDto;
import com.uoquo.platform.logs.model.dto.LogBusinessChangeDto;
import com.uoquo.platform.logs.model.param.LogsBusinessSearchParam;
import com.uoquo.platform.logs.model.pojo.LogBusinessAccess;
import com.uoquo.platform.logs.model.pojo.LogBusinessChange;
import com.uoquo.platform.logs.service.LogsBusinessService;
import com.uoquo.utils.StringUtil;
import com.uoquo.web.mybatis.page.PageHelper;
import com.uoquo.mybatis.page.PageList;
import com.uoquo.mybatis.page.PageResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class LogsBusinessServiceImpl implements LogsBusinessService {
    final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private LogBusinessChangeMapper logBusinessChangeMapper;

    @Autowired
    private LogBusinessAccessMapper logBusinessAccessMapper;


    @Override
    public PageResult<LogBusinessChangeDto> listChangeLogs(LogsBusinessSearchParam param) {
        // 分页查询
        Map<String, Object> paramMap = new HashMap<>();
        if (StringUtil.notNull(param.getOperatorId())) {
            paramMap.put("operatorId", param.getOperatorId());
        }
        if (StringUtil.notNull(param.getToken())) {
            paramMap.put("token", param.getToken());
        }
        if (StringUtil.notNull(param.getBusinessType())) {
            paramMap.put("businessType", param.getBusinessType());
        }
        if (StringUtil.notNull(param.getBusinessId())) {
            paramMap.put("businessId", param.getBusinessId());
        }
        if (StringUtil.notNull(param.getInstituteId())) {
            paramMap.put("businessInstituteId", param.getInstituteId());
        }
        if (StringUtil.notNull(param.getOperationType())) {
            paramMap.put("operationType", param.getOperationType());
        }
        if (param.getOperationTimeStart() != null) {
            paramMap.put("operationTimeStart", param.getOperationTimeStart());
            paramMap.put("operationTimeEnd", param.getOperationTimeEnd() == null ? new Date() : param.getOperationTimeEnd());
        }
        PageHelper.startPage(param.getPageNum(), param.getPageSize());
        PageList<LogBusinessChange> list = (PageList<LogBusinessChange>) logBusinessChangeMapper.listBySearch(paramMap);
        // 对象转换
        List<LogBusinessChangeDto> resultList = new ArrayList<>();
        for (LogBusinessChange item : list.getResult()) {
            resultList.add(this.convert2Dto(item));
        }
        return PageResult.of(list, resultList);
    }

    @Override
    public PageResult<LogBusinessAccessDto> listAccessLogs(LogsBusinessSearchParam param) {
        // 分页查询
        Map<String, Object> paramMap = new HashMap<>();
        if (StringUtil.notNull(param.getOperatorId())) {
            paramMap.put("operatorId", param.getOperatorId());
        }
        if (StringUtil.notNull(param.getToken())) {
            paramMap.put("token", param.getToken());
        }
        if (StringUtil.notNull(param.getBusinessType())) {
            paramMap.put("businessType", param.getBusinessType());
        }
        if (StringUtil.notNull(param.getBusinessId())) {
            paramMap.put("businessId", param.getBusinessId());
        }
        if (StringUtil.notNull(param.getInstituteId())) {
            paramMap.put("businessInstituteId", param.getInstituteId());
        }
        if (StringUtil.notNull(param.getOperationType())) {
            paramMap.put("operationType", param.getOperationType());
        }
        if (param.getOperationTimeStart() != null) {
            paramMap.put("operationTimeStart", param.getOperationTimeStart());
            paramMap.put("operationTimeEnd", param.getOperationTimeEnd() == null ? new Date() : param.getOperationTimeEnd());
        }
        PageHelper.startPage(param.getPageNum(), param.getPageSize());
        PageList<LogBusinessAccess> list = (PageList<LogBusinessAccess>) logBusinessAccessMapper.listBySearch(paramMap);
        // 对象转换
        List<LogBusinessAccessDto> resultList = new ArrayList<>();
        for (LogBusinessAccess item : list.getResult()) {
            resultList.add(this.convert2Dto(item));
        }
        return PageResult.of(list, resultList);
    }

    private LogBusinessChangeDto convert2Dto(LogBusinessChange info) {
        LogBusinessChangeDto dto = new LogBusinessChangeDto();
        BeanUtils.copyProperties(info, dto);
        return dto;
    }

    private LogBusinessAccessDto convert2Dto(LogBusinessAccess info) {
        LogBusinessAccessDto dto = new LogBusinessAccessDto();
        BeanUtils.copyProperties(info, dto);
        return dto;
    }
}
