package com.uoquo.platform.system.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uoquo.mybatis.page.PageList;
import com.uoquo.mybatis.page.PageResult;
import com.uoquo.platform.system.mapper.SysReturnCodeMapper;
import com.uoquo.platform.system.model.dto.SysReturnCodeDto;
import com.uoquo.platform.system.model.param.SysReturnCodeParam;
import com.uoquo.platform.system.model.param.SysReturnCodeSearchParam;
import com.uoquo.platform.system.model.pojo.SysReturnCode;
import com.uoquo.platform.system.service.SysReturnCodeService;
import com.uoquo.utils.IDGenerator;
import com.uoquo.utils.StringUtil;
import com.uoquo.web.exception.ResourceNotFoundException;
import com.uoquo.web.mybatis.page.PageHelper;

import jakarta.annotation.Resource;

@Service
public class SysReturnCodeServiceImpl implements SysReturnCodeService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    private SysReturnCodeMapper sysReturnCodeMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String saveReturnCode(SysReturnCodeParam param) {
        // 1. 校验响应码是否已存在
        SysReturnCode old = sysReturnCodeMapper.selectByReturnCode(param.getReturnCode());
        // 2. 保存
        SysReturnCode info = new SysReturnCode();
        BeanUtils.copyProperties(param, info);
        if (old == null) {
            // 新增
            info.setId(IDGenerator.getNextULID());
            sysReturnCodeMapper.insert(info);
            logger.info("新增系统响应码成功，ID: {}, Code: {}", info.getId(), info.getReturnCode());
        } else {
            // 修改
            info.setId(old.getId());
            sysReturnCodeMapper.updateByPrimaryKey(info);
            logger.info("修改系统响应码成功，ID: {}, Code: {}", info.getId(), info.getReturnCode());
        }

        return info.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteReturnCode(String id) {
        // 1. 校验是否存在
        SysReturnCode existCode = sysReturnCodeMapper.selectByPrimaryKey(id);
        if (existCode == null) {
            throw new ResourceNotFoundException("系统响应码不存在");
        }
        // 2. 删除
        sysReturnCodeMapper.deleteByPrimaryKey(id);
        logger.info("删除系统响应码成功，ID: {}", id);
    }

    @Override
    public SysReturnCodeDto getReturnCodeById(String id) {
        SysReturnCode returnCode = sysReturnCodeMapper.selectByPrimaryKey(id);
        if (returnCode == null) {
            throw new ResourceNotFoundException("系统响应码不存在");
        }
        return convert2Dto(returnCode);
    }

    @Override
    public SysReturnCodeDto getReturnCodeByCode(String returnCode) {
        SysReturnCode info = sysReturnCodeMapper.selectByReturnCode(returnCode);
        if (info == null) {
            return null;
        }
        return convert2Dto(info);
    }

    @Override
    public PageResult<SysReturnCodeDto> listReturnCodeByPage(SysReturnCodeSearchParam param) {
        // 1. 拼接查询条件
        Map<String, Object> paramMap = new HashMap<>();
        if (StringUtil.notNull(param.getReturnCode())) {
            paramMap.put("returnCode", param.getReturnCode());
        }
        // 2. 分页查询
        PageHelper.startPage(param.getPageNum(), param.getPageSize());
        PageList<SysReturnCode> list = (PageList<SysReturnCode>) sysReturnCodeMapper.selectBySearch(paramMap);
        // 3. 对象转换
        List<SysReturnCodeDto> resultList = new ArrayList<>();
        for (SysReturnCode item : list.getResult()) {
            resultList.add(convert2Dto(item));
        }
        return PageResult.of(list, resultList);
    }

    @Override
    public List<SysReturnCodeDto> listAllReturnCodes() {
        List<SysReturnCode> list = sysReturnCodeMapper.selectAll();
        List<SysReturnCodeDto> resultList = new ArrayList<>();
        for (SysReturnCode item : list) {
            // 只返回需要的字段
            SysReturnCodeDto dto = new SysReturnCodeDto();
            dto.setReturnCode(item.getReturnCode());
            dto.setReturnValue(item.getReturnValue());
            resultList.add(dto);
        }
        return resultList;
    }

    /**
     * 对象转换
     */
    private SysReturnCodeDto convert2Dto(SysReturnCode info) {
        SysReturnCodeDto dto = new SysReturnCodeDto();
        BeanUtils.copyProperties(info, dto);
        return dto;
    }
}
