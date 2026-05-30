package com.uoquo.platform.system.service;

import com.uoquo.platform.system.model.dto.SysDictionaryDto;
import com.uoquo.platform.system.model.dto.SysDictionarySimpleDto;
import com.uoquo.platform.system.model.param.SysDictionaryParam;

import java.util.List;

public interface SysDictionaryService {

    /**
     * 新增字典信息
     */
    String addInfo(SysDictionaryParam param);

    /**
     * 修改字典信息
     */
    String updateInfo(SysDictionaryParam param);
    /**
     * 删除字典信息
     */
    int deleteInfo(String id);

    /**
     * 获取指定编码开头的字典信息
     * 注：传入空值时，返回一级字典信息
     */
    List<SysDictionaryDto> listByPrefix(String codePrefix);

    /**
     * 获取所有的字典信息
     */
    List<SysDictionaryDto> listAllDictionary();

    /**
     * 获取指定编码开头的字典信息（简版）<br>
     * 去除ID等敏感信息
     */
    List<SysDictionarySimpleDto> listSimpleByPrefix(String codePrefix);

    /**
     * 获取所有的字典信息（简版）<br>
     * 去除ID等敏感信息
     */
    List<SysDictionarySimpleDto> listSimpleByAll();

}
