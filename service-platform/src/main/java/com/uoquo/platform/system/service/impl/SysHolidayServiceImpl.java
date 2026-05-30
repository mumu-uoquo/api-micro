package com.uoquo.platform.system.service.impl;

import com.uoquo.platform.system.mapper.SysHolidayMapper;
import com.uoquo.platform.system.model.dto.SysHolidayDto;
import com.uoquo.platform.system.model.param.SysHolidayInfoParam;
import com.uoquo.platform.system.model.param.SysHolidaySearchParam;
import com.uoquo.platform.system.model.pojo.SysHoliday;
import com.uoquo.platform.system.service.SysHolidayService;
import com.uoquo.utils.DateUtil;
import com.uoquo.utils.IDGenerator;
import com.uoquo.utils.StringUtil;
import com.uoquo.utils.json.JsonUtil;
import com.uoquo.web.exception.ResourceNotFoundException;
import com.uoquo.web.exception.SystemErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class SysHolidayServiceImpl implements SysHolidayService {
    Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private SysHolidayMapper sysHolidayMapper;

    @Override
    public String saveHolidayInfo(SysHolidayInfoParam param) {
        // 基本校验
        SysHoliday old = sysHolidayMapper.selectByDate(param.getDateValue());
        if (old != null) {
            throw new SystemErrorException("日期[%s]已经存在", DateUtil.toString(param.getDateValue(), DateUtil.FORMAT_DATE));
        }
        // 保存
        SysHoliday info = new SysHoliday();
        BeanUtils.copyProperties(param, info);
        info.setId(IDGenerator.getNextULID());
        sysHolidayMapper.insert(info);
        return info.getId();
    }

    @Override
    public void batchSaveHolidayInfo(List<SysHolidayInfoParam> params) {
        // 预处理
        List<SysHoliday> list = new ArrayList<>(params.size());
        List<String> errList = new ArrayList<>();
        List<Long> repeatDateList = new ArrayList<>();
        params.forEach(item -> {
            // 将日期转换为当天开始
            Date date = DateUtil.getDayStart(item.getDateValue());
            // 基本校验
            SysHoliday old = sysHolidayMapper.selectByDate(date);
            if (old != null || repeatDateList.contains(date.getTime())) {
                errList.add(DateUtil.toString(date, DateUtil.FORMAT_DATE));
                return;
            }
            // 创建对象
            SysHoliday info = new SysHoliday();
            BeanUtils.copyProperties(item, info);
            info.setId(IDGenerator.getNextULID());
            info.setDateValue(date);
            list.add(info);
            repeatDateList.add(date.getTime());
        });
        // 保存
        if (!errList.isEmpty()) {
            throw new SystemErrorException("日期[%s]已经存在", String.join(", ", errList));
        }
        sysHolidayMapper.batchInsert(list);
    }

    @Override
    public void updateHolidayInfo(SysHolidayInfoParam param) {
        // 查询现有的
        SysHoliday old;
        if (StringUtil.notNull(param.getId())) {
            old = sysHolidayMapper.selectByPrimaryKey(param.getId());
        } else {
            old = sysHolidayMapper.selectByDate(param.getDateValue());
        }
        if (old == null) {
            throw new ResourceNotFoundException("待更新的信息不存在");
        }
        if (logger.isDebugEnabled()) {
            logger.debug("将节假日[{}]更新为[{}]", JsonUtil.serialize(old), JsonUtil.serialize(param));
        }
        // 更新数据
        old.setDateValue(param.getDateValue());
        old.setDateType(param.getDateType());
        old.setDescription(param.getDescription());
        sysHolidayMapper.updateByPrimaryKey(old);
    }

    @Override
    public void deleteHolidayInfo(String id) {
        SysHoliday old = sysHolidayMapper.selectByPrimaryKey(id);
        if (old == null) {
            throw new ResourceNotFoundException("节假日信息不存在");
        }
        sysHolidayMapper.deleteByPrimaryKey(id);
    }

    @Override
    public List<SysHolidayDto> listHolidayInfo(SysHolidaySearchParam param) {
        // 参数有效性处理
        if (param == null) {
            param = new SysHolidaySearchParam();
        }
        if (param.getStartDate() == null && param.getEndDate() == null) {
            Date today = new Date();
            param.setStartDate(DateUtil.getYearStart(today));
            param.setEndDate(DateUtil.getYearEnd(today));
        } else if (param.getStartDate() == null) {
            param.setStartDate(DateUtil.getYearStart(param.getEndDate()));
        } else if (param.getEndDate() == null) {
            param.setEndDate(DateUtil.getYearEnd(param.getStartDate()));
        }
        // 查询数据
        List<SysHoliday> list = sysHolidayMapper.listByDateRang(param.getStartDate(), param.getEndDate());
        List<SysHolidayDto> result = new ArrayList<>(list.size());
        for (SysHoliday item : list) {
            SysHolidayDto dto = new SysHolidayDto();
            BeanUtils.copyProperties(item, dto);
            result.add(dto);
        }
        return result;
    }

}
