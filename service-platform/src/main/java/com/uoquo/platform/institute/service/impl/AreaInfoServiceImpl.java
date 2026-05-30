package com.uoquo.platform.institute.service.impl;

import com.uoquo.cloud.events.RemoteEvent;
import com.uoquo.platform.common.BusinessOperationEnum;
import com.uoquo.platform.common.BusinessTypeEnum;
import com.uoquo.platform.common.exception.InstituteReturnCode;
import com.uoquo.platform.institute.mapper.AreaInfoMapper;
import com.uoquo.platform.institute.mapper.DepartmentInfoMapper;
import com.uoquo.platform.institute.model.dto.AreaInfoDto;
import com.uoquo.platform.institute.model.param.AreaInfoParam;
import com.uoquo.platform.institute.model.pojo.AreaInfo;
import com.uoquo.platform.institute.service.AreaInfoService;
import com.uoquo.utils.CurrentUser;
import com.uoquo.web.BaseReturnCode;
import com.uoquo.web.SystemReturnCode;
import com.uoquo.web.events.UoquoEventPublisher;
import com.uoquo.utils.IDGenerator;
import com.uoquo.utils.StringUtil;
import com.uoquo.platform.common.BaseConstant;
import com.uoquo.web.exception.ForbiddenException;
import com.uoquo.web.exception.ResourceNotFoundException;
import com.uoquo.web.exception.UoquoException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class AreaInfoServiceImpl implements AreaInfoService {

    @Autowired
    private AreaInfoMapper areaInfoMapper;

    @Autowired
    private DepartmentInfoMapper departmentInfoMapper;

    @Autowired
    private UoquoEventPublisher eventPublisher;

    @Override
    public String addAreaInfo(AreaInfoParam param) {
        // 1. 基础校验
        // 名称重复校验
        checkNameRepeat(null, param.getInstituteId(), param.getAreaName());
        if (StringUtil.notNull(param.getAreaCode())) {
            checkCodeRepeat(null, param.getInstituteId(), param.getAreaCode());
        }
        if (StringUtil.notNull(param.getThirdId())) {
            checkThirdIdRepeat(null, param.getInstituteId(), param.getThirdId());
        }
        // 2. 参数拼接
        AreaInfo info = new AreaInfo();
        BeanUtils.copyProperties(param, info);
        info.setId(IDGenerator.getNextULID());
        // 其他信息
        info.setCreateUser(CurrentUser.getInfo().getUserId());
        info.setCreateTime(new Date());
        info.setDeleteState(BaseConstant.NOT_DELETED);
        // 3. 执行保存
        areaInfoMapper.insert(info);
        // 4. 发布事件（新增分区）
        this.publishEvent(BusinessOperationEnum.CREATE, SystemReturnCode.SUCCESS, null, info);
        return info.getId();
    }

    @Override
    public void updateAreaInfo(AreaInfoParam param) {
        AreaInfo old = areaInfoMapper.selectByPrimaryKey(param.getId());
        if (old == null) {
            throw new ResourceNotFoundException("信息不存在");
        }
        // 1. 基础校验
        // 名称重复校验
        checkNameRepeat(param.getId(), param.getInstituteId(), param.getAreaName());
        if (StringUtil.notNull(param.getAreaCode())) {
            checkCodeRepeat(param.getId(), param.getInstituteId(), param.getAreaCode());
        }
        if (StringUtil.notNull(param.getThirdId())) {
            checkThirdIdRepeat(param.getId(), param.getInstituteId(), param.getThirdId());
        }
        // 2. 参数拼接
        AreaInfo info = new AreaInfo();
        BeanUtils.copyProperties(param, info);
        // 其他信息
        info.setUpdateUser(CurrentUser.getInfo().getUserId());
        info.setUpdateTime(new Date());
        // 3. 执行保存
        areaInfoMapper.updateByPrimaryKey(info);
        // 4. 发布事件（修改分区）
        AreaInfo newInfo = areaInfoMapper.selectByPrimaryKey(param.getId());
        this.publishEvent(BusinessOperationEnum.UPDATE, SystemReturnCode.SUCCESS, old, newInfo);
    }

    @Override
    @Transactional
    public void deleteAreaInfo(String areaId) {
        AreaInfo info = areaInfoMapper.selectByPrimaryKey(areaId);
        if (info == null) {
            throw new ResourceNotFoundException("信息不存在");
        } else if (info.getDefaulted()) {
            throw new ForbiddenException("默认分区不允许删除");
        }
        // 1. 删除分区
        areaInfoMapper.deleteByPrimaryKey(areaId, System.currentTimeMillis());
        // 2. 调整该分区下的部门到默认分区
        AreaInfo defaultInfo = areaInfoMapper.selectByDefault(info.getInstituteId());
        departmentInfoMapper.batchUpdateArea(areaId, defaultInfo.getId());
        // 3. 发布事件（删除分区）
        this.publishEvent(BusinessOperationEnum.DELETE, SystemReturnCode.SUCCESS, info, null);
    }

    @Override
    public AreaInfoDto getAreaInfo(String areaId) {
        AreaInfo info = areaInfoMapper.selectByPrimaryKey(areaId);
        if (info == null) {
            throw new ResourceNotFoundException("信息不存在");
        }
        return convertInfo2Dto(info);
    }

    @Override
    public List<AreaInfoDto> listAreaInfoByList(String instituteId) {
        List<AreaInfo> list = areaInfoMapper.listByInstituteId(instituteId);
        List<AreaInfoDto> resultList = new ArrayList<>();
        for (AreaInfo item : list) {
            resultList.add(convertInfo2Dto(item));
        }
        return resultList;
    }


    private AreaInfoDto convertInfo2Dto(AreaInfo info) {
        AreaInfoDto dto = new AreaInfoDto();
        BeanUtils.copyProperties(info, dto);
        return dto;
    }

    /**
     * 校验名称是否重复：机构唯一
     */
    private void checkNameRepeat(String id, String instituteId, String name) {
        AreaInfo info = areaInfoMapper.selectByName(instituteId, name);
        if (info != null && !info.getId().equals(id)) {
            throw new UoquoException(InstituteReturnCode.AREA_NAME_EXIST);
        }
    }

    /**
     * 校验编码是否重复：机构唯一
     */
    private void checkCodeRepeat(String id, String instituteId, String code) {
        if (StringUtil.isNull(code)) {
            return;
        }
        AreaInfo info = areaInfoMapper.selectByCode(instituteId, code);
        if (info != null && StringUtil.notNull(id) && !info.getId().equals(id)) {
            throw new UoquoException(InstituteReturnCode.AREA_CODE_EXIST);
        }
    }

    /**
     * 校验三方ID是否重复：机构唯一
     */
    private void checkThirdIdRepeat(String id, String instituteId, String thirdId) {
        if (StringUtil.isNull(thirdId)) {
            return;
        }
        AreaInfo info = areaInfoMapper.selectByThirdId(instituteId, thirdId);
        if (info != null && StringUtil.notNull(id) && !info.getId().equals(id)) {
            throw new UoquoException(InstituteReturnCode.AREA_THIRDID_EXIST);
        }
    }

    private void publishEvent(BusinessOperationEnum type, BaseReturnCode status, AreaInfo oldInfo, AreaInfo newInfo) {
        RemoteEvent<AreaInfo> event = new RemoteEvent<>(BusinessTypeEnum.INSTITUTE.getCode(), type.getCode(), status.getCode());
        event.setBusinessSubType("AREA");
        event.setOldData(oldInfo);
        event.setNewData(newInfo);
        // 补充业务信息
        AreaInfo info = (newInfo == null) ? oldInfo : newInfo;
        if (info != null) {
            event.setBusinessId(info.getId());
            event.setBusinessInstituteId(info.getInstituteId());
        }
        event.setRemarks(status.getText());
        eventPublisher.publishEvent(event);
    }
}
