package com.uoquo.platform.system.service.impl;

import com.uoquo.platform.common.DictionaryCodeEnum;
import com.uoquo.platform.system.mapper.SysDictionaryMapper;
import com.uoquo.platform.system.model.dto.SysDictionaryDto;
import com.uoquo.platform.system.model.dto.SysDictionarySimpleDto;
import com.uoquo.platform.system.model.param.SysDictionaryParam;
import com.uoquo.platform.system.model.pojo.SysDictionary;
import com.uoquo.platform.system.service.SysDictionaryService;
import com.uoquo.utils.IDGenerator;
import com.uoquo.utils.StringUtil;
import com.uoquo.platform.common.BaseConstant;
import com.uoquo.web.exception.ForbiddenException;
import com.uoquo.web.exception.ResourceNotFoundException;
import com.uoquo.web.exception.SystemErrorException;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class SysDictionaryServiceImpl implements SysDictionaryService {

    @Resource
    private SysDictionaryMapper sysDictionaryMapper;


    @Override
    public String addInfo(SysDictionaryParam param) {
        // 1. 基础校验
        SysDictionary old = sysDictionaryMapper.selectByCode(param.getDicCode());
        if (old != null) {
            throw new SystemErrorException("编码[%s]已经存在", param.getDicCode());
        }
        // 2. 添加数据
        // 组装数据
        SysDictionary info = new SysDictionary();
        info.setId(IDGenerator.getNextULID());
        info.setDicCode(param.getDicCode());
        info.setDicValue(param.getDicValue());
        info.setSortIdx(param.getSortIdx());
        info.setTagStyle(param.getTagStyle());
        info.setDeleteState(BaseConstant.NOT_DELETED);
        // 默认为通用字典（可删除）
        if (StringUtil.isNull(param.getDicType())) {
            info.setDicType(DictionaryCodeEnum.ROLE_TYPE_NORMAL.getCode());
        } else {
            info.setDicType(param.getDicType());
        }
        // 执行新增
        sysDictionaryMapper.insert(info);
        return info.getId();
    }

    @Override
    public String updateInfo(SysDictionaryParam param) {
        // 1. 基础校验
        SysDictionary old = sysDictionaryMapper.selectById(param.getId());
        if (old == null) {
            throw new ResourceNotFoundException("字典信息不存在");
        }
        // 2. 更新数据
        // 组装数据
        SysDictionary info = new SysDictionary();
        info.setId(param.getId());
        info.setDicValue(param.getDicValue());
        info.setSortIdx(param.getSortIdx());
        info.setTagStyle(param.getTagStyle());
        // 非内置字典才允许修改字典类型
        if (!DictionaryCodeEnum.ROLE_TYPE_INNER.getCode().equals(old.getDicType())) {
            info.setDicType(param.getDicType());
        }
        // 执行更新
        sysDictionaryMapper.update(info);
        return param.getId();
    }

    @Override
    public int deleteInfo(String id) {
        // 1. 基础校验
        SysDictionary old = sysDictionaryMapper.selectById(id);
        if (old == null) {
            throw new ResourceNotFoundException("字典信息不存在");
        } else if (DictionaryCodeEnum.ROLE_TYPE_INNER.getCode().equals(old.getDicType())) {
            throw new ForbiddenException("无权删除内置字典");
        }
        // 2. 删除数据
        old.setDeleteState(System.currentTimeMillis());
        int rows = sysDictionaryMapper.deleteByPrimaryKey(id, old.getDeleteState());
        return rows;
    }

    @Override
    public List<SysDictionaryDto> listByPrefix(String codePrefix) {
        List<SysDictionary> list = null;
        if (StringUtil.isNull(codePrefix)) {
            // 查询一级字典
            list = sysDictionaryMapper.selectByRoot();
        } else {
            list = sysDictionaryMapper.selectByPrevCode(codePrefix);
        }
        // 对象转换
        List<SysDictionaryDto> result = new ArrayList<>(list.size());
        for (SysDictionary sysDictionary : list) {
            SysDictionaryDto dicDTO = new SysDictionaryDto();
            BeanUtils.copyProperties(sysDictionary, dicDTO);
            result.add(dicDTO);
        }
        result.sort(Comparator.comparing(SysDictionaryDto::getSortIdx));
        return result;
    }

    /**
     * 获取所有的字典信息
     */
    @Override
    public List<SysDictionaryDto> listAllDictionary() {
        List<SysDictionary> list = sysDictionaryMapper.selectAll();
        // 对象转换
        List<SysDictionaryDto> result = new ArrayList<>();
        list.stream()
                // 查找所有一级字典
                .filter(item  -> item.getDicCode().length() == 3)
                .sorted(Comparator.comparing(SysDictionary::getSortIdx))
                .forEach(item -> {
                    // 查找对应的子级字典
                    List<SysDictionaryDto> children = new ArrayList<>();
                    list.stream()
                            .filter(child -> child.getDicCode().startsWith(item.getDicCode()) && !child.getDicCode().equals(item.getDicCode()) )
                            .sorted(Comparator.comparing(SysDictionary::getSortIdx))
                            .forEach(child -> {
                                SysDictionaryDto childDTO = new SysDictionaryDto();
                                BeanUtils.copyProperties(child, childDTO);
                                children.add(childDTO);
                            });
                    // 拼装返回的一级对象
                    SysDictionaryDto dto = new SysDictionaryDto();
                    BeanUtils.copyProperties(item, dto);
                    dto.setChildren(children);
                    result.add(dto);
                });
        return result;
    }

    @Override
    public List<SysDictionarySimpleDto> listSimpleByPrefix(String codePrefix) {
        List<SysDictionary> list = sysDictionaryMapper.selectByPrevCode(codePrefix);
        // 对象转换
        List<SysDictionarySimpleDto> result = new ArrayList<>(list.size());
        list.stream()
                .sorted(Comparator.comparing(SysDictionary::getSortIdx))
                .forEach(item -> {
                    SysDictionarySimpleDto dicDTO = new SysDictionarySimpleDto();
                    BeanUtils.copyProperties(item, dicDTO);
                    result.add(dicDTO);
                });
        return result;
    }

    @Override
    public List<SysDictionarySimpleDto> listSimpleByAll() {
        List<SysDictionary> list = sysDictionaryMapper.selectAll();
        // 对象转换
        List<SysDictionarySimpleDto> result = new ArrayList<>();
        list.stream()
                // 查找所有一级字典
                .filter(item  -> item.getDicCode().length() == 3)
                .sorted(Comparator.comparing(SysDictionary::getSortIdx))
                .forEach(item -> {
                    // 查找对应的子级字典
                    List<SysDictionarySimpleDto> children = new ArrayList<>();
                    list.stream()
                            .filter(child -> child.getDicCode().startsWith(item.getDicCode()) && !child.getDicCode().equals(item.getDicCode()) )
                            .sorted(Comparator.comparing(SysDictionary::getSortIdx))
                            .forEach(child -> {
                                SysDictionarySimpleDto childDTO = new SysDictionarySimpleDto();
                                BeanUtils.copyProperties(child, childDTO);
                                children.add(childDTO);
                            });
                    // 拼装返回的一级对象
                    SysDictionarySimpleDto dto = new SysDictionarySimpleDto();
                    BeanUtils.copyProperties(item, dto);
                    dto.setChildren(children);
                    result.add(dto);
                });
        return result;
    }
}
