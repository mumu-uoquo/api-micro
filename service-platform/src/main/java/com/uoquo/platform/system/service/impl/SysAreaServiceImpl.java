package com.uoquo.platform.system.service.impl;

import com.uoquo.platform.system.mapper.SysAreaMapper;
import com.uoquo.platform.system.model.dto.SysAreaDto;
import com.uoquo.platform.system.model.pojo.SysArea;
import com.uoquo.platform.system.service.SysAreaService;
import com.uoquo.utils.StringUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SysAreaServiceImpl implements SysAreaService {

    @Resource
    private SysAreaMapper sysAreaMapper;

    @Override
    public List<SysAreaDto> selectByTree4PrevCode(String prevCode) {
        List<SysArea> list = sysAreaMapper.selectByPrevCode(prevCode);
        if (StringUtil.isNull(prevCode)) {
            return filter2Province(list);
        } else if (prevCode.length() == 2) {
            return filter2City(prevCode, list);
        } else if (prevCode.length() == 4) {
            return filter2County(prevCode, list);
        }
        return new ArrayList<>();
    }

    /**
     * 省
     */
    private List<SysAreaDto> filter2Province(final List<SysArea> list) {
        List<SysArea> provinceList = list.stream()
                .filter(item -> item.getDistrictCode().endsWith("0000"))
                .sorted(Comparator.comparingInt(SysArea::getSortIdx))
                .collect(Collectors.toList());
        List<SysAreaDto> result = new ArrayList<>();
        for (SysArea item : provinceList) {
            SysAreaDto dto = new SysAreaDto();
            BeanUtils.copyProperties(item, dto);
            dto.setFullName(item.getProvince());
            dto.setShortName(item.getProvinceShort());
            dto.setAbbrName(item.getProvinceAbbr());
            // 补全子节点
            dto.setChildren(filter2City(dto.getDistrictCode().substring(0, 2), list));
            result.add(dto);
        }
        return result;
    }

    /**
     * 市
     */
    private List<SysAreaDto> filter2City(final String provinceCode, final List<SysArea> list) {
        List<SysArea> cityList = list.stream()
                .filter(item -> item.getDistrictCode().startsWith(provinceCode) && !item.getDistrictCode().endsWith("0000") && item.getDistrictCode().endsWith("00"))
                .sorted(Comparator.comparingInt(SysArea::getSortIdx))
                .collect(Collectors.toList());
        List<SysAreaDto> result = new ArrayList<>();
        for (SysArea item : cityList) {
            SysAreaDto dto = new SysAreaDto();
            BeanUtils.copyProperties(item, dto);
            dto.setFullName(item.getCity());
            dto.setShortName(item.getCity());
            dto.setAbbrName(item.getCity());
            // 补全子节点
            dto.setChildren(filter2County(dto.getDistrictCode().substring(0, 4), list));
            result.add(dto);
        }
        return result;
    }

    /**
     * 区
     */
    private List<SysAreaDto> filter2County(final String cityCode, final List<SysArea> list) {
        List<SysArea> countyList = list.stream()
                .filter(item -> item.getDistrictCode().startsWith(cityCode) && !item.getDistrictCode().endsWith("00"))
                .sorted(Comparator.comparingInt(SysArea::getSortIdx))
                .collect(Collectors.toList());
        List<SysAreaDto> result = new ArrayList<>();
        for (SysArea item : countyList) {
            SysAreaDto dto = new SysAreaDto();
            BeanUtils.copyProperties(item, dto);
            dto.setFullName(item.getCounty());
            dto.setShortName(item.getCounty());
            dto.setAbbrName(item.getCounty());
            result.add(dto);
        }
        return result;
    }
}
