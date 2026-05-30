package com.uoquo.platform.system.service;


import com.uoquo.platform.system.model.dto.SysAreaDto;

import java.util.List;

public interface SysAreaService {

    /**
     * 查询地区列表
     */
    List<SysAreaDto> selectByTree4PrevCode(String prevCode);

}
