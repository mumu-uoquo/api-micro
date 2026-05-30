package com.uoquo.platform.institute.service.impl;

import com.uoquo.cloud.events.RemoteEvent;
import com.uoquo.platform.common.BusinessOperationEnum;
import com.uoquo.platform.common.BusinessTypeEnum;
import com.uoquo.platform.common.exception.InstituteReturnCode;
import com.uoquo.platform.institute.mapper.AreaInfoMapper;
import com.uoquo.platform.institute.mapper.DepartmentInfoMapper;
import com.uoquo.platform.institute.model.dto.DepartmentTreeDto;
import com.uoquo.platform.institute.model.param.DepartmentInfoParam;
import com.uoquo.platform.institute.model.pojo.AreaInfo;
import com.uoquo.platform.institute.model.pojo.DepartmentInfo;
import com.uoquo.platform.institute.service.DepartmentInfoService;
import com.uoquo.platform.user.mapper.UserInfoMapper;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DepartmentInfoServiceImpl implements DepartmentInfoService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private DepartmentInfoMapper departmentInfoMapper;

    @Autowired
    private AreaInfoMapper areaInfoMapper;

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Autowired
    private UoquoEventPublisher eventPublisher;

    @Override
    public String addDepartmentInfo(DepartmentInfoParam param) {
        // 1. 基础校验
        // 名称重复校验
        checkNameRepeat(null, param.getInstituteId(), param.getParentId(), param.getDeptName());
        if (StringUtil.notNull(param.getDeptCode())) {
            checkCodeRepeat(null, param.getInstituteId(), param.getDeptCode());
        }
        if (StringUtil.notNull(param.getThirdId())) {
            checkThirdIdRepeat(null, param.getInstituteId(), param.getThirdId());
        }
        // 2. 参数拼接
        DepartmentInfo info = new DepartmentInfo();
        BeanUtils.copyProperties(param, info);
        info.setId(IDGenerator.getNextULID());
        if (StringUtil.isNull(info.getParentId())) {
            // 赋值为空串而不是NULL，方便后续的检索
            info.setParentId("");
            info.setParentPath("");
        } else {
            // 补全父级路径
            DepartmentInfo parentInfo = departmentInfoMapper.selectByPrimaryKey(info.getParentId());
            if (parentInfo == null) {
                throw new ResourceNotFoundException("信息不存在");
            }
            String parentPath = StringUtil.isNull(parentInfo.getParentPath()) ? "" : parentInfo.getParentPath();
            info.setParentPath(parentPath + parentInfo.getId() + ",");
        }
        // 没指定分区时，添加到默认分区
        if (StringUtil.isNull(info.getAreaId())) {
            AreaInfo defaultArea = areaInfoMapper.selectByDefault(info.getInstituteId());
            info.setAreaId(defaultArea.getId());
        }
        // 名称转拼音
        if (StringUtil.notNull(info.getDeptName())) {
            info.setPinYin(PinYinUtil.getPinYin4FirstChar(info.getDeptName()));
        }
        // 其他信息
        info.setCreateUser(CurrentUser.getInfo().getUserId());
        info.setCreateTime(new Date());
        info.setDeleteState(BaseConstant.NOT_DELETED);
        // 3. 执行保存
        departmentInfoMapper.insert(info);
        // 4. 发布事件（新增部门）
        this.publishEvent(BusinessOperationEnum.CREATE, SystemReturnCode.SUCCESS, null, info, null);
        return info.getId();
    }

    @Override
    @Transactional
    public void updateDepartmentInfo(DepartmentInfoParam param) {
        // 1. 基础校验
        // 名称重复校验
        checkNameRepeat(param.getId(), param.getInstituteId(), param.getParentId(), param.getDeptName());
        if (StringUtil.notNull(param.getDeptCode())) {
            checkCodeRepeat(param.getId(), param.getInstituteId(), param.getDeptCode());
        }
        if (StringUtil.notNull(param.getThirdId())) {
            checkThirdIdRepeat(param.getId(), param.getInstituteId(), param.getThirdId());
        }
        // 2. 参数拼接
        DepartmentInfo old = departmentInfoMapper.selectByPrimaryKey(param.getId());
        DepartmentInfo info = new DepartmentInfo();
        BeanUtils.copyProperties(param, info);
        if (StringUtil.isNull(info.getParentId())) {
            // 赋值为空串而不是NULL，方便后续的检索
            info.setParentId("");
            info.setParentPath("");
        } else if (!old.getParentId().equals(info.getParentId())) {
            // 补全父级路径
            DepartmentInfo parentInfo = departmentInfoMapper.selectByPrimaryKey(info.getParentId());
            if (parentInfo == null) {
                throw new ResourceNotFoundException("信息不存在");
            }
            String parentPath = StringUtil.isNull(parentInfo.getParentPath()) ? "" : parentInfo.getParentPath();
            info.setParentPath(parentPath + parentInfo.getId() + ",");
        }
        // 分区没更新时，不修改
        if (StringUtil.isNull(info.getAreaId()) || info.getAreaId().equals(old.getAreaId())) {
            info.setAreaId(null);
        }
        // 名称转拼音
        if (!old.getDeptName().equals(info.getDeptName())) {
            info.setPinYin(PinYinUtil.getPinYin4FirstChar(info.getDeptName()));
        }
        // 其他信息
        info.setUpdateUser(CurrentUser.getInfo().getUserId());
        info.setUpdateTime(new Date());
        // 3. 执行保存
        departmentInfoMapper.updateByPrimaryKey(info);
        // 4. 其他操作
        // 更新了父节点，则需要级联更新子部门的路径
        if (StringUtil.notNull(param.getParentId()) && !param.getParentId().equals(old.getParentId())) {
            String oldPath = old.getParentPath() + info.getId() + ",";
            String newPath = info.getParentPath() + info.getId() + ",";
            int num = departmentInfoMapper.batchUpdateParentPath(oldPath, newPath);
            logger.info("部门[{}]的父级从[{}]变更为[{}]，所有的[{}]子节点父级路径从[{}]变更为[{}]",
                    info.getId(), old.getParentId(), info.getParentId(), num, oldPath, newPath);
        }
        // 5. 发布事件（修改部门）
        DepartmentInfo newInfo = departmentInfoMapper.selectByPrimaryKey(param.getId());
        Map<String, String> map = null;
        if (StringUtil.notNull(info.getAreaId())) {
            map = new HashMap<>();
            map.put("oldArea", old.getAreaId());
            map.put("newArea", info.getAreaId());
        }
        this.publishEvent(BusinessOperationEnum.UPDATE, SystemReturnCode.SUCCESS, old, newInfo, map);
    }

    @Override
    public void updateDepartmentArea2Default(String departmentId) {
        DepartmentInfo old = departmentInfoMapper.selectByPrimaryKey(departmentId);
        if (old == null) {
            throw new ResourceNotFoundException("信息不存在");
        } else if (old.getDefaulted()) {
            throw new ForbiddenException("默认部门不允许删除");
        }
        AreaInfo defaultArea = areaInfoMapper.selectByDefault(old.getInstituteId());
        DepartmentInfo info = new DepartmentInfo();
        info.setId(old.getId());
        info.setAreaId(defaultArea.getId());
        info.setUpdateUser(CurrentUser.getInfo().getUserId());
        info.setUpdateTime(new Date());
        departmentInfoMapper.updateByPrimaryKey(info);
        // 发布事件（调整分区）
        DepartmentInfo newInfo = departmentInfoMapper.selectByPrimaryKey(departmentId);
        Map<String, String> map = new HashMap<>();
        map.put("oldArea", old.getAreaId());
        map.put("newArea", info.getAreaId());
        this.publishEvent(BusinessOperationEnum.CHANGE_RELATION, SystemReturnCode.SUCCESS, old, newInfo, map);
    }

    @Override
    public void deleteDepartmentInfo(String departmentId) {
        DepartmentInfo info = departmentInfoMapper.selectByPrimaryKey(departmentId);
        if (info == null) {
            throw new ResourceNotFoundException("信息不存在");
        } else if (info.getDefaulted()) {
            throw new ForbiddenException("默认部门不允许删除");
        }
        // 有子部门时不允许删除
        Map<String, Object> param = new HashMap<>();
        param.put("parentId", departmentId);
        List<DepartmentTreeDto> list = departmentInfoMapper.listBySearch(param);
        if (!list.isEmpty()) {
            throw new ForbiddenException("该部门下有子部门，不允许删除");
        }
        // 1. 删除部门
        departmentInfoMapper.deleteByPrimaryKey(departmentId, System.currentTimeMillis());
        // 2. 调整该部门下的用户到默认部门
        DepartmentInfo defaultInfo = departmentInfoMapper.selectByDefault(info.getInstituteId());
        userInfoMapper.batchUpdateDepartment(departmentId, defaultInfo.getId());
        // 3. 发布事件（删除部门）
        this.publishEvent(BusinessOperationEnum.DELETE, SystemReturnCode.SUCCESS, info, null, null);
    }

    @Override
    public DepartmentTreeDto getDepartmentInfo(String departmentId) {
        DepartmentInfo info = departmentInfoMapper.selectByPrimaryKey(departmentId);
        if (info == null) {
            throw new ResourceNotFoundException("信息不存在");
        }
        return convertInfo2Dto(info);
    }

    @Override
    public List<DepartmentTreeDto> listDepartmentInfoByTree(String instituteId) {
        Map<String, Object> param = new HashMap<>();
        param.put("instituteId", instituteId);
        List<DepartmentTreeDto> list = departmentInfoMapper.listBySearch(param);
        // 2. 对象转换
        return findChildrenInstitute(null, list);
    }

    /**
     * 递归查找子机构
     */
    private List<DepartmentTreeDto> findChildrenInstitute(String parentId, final List<DepartmentTreeDto> list) {
        List<DepartmentTreeDto> resultList = new ArrayList<>();
        List<DepartmentTreeDto> tmpList = list.stream().filter(item -> {
            if (StringUtil.isNull(parentId)) {
                return StringUtil.isNull(item.getParentId());
            } else {
                return parentId.equals(item.getParentId());
            }
        }).collect(Collectors.toList());
        tmpList.forEach(item -> {
            // 查找子节点
            List<DepartmentTreeDto> children = findChildrenInstitute(item.getId(), list);
            // 拼接返回内容
            item.setChildren(children);
            resultList.add(item);
        });
        return resultList;
    }

    private DepartmentTreeDto convertInfo2Dto(DepartmentInfo info) {
        DepartmentTreeDto dto = new DepartmentTreeDto();
        BeanUtils.copyProperties(info, dto);
        return dto;
    }

    /**
     * 校验名称是否重复：本节点唯一
     */
    private void checkNameRepeat(String id, String instituteId, String parentId, String name) {
        DepartmentInfo info = departmentInfoMapper.selectByName(instituteId, parentId, name);
        if (info != null && !info.getId().equals(id)) {
            throw new UoquoException(InstituteReturnCode.DEPT_NAME_EXIST);
        }
    }

    /**
     * 校验编码是否重复：机构唯一
     */
    private void checkCodeRepeat(String id, String instituteId, String code) {
        if (StringUtil.isNull(code)) {
            return;
        }
        DepartmentInfo info = departmentInfoMapper.selectByCode(instituteId, code);
        if (info != null && StringUtil.notNull(id) && !info.getId().equals(id)) {
            throw new UoquoException(InstituteReturnCode.DEPT_CODE_EXIST);
        }
    }

    /**
     * 校验三方ID是否重复：机构唯一
     */
    private void checkThirdIdRepeat(String id, String instituteId, String thirdId) {
        if (StringUtil.isNull(thirdId)) {
            return;
        }
        DepartmentInfo info = departmentInfoMapper.selectByThirdId(instituteId, thirdId);
        if (info != null && StringUtil.notNull(id) && !info.getId().equals(id)) {
            throw new UoquoException(InstituteReturnCode.DEPT_THIRDID_EXIST);
        }
    }

    private void publishEvent(BusinessOperationEnum type, BaseReturnCode status, DepartmentInfo oldInfo, DepartmentInfo newInfo, Map<String, String> map) {
        RemoteEvent<DepartmentInfo> event = new RemoteEvent<>(BusinessTypeEnum.INSTITUTE.getCode(), type.getCode(), status.getCode());
        event.setBusinessSubType("DEPARTMENT");
        event.setOldData(oldInfo);
        event.setNewData(newInfo);
        // 补充业务信息
        DepartmentInfo info = (newInfo == null) ? oldInfo : newInfo;
        if (info != null) {
            event.setBusinessId(info.getId());
            event.setBusinessInstituteId(info.getInstituteId());
        }
        event.setRemarks(status.getText());
        event.setExtension(map);
        eventPublisher.publishEvent(event);
    }
}
