package com.uoquo.platform.institute.service.impl;

import com.uoquo.cloud.events.RemoteEvent;
import com.uoquo.platform.common.BusinessOperationEnum;
import com.uoquo.platform.common.BusinessTypeEnum;
import com.uoquo.platform.common.DictionaryCodeEnum;
import com.uoquo.platform.common.exception.InstituteReturnCode;
import com.uoquo.platform.institute.mapper.InstituteInfoMapper;
import com.uoquo.platform.institute.model.dto.InstituteInfoDto;
import com.uoquo.platform.institute.model.dto.InstituteTreeDto;
import com.uoquo.platform.institute.model.param.*;
import com.uoquo.platform.institute.model.pojo.InstituteInfo;
import com.uoquo.platform.institute.service.AreaInfoService;
import com.uoquo.platform.institute.service.DepartmentInfoService;
import com.uoquo.platform.institute.service.InstituteInfoService;
import com.uoquo.utils.CurrentUser;
import com.uoquo.web.BaseReturnCode;
import com.uoquo.web.SystemReturnCode;
import com.uoquo.web.events.UoquoEventPublisher;
import com.uoquo.utils.IDGenerator;
import com.uoquo.utils.PinYinUtil;
import com.uoquo.utils.StringUtil;
import com.uoquo.platform.common.BaseConstant;
import com.uoquo.web.exception.ForbiddenException;
import com.uoquo.web.exception.ResourceNotFoundException;
import com.uoquo.web.exception.UoquoException;
import com.uoquo.web.mybatis.page.PageHelper;
import com.uoquo.mybatis.page.PageList;
import com.uoquo.mybatis.page.PageResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class InstituteInfoServiceImpl implements InstituteInfoService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private InstituteInfoMapper instituteInfoMapper;

    @Autowired
    private AreaInfoService areaInfoService;

    @Autowired
    private DepartmentInfoService departmentInfoService;

    @Autowired
    private UoquoEventPublisher eventPublisher;

    @Override
    public String addInstituteInfo(InstituteInfoParam param) {
        // 1. 基础校验
        // 名称重复校验
        checkInstituteNameRepeat(null, param.getInstituteName());
        if (StringUtil.notNull(param.getThirdId())) {
            checkInstituteThirdIdRepeat(null, param.getParentId(), param.getThirdId());
        }
        // 2. 参数拼接
        InstituteInfo info = new InstituteInfo();
        BeanUtils.copyProperties(param, info);
        info.setId(IDGenerator.getNextULID());
        // 补齐父级路径
        info.setParentId(param.getParentId());
        InstituteInfo parentInstitute = instituteInfoMapper.selectByPrimaryKey(info.getParentId());
        String parentPath = StringUtil.isNull(parentInstitute.getParentPath()) ? "" : parentInstitute.getParentPath();
        info.setParentPath(parentPath + parentInstitute.getId() + ",");
        // 授权角色组（默认同父节点）
        if (StringUtil.isNull(param.getRoleGroup())) {
            info.setRoleGroup(parentInstitute.getRoleGroup());
        }
        // 机构类型（默认同父节点）
        if (StringUtil.isNull(param.getInstituteType())) {
            info.setInstituteType(parentInstitute.getInstituteType());
        }
        // 名称转拼音
        if (StringUtil.notNull(info.getInstituteName())) {
            info.setPinYin(PinYinUtil.getPinYin4FirstChar(info.getInstituteName()));
        }
        // 其他信息
        info.setStatus(DictionaryCodeEnum.INSTITUTE_STATUS_NORMAL.getCode());
        info.setStatusTime(new Date());
        info.setCreateUser(CurrentUser.getInfo().getUserId());
        info.setCreateTime(new Date());
        info.setDeleteState(BaseConstant.NOT_DELETED);
        // 执行保存
        instituteInfoMapper.insert(info);
        // 3. 添加默认分区
        AreaInfoParam areaParam = new AreaInfoParam();
        areaParam.setInstituteId(info.getId());
        areaParam.setAreaName("默认");
        areaParam.setDefaulted(true);
        String areaId = areaInfoService.addAreaInfo(areaParam);
        // 4. 添加默认部门
        DepartmentInfoParam deptParam = new DepartmentInfoParam();
        deptParam.setInstituteId(info.getId());
        deptParam.setAreaId(areaId);
        deptParam.setDeptName("默认");
        deptParam.setDefaulted(true);
        departmentInfoService.addDepartmentInfo(deptParam);
        // 5. 发布事件（新增机构）
        this.publishEvent(BusinessOperationEnum.CREATE, SystemReturnCode.SUCCESS, null, info);
        return info.getId();
    }

    @Override
    @Transactional
    public void updateInstituteInfo(InstituteInfoParam param) {
        InstituteInfo old = instituteInfoMapper.selectByPrimaryKey(param.getId());
        // 1. 有效性检测
        // 名称有改变时，需校验是否重复
        if (StringUtil.notNull(param.getInstituteName()) && !param.getInstituteName().equals(old.getInstituteName())) {
            checkInstituteNameRepeat(param.getId(), param.getInstituteName());
        }
        // thirdId有改变时，需校验是否重复
        if (StringUtil.notNull(param.getThirdId()) && !param.getThirdId().equals(old.getThirdId())) {
            checkInstituteThirdIdRepeat(param.getId(), param.getParentId(), param.getThirdId());
        }
        // 父级机构有改变时，需校验权限
        if (StringUtil.notNull(param.getParentId()) && !param.getParentId().equals(old.getParentId())) {
            boolean flag = checkSelfManageInstitute(param.getParentId());
            if (!flag) {
                throw new ForbiddenException("父级机构只能是自己管辖的机构");
            }
        }
        // 2. 更新
        // 只更新部分字段，所以不进行bean copy
        InstituteInfo info = new InstituteInfo();
        BeanUtils.copyProperties(param, info);
        String parentPath = null;
        if (StringUtil.notNull(param.getParentId()) && !param.getParentId().equals(old.getParentId())) {
            // 父级机构有变化时，需补全路径
            InstituteInfo parent = instituteInfoMapper.selectByPrimaryKey(param.getParentId());
            parentPath = StringUtil.isNull(parent.getParentPath()) ? "" : parent.getParentPath();
            parentPath = parentPath + parent.getId() + ",";
            info.setParentPath(parentPath);
        } else {
            // 父级机构无变化时，不更新
            info.setParentId(null);
        }
        // 名称转拼音
        if (!old.getInstituteName().equals(info.getInstituteName())) {
            info.setPinYin(PinYinUtil.getPinYin4FirstChar(info.getInstituteName()));
        }
        // 状态不更新
        info.setStatus(null);
        // 补全操作人信息
        info.setUpdateUser(CurrentUser.getInfo().getUserId());
        info.setUpdateTime(new Date());
        instituteInfoMapper.updateByPrimaryKey(info);
        // 3. 其他操作
        // 更新了父机构，则需要级联更新子机构的路径
        if (StringUtil.notNull(parentPath)) {
            String oldPath = old.getParentPath() + info.getId() + ",";
            String newPath = parentPath + info.getId() + ",";
            int num = instituteInfoMapper.batchUpdateParentPath(oldPath, newPath);
            logger.info("机构[{}]的父级从[{}]变更为[{}]，所有的[{}]子机构父级路径从[{}]变更为[{}]",
                    info.getId(), old.getParentId(), info.getParentId(), num, oldPath, newPath);
        }
        // 4. 发布事件（修改机构）
        InstituteInfo newInfo = instituteInfoMapper.selectByPrimaryKey(param.getId());
        this.publishEvent(BusinessOperationEnum.UPDATE, SystemReturnCode.SUCCESS, old, newInfo);
    }

    @Override
    public void updateInstituteStatus(InstituteStateParam param) {
        InstituteInfo old = instituteInfoMapper.selectByPrimaryKey(param.getId());
        if (old == null) {
            throw new ResourceNotFoundException("信息不存在");
        }
        // 2. 修改状态
        InstituteInfo info = new InstituteInfo();
        info.setId(param.getId());
        info.setStatus(param.getStatus());
        info.setStatusTime(new Date());
        info.setStatusMemo(param.getStatusMemo());
        info.setUpdateUser(CurrentUser.getInfo().getUserId());
        info.setUpdateTime(new Date());
        instituteInfoMapper.updateByPrimaryKey(info);
        // 3. 发布事件（修改状态）
        InstituteInfo newInfo = instituteInfoMapper.selectByPrimaryKey(param.getId());
        if (DictionaryCodeEnum.STATE_NORMAL.getCode().equals(param.getStatus())) {
            this.publishEvent(BusinessOperationEnum.ENABLE, SystemReturnCode.SUCCESS, old, newInfo);
        } else if (DictionaryCodeEnum.STATE_DISABLE.getCode().equals(param.getStatus())) {
            this.publishEvent(BusinessOperationEnum.DISABLE, SystemReturnCode.SUCCESS, old, newInfo);
        } else {
            this.publishEvent(BusinessOperationEnum.CHANGE_STATUS, SystemReturnCode.SUCCESS, old, newInfo);
        }
    }

    @Override
    public void deleteInstituteInfo(String instituteId) {
        InstituteInfo info = instituteInfoMapper.selectByPrimaryKey(instituteId);
        if (info == null) {
            throw new ResourceNotFoundException("信息不存在");
        }
        // 有子机构时，不能删除
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("parentId", instituteId);
        List<InstituteInfo> children = instituteInfoMapper.selectBySearch(paramMap);
        if (!children.isEmpty()) {
            throw new ForbiddenException("有子机构，只能禁用，不能删除");
        }
        instituteInfoMapper.deleteByPrimaryKey(instituteId, System.currentTimeMillis());
        // 3. 发布事件（删除机构）
        this.publishEvent(BusinessOperationEnum.DELETE, SystemReturnCode.SUCCESS, info, null);
    }

    @Override
    public InstituteInfoDto getInstituteInfo(String instituteId) {
        InstituteInfo info = instituteInfoMapper.selectByPrimaryKey(instituteId);
        if (info == null) {
            throw new ResourceNotFoundException("信息不存在");
        }
        // 对象转换
        InstituteInfoDto dto = convertInfo2Dto(info);
        // 补充父级机构
        if (StringUtil.notNull(dto.getParentId())) {
            InstituteInfo parent = instituteInfoMapper.selectByPrimaryKey(dto.getParentId());
            dto.setParentName(parent.getInstituteName());
        }
        return dto;
    }

    @Override
    public boolean checkSelfManageInstitute(String instituteId) {
        InstituteInfo info = instituteInfoMapper.selectByPrimaryKey(instituteId);
        if (info == null) {
            return false;
        }
        String currentInstituteId = CurrentUser.getInfo().getInstituteId();
        // 自己所在机构，或自己的下级机构
        return info.getId().equals(currentInstituteId) || info.getParentPath().contains(currentInstituteId);
    }

    @Override
    public PageResult<InstituteInfoDto> listInstituteInfoByPage(String rootInstituteId, InstituteListParam param) {
        InstituteInfo rootInstitute = null;
        // 1. 查询条件
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("instituteName", param.getInstituteName());
        if (StringUtil.notNull(param.getParentId())) {
            // 指定了parentId，则只查询指定机构下的直属机构信息
            paramMap.put("parentId", param.getParentId());
        } else {
            // 没有指定parentId，则查询指定根机构下的所有信息（即：管辖的所有机构信息）
            if (StringUtil.notNull(rootInstituteId)) {
                rootInstitute = instituteInfoMapper.selectByPrimaryKey(rootInstituteId);
                String parentPath = StringUtil.isNull(rootInstitute.getParentPath()) ? "" : rootInstitute.getParentPath();
                paramMap.put("parentPath", parentPath + rootInstitute.getId() + ",");
            }
        }
        // 分页查询
        PageHelper.startPage(param.getPageNum(), param.getPageSize());
        PageList<InstituteInfo> list = (PageList<InstituteInfo>) instituteInfoMapper.selectBySearch(paramMap);
        // 2. 对象转换
        List<InstituteInfoDto> resultList = new ArrayList<>();
        if (rootInstitute != null) {
            resultList.add(convertInfo2Dto(rootInstitute));
        }
        for (InstituteInfo item : list.getResult()) {
            resultList.add(convertInfo2Dto(item));
        }
        // 3. 返回结果
        return PageResult.of(list, resultList);
    }

    @Override
    public List<InstituteTreeDto> listInstituteInfoByTree(String rootInstituteId) {
        // 1. 查询条件
        Map<String, Object> paramMap = new HashMap<>();
        // 只查询指定根节点下的机构信息
        InstituteInfo rootInstitute = null;
        if (StringUtil.notNull(rootInstituteId)) {
            rootInstitute = instituteInfoMapper.selectByPrimaryKey(rootInstituteId);
            String parentPath = StringUtil.isNull(rootInstitute.getParentPath()) ? "" : rootInstitute.getParentPath();
            paramMap.put("parentPath", parentPath + rootInstitute.getId() + ",");
        }
        // 分页查询
        List<InstituteInfo> list = instituteInfoMapper.selectBySearch(paramMap);
        // 2. 对象转换
        List<InstituteInfo> rootList = findRootInstitute(rootInstitute, list);
        List<InstituteTreeDto> resultList = new ArrayList<>();
        for (InstituteInfo item : rootList) {
            // 查找子节点
            List<InstituteTreeDto> children = findChildrenInstitute(item.getId(), list);
            // 对象转换
            InstituteTreeDto dto = convertInfo2TreeDto(item);
            dto.setChildren(children);
            resultList.add(dto);
        }
        return resultList;
    }

    /**
     * 查找根机构<br/>
     * 无指定根机构时，所有的一级机构
     */
    private List<InstituteInfo> findRootInstitute(InstituteInfo rootInstitute, final List<InstituteInfo> list) {
        List<InstituteInfo> resultList = new ArrayList<>();
        if (rootInstitute != null) {
            resultList.add(rootInstitute);
        } else {
            List<InstituteInfo> tmpList = list.stream().filter(item -> StringUtil.isNull(item.getParentId())).collect(Collectors.toList());
            resultList.addAll(tmpList);
        }
        return resultList;
    }

    /**
     * 递归查找子机构
     */
    private List<InstituteTreeDto> findChildrenInstitute(String parentId, final List<InstituteInfo> list) {
        List<InstituteTreeDto> resultList = new ArrayList<>();
        List<InstituteInfo> tmpList = list.stream().filter(item -> parentId.equals(item.getParentId())).collect(Collectors.toList());
        tmpList.forEach(item -> {
            // 查找子节点
            List<InstituteTreeDto> children = findChildrenInstitute(item.getId(), list);
            // 拼接返回内容
            InstituteTreeDto dto = convertInfo2TreeDto(item);
            dto.setChildren(children);
            resultList.add(dto);
        });
        return resultList;
    }

    /**
     * 将info转换为dto
     */
    private InstituteInfoDto convertInfo2Dto(final InstituteInfo info) {
        InstituteInfoDto dto = new InstituteInfoDto();
        BeanUtils.copyProperties(info, dto);
        return dto;
    }

    /**
     * 将info转换为树形dto
     */
    private InstituteTreeDto convertInfo2TreeDto(final InstituteInfo info) {
        InstituteTreeDto dto = new InstituteTreeDto();
        dto.setId(info.getId());
        dto.setParentId(info.getParentId());
        dto.setInstituteName(info.getInstituteName());
        dto.setShortName(info.getShortName());
        dto.setInstituteType(info.getInstituteType());
        return dto;
    }

    /**
     * 校验机构名是否重复：系统唯一
     */
    private void checkInstituteNameRepeat(String id, String instituteName) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("id", id);
        paramMap.put("instituteName", instituteName);
        InstituteInfo info = instituteInfoMapper.selectByUnique(paramMap);
        if (info != null) {
            throw new UoquoException(InstituteReturnCode.INST_NAME_EXIST);
        }
    }

    /**
     * 校验三方ID是否重复：同级唯一
     */
    private void checkInstituteThirdIdRepeat(String id, String parentId, String thirdId) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("id", id);
        paramMap.put("thirdId", thirdId);
        paramMap.put("parentId", parentId);
        InstituteInfo info = instituteInfoMapper.selectByUnique(paramMap);
        if (info != null) {
            throw new UoquoException(InstituteReturnCode.INST_THIRDID_EXIST);
        }
    }

    /**
     * 发布机构信息事件
     */
    private void publishEvent(BusinessOperationEnum type, BaseReturnCode status, InstituteInfo oldInfo, InstituteInfo newInfo) {
        RemoteEvent<InstituteInfo> event = new RemoteEvent<>(BusinessTypeEnum.INSTITUTE.getCode(), type.getCode(), status.getCode());
        event.setBusinessSubType("INSTITUTE");
        event.setOldData(oldInfo);
        event.setNewData(newInfo);
        // 补充业务信息
        InstituteInfo info = (newInfo == null) ? oldInfo : newInfo;
        if (info != null) {
            event.setBusinessId(info.getId());
            event.setBusinessInstituteId(info.getId());
        }
        event.setRemarks(status.getText());
        eventPublisher.publishEvent(event);
    }
}
