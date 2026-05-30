package com.uoquo.platform.role.service.impl;

import com.uoquo.cloud.events.RemoteEvent;
import com.uoquo.platform.common.BusinessOperationEnum;
import com.uoquo.platform.common.BusinessTypeEnum;
import com.uoquo.platform.common.DictionaryCodeEnum;
import com.uoquo.platform.common.exception.AccountReturnCode;
import com.uoquo.platform.institute.mapper.InstituteInfoMapper;
import com.uoquo.platform.institute.model.pojo.InstituteInfo;
import com.uoquo.platform.role.mapper.ModuleInfoMapper;
import com.uoquo.platform.role.mapper.ResourceInfoMapper;
import com.uoquo.platform.role.mapper.RoleInfoMapper;
import com.uoquo.platform.role.mapper.RoleModuleMapper;
import com.uoquo.platform.role.model.dto.ResourceInfoDto;
import com.uoquo.platform.role.model.dto.RoleInfoDto;
import com.uoquo.platform.role.model.param.RoleInfoParam;
import com.uoquo.platform.role.model.param.RoleListParam;
import com.uoquo.platform.role.model.pojo.ModuleInfo;
import com.uoquo.platform.role.model.pojo.RoleInfo;
import com.uoquo.platform.role.model.pojo.RoleModule;
import com.uoquo.platform.role.service.RoleInfoService;
import com.uoquo.platform.user.mapper.UserRoleMapper;
import com.uoquo.utils.CurrentUser;
import com.uoquo.utils.IDGenerator;
import com.uoquo.utils.StringUtil;
import com.uoquo.platform.common.BaseConstant;
import com.uoquo.web.BaseReturnCode;
import com.uoquo.web.SystemReturnCode;
import com.uoquo.web.events.UoquoEventPublisher;
import com.uoquo.web.BaseCacheKey;
import com.uoquo.web.exception.ForbiddenException;
import com.uoquo.web.exception.ParamErrorException;
import com.uoquo.web.exception.ResourceNotFoundException;
import com.uoquo.web.exception.UoquoException;
import com.uoquo.utils.spring.RedisUtil;
import com.uoquo.web.mybatis.page.PageHelper;
import com.uoquo.mybatis.page.PageList;
import com.uoquo.mybatis.page.PageResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import jakarta.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RoleInfoServiceImpl implements RoleInfoService {
    final Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    private RoleInfoMapper roleInfoMapper;

    @Autowired
    private RoleModuleMapper roleModuleMapper;

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Autowired
    private ModuleInfoMapper moduleInfoMapper;

    @Autowired
    private InstituteInfoMapper instituteInfoMapper;

    @Autowired
    private ResourceInfoMapper resourceInfoMapper;

    @Autowired
    private UoquoEventPublisher eventPublisher;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String addRoleInfo(RoleInfoParam param) {
        // 1. 参数校验
        // 1.1 必传校验
        if (StringUtil.isNull(param.getInstituteId())) {
            throw new ParamErrorException("机构id不能为空");
        }
        InstituteInfo instituteInfo = instituteInfoMapper.selectByPrimaryKey(param.getInstituteId());
        if (instituteInfo == null) {
            throw new ResourceNotFoundException("机构信息不存在");
        }
        // 1.3 校验当前机构角色名是否已存在
        int roleNameCount = roleInfoMapper.checkRoleNameIsExist(null, param.getRoleName(), param.getInstituteId());
        if (roleNameCount > 0) {
            throw new UoquoException(AccountReturnCode.ROLE_NAME_EXIST);
        }
        // 如果是通用角色，还需要判断是否与父级通用角色重名
        if (Objects.equals(param.getRoleType(), DictionaryCodeEnum.ROLE_TYPE_NORMAL.getCode())) {
            List<RoleInfoDto> resultList = getNormalRoleInfo(param.getInstituteId(), null);
            for (RoleInfoDto roleInfoDto : resultList) {
                if (Objects.equals(roleInfoDto.getRoleName(), param.getRoleName())) {
                    throw new UoquoException(AccountReturnCode.ROLE_NAME_EXIST);
                }
            }
        }
        // 2. 添加角色信息
        RoleInfo info = new RoleInfo();
        info.setId(IDGenerator.getNextULID());
        info.setInstituteId(param.getInstituteId());
        info.setRoleName(param.getRoleName());
        info.setDescription(param.getDescription());
        info.setRoleGrade(param.getRoleGrade());
        info.setRoleType(param.getRoleType());
        // 添加的角色分组与机构的所属角色分组相同
        info.setRoleGroup(instituteInfo.getRoleGroup());
        info.setCreateUser(CurrentUser.getInfo().getUserId());
        info.setCreateTime(new Date());
        info.setDeleteState(BaseConstant.NOT_DELETED);
        roleInfoMapper.insert(info);
        logger.debug("机构[{}]增加角色[{}][{}]成功", info.getInstituteId(), info.getRoleName(), info.getId());
        // 3. 复制授权
        List<String> addModuleIds = new ArrayList<>();
        if (StringUtil.notNull(param.getFromRoleId())) {
            List<RoleModule> list = roleModuleMapper.listByRoleId(param.getFromRoleId());
            if (!CollectionUtils.isEmpty(list)) {
                List<RoleModule> newList = list.stream().map(item -> {
                    addModuleIds.add(item.getModuleId());
                    RoleModule newItem = new RoleModule();
                    newItem.setId(IDGenerator.getNextULID());
                    newItem.setRoleId(info.getId());
                    newItem.setModuleId(item.getModuleId());
                    return newItem;
                }).collect(Collectors.toList());
                roleModuleMapper.batchInsert(newList);
                logger.debug("角色[{}]复制[{}]的权限成功", info.getId(), param.getFromRoleId());
            }
        }
        // 4. 事件发布（增加角色）
        this.publishEvent(BusinessOperationEnum.CREATE, SystemReturnCode.SUCCESS, null, info, null);
        if (!addModuleIds.isEmpty()) {
            Map<String, List<String>> map = new HashMap<>();
            map.put("addModule", addModuleIds);
            this.publishEvent(BusinessOperationEnum.ADD_RELATION, SystemReturnCode.SUCCESS, info, null, map);
        }
        return info.getId();
    }

    @Override
    public void updateRoleInfo(RoleInfoParam param) {
        // 1. 基础校验
        RoleInfo old = roleInfoMapper.selectByPrimaryKey(param.getId());
        if (old == null) {
            throw new ResourceNotFoundException("角色信息不存在");
        }
        // 校验当前机构角色名是否已存在
        int roleNameCount = roleInfoMapper.checkRoleNameIsExist(param.getId(), param.getRoleName(), param.getInstituteId());
        if (roleNameCount > 0) {
            throw new UoquoException(AccountReturnCode.ROLE_NAME_EXIST);
        }
        // 2. 更新角色信息
        CurrentUser.UserInfo currentUser = CurrentUser.getInfo();
        RoleInfo info = new RoleInfo();
        BeanUtils.copyProperties(param, info);
        info.setUpdateUser(currentUser.getUserId());
        info.setUpdateTime(new Date());
        roleInfoMapper.updateByPrimaryKey(info);
        logger.debug("角色[{}]更新成功", info.getId());
        // 3. 事件发布（修改角色）
        RoleInfo newInfo = roleInfoMapper.selectByPrimaryKey(param.getId());
        this.publishEvent(BusinessOperationEnum.UPDATE, SystemReturnCode.SUCCESS, old, newInfo, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRole(String id) {
        // 1. 基础校验
        // 基础校验
        RoleInfo old = roleInfoMapper.selectByPrimaryKey(id);
        if (old == null) {
            throw new ResourceNotFoundException("角色信息不存在");
        }
        // 2. 内置角色不能删除
        if (DictionaryCodeEnum.ROLE_TYPE_INNER.getCode().equals(old.getRoleType())) {
            throw new ForbiddenException("内置角色不可操作");
        }
        // 3. 删除角色信息（物理删除）
        roleInfoMapper.deleteByPrimaryKey(id);
        // 删除关联的模块信息
        roleModuleMapper.deleteByRoleId(id);
        // 删除关联的用户信息
        userRoleMapper.deleteByRoleId(id);
        logger.debug("角色[{}]删除成功", id);
        // 4. 事件发布（删除角色）
        this.publishEvent(BusinessOperationEnum.DELETE, SystemReturnCode.SUCCESS, old, null, null);
    }

    @Override
    public RoleInfoDto getRoleInfo(String roleId) {
        RoleInfo info = roleInfoMapper.selectByPrimaryKey(roleId);
        if (info == null) {
            throw new ResourceNotFoundException("信息不存在");
        }
        RoleInfoDto dto = new RoleInfoDto();
        BeanUtils.copyProperties(info, dto);
        return perfectDto(dto);
    }

    @Override
    public List<RoleInfoDto> listRoleInfoByInstitute(RoleListParam param) {
        CurrentUser.UserInfo currentUser = CurrentUser.getInfo();
        // 当前角色
        RoleInfo currentRole = roleInfoMapper.selectByPrimaryKey(currentUser.getCurrentRoleId());
        // 1.查询可见的内置角色及私有角色
        Map<String, Object> paramMap = new HashMap<>();
        // 只能查当前角色等级以下的
        paramMap.put("roleGrade", currentRole.getRoleGrade());
        // 未指定机构时，仅查自己机构的
        String insituteId = param.getInstituteId();
        if (StringUtil.isNull(param.getInstituteId())) {
            insituteId = currentUser.getInstituteId();
        }
        // 只能查当前机构所在分组的内置角色
        InstituteInfo insitute = instituteInfoMapper.selectByPrimaryKey(insituteId);
        paramMap.put("roleGroup", insitute.getRoleGroup());
        // 指定机构的私有角色
        paramMap.put("instituteId", insitute.getId());
        paramMap.put("roleName", param.getRoleName());
        List<RoleInfo> roleList = roleInfoMapper.selectByInstitute(paramMap);
        // 2. 查询可见的通用角色
        List<RoleInfoDto> resultList = getNormalRoleInfo(insitute.getId(), currentRole);
        // 3. 对象转换
        for (RoleInfo info : roleList) {
            RoleInfoDto dto = new RoleInfoDto();
            BeanUtils.copyProperties(info, dto);
            resultList.add(perfectDto(dto));
        }
        return resultList;
    }

    /**
     * 查询继承的父机构通用角色
     */
    private List<RoleInfoDto> getNormalRoleInfo(String insituteId, RoleInfo currentRole) {
        CurrentUser.UserInfo currentUser = CurrentUser.getInfo();
        // 当前角色
        if (currentRole == null) {
            currentRole = roleInfoMapper.selectByPrimaryKey(currentUser.getCurrentRoleId());
        }
        // 1.角色等级
        Map<String, Object> paramMap = new HashMap<>();
        // 只能查当前角色等级以下的
        paramMap.put("roleGrade", currentRole.getRoleGrade());
        paramMap.put("roleType", DictionaryCodeEnum.ROLE_TYPE_NORMAL.getCode());
        // 未指定机构时，为自己机构及父机构
        if (StringUtil.isNull(insituteId)) {
            insituteId = currentUser.getInstituteId();
        }
        // 2. 角色分组（只能查当前机构所在分组的）
        InstituteInfo insitute = instituteInfoMapper.selectByPrimaryKey(insituteId);
        paramMap.put("roleGroup", insitute.getRoleGroup());
        // 3. 父级机构
        List<String> instituteList = new ArrayList<>();
        if (StringUtil.notNull(insitute.getParentPath())) {
            Arrays.stream(insitute.getParentPath().split(","))
                    .filter(StringUtil::notNull)
                    .forEach(instituteList::add);
        }
        instituteList.add(insituteId);
        paramMap.put("instituteList", instituteList);
        return roleInfoMapper.selectBySearch(paramMap);
    }

    /**
     * 超级管理员获取角色信息
     */
    @Override
    public PageResult<RoleInfoDto> listRoleInfoByPage(RoleListParam param) {
        CurrentUser.UserInfo currentUser = CurrentUser.getInfo();
        // 当前角色
        RoleInfo currentRole = roleInfoMapper.selectByPrimaryKey(currentUser.getCurrentRoleId());
        // 查询参数
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("roleGrade", currentRole.getRoleGrade()); // 只能查当前角色等级以下的
        paramMap.put("instituteId", param.getInstituteId());
        paramMap.put("roleName",    param.getRoleName());
        paramMap.put("roleGroup",   param.getRoleGroup());
        paramMap.put("roleType",    param.getRoleType());
        PageHelper.startPage(param.getPageNum(), param.getPageSize());
        PageList<RoleInfoDto> pageList = (PageList<RoleInfoDto>) roleInfoMapper.selectBySearch(paramMap);
        // 对象转换
        for (RoleInfoDto dto : pageList) {
            perfectDto(dto);
        }
        // 封装数据
        return PageResult.of(pageList);
    }

    private RoleInfoDto perfectDto(RoleInfoDto dto) {
        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRoleRelationModule(String roleId, List<String> moduleIds) {
        RoleInfo role = roleInfoMapper.selectByPrimaryKey(roleId);
        if (role == null) {
            throw new ResourceNotFoundException("角色信息不存在");
        }
        // 1. 过滤数据
        List<RoleModule> listRoleModule = roleModuleMapper.listByRoleId(roleId);
        List<String> oldModuleIds = listRoleModule.stream().map(RoleModule::getModuleId).collect(Collectors.toList());
        // 1.1 新增的授权
        List<String> newModuleIds = moduleIds.stream().filter(item -> !oldModuleIds.contains(item)).collect(Collectors.toList());
        // 1.2 删除的授权
        List<String> delModuleIds = oldModuleIds.stream().filter(item -> !moduleIds.contains(item)).collect(Collectors.toList());
        // 2. 保存
        // 2.1 新增的授权
        List<RoleModule> list = new ArrayList<>();
        for (String moduleId : newModuleIds) {
            RoleModule info = new RoleModule();
            info.setId(IDGenerator.getNextULID());
            info.setRoleId(roleId);
            info.setModuleId(moduleId);
            list.add(info);
        }
        if (!CollectionUtils.isEmpty(list)) {
            roleModuleMapper.batchInsert(list);
        }
        // 2.2 删除的授权
        for (String moduleId : delModuleIds) {
            roleModuleMapper.deleteByRoleIdAndModuleId(roleId, moduleId);
        }
        // 事件发布（角色授权）
        Map<String, List<String>> map = new HashMap<>();
        if (!newModuleIds.isEmpty()) {
            logger.debug("角色[{}]增加模块授权[{}]", roleId, newModuleIds);
            map.put("addModule", newModuleIds);
        }
        if (!delModuleIds.isEmpty()) {
            logger.debug("角色[{}]取消模块授权[{}]", roleId, delModuleIds);
            map.put("delModule", delModuleIds);
        }
        this.publishEvent(BusinessOperationEnum.CHANGE_RELATION, SystemReturnCode.SUCCESS, role, role, map);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRoleRelationModule(String roleId, String moduleId) {
        RoleInfo role = roleInfoMapper.selectByPrimaryKey(roleId);
        if (role == null) {
            throw new ResourceNotFoundException("角色信息不存在");
        }
        // 1. 找出当前节点下的所有模块
        List<ModuleInfo> moduleList = moduleInfoMapper.listBySearch(null);
        List<ModuleInfo> relationModuleList = moduleList.stream().filter(item -> item.getId().equals(moduleId)).collect(Collectors.toList());
        packageChildrenList(moduleId, moduleList, relationModuleList);
        List<String> relationModuleIdList = relationModuleList.stream().map(ModuleInfo::getId).collect(Collectors.toList());
        // 2. 添加或删除
        List<RoleModule> existRoleModuleList = roleModuleMapper.listByRoleId(roleId);
        List<String> existRoleModuleIdList = existRoleModuleList.stream().map(RoleModule::getModuleId).collect(Collectors.toList());
        // 2.1 已经存在，则删除
        if (existRoleModuleIdList.contains(moduleId)) {
            logger.debug("角色[{}]取消模块授权[{}]", roleId, relationModuleIdList);
            roleModuleMapper.batchDeleteByModuleId(roleId, relationModuleIdList);
            // 事件发布（删除授权）
            Map<String, List<String>> map = new HashMap<>();
            map.put("delModule", relationModuleIdList);
            this.publishEvent(BusinessOperationEnum.DEL_RELATION, SystemReturnCode.SUCCESS, role, role, map);
            return;
        }
        // 2.2 不存在，则添加
        List<String> addModuleIdList = new ArrayList<>();
        // 添加子节点
        List<RoleModule> paramList = new ArrayList<>();
        for (String relationModuleId : relationModuleIdList) {
            if (existRoleModuleIdList.contains(relationModuleId)) {
                // 已存在，则不重复添加
                continue;
            }
            RoleModule roleModule = new RoleModule();
            roleModule.setId(IDGenerator.getNextULID());
            roleModule.setRoleId(roleId);
            roleModule.setModuleId(relationModuleId);
            paramList.add(roleModule);
            addModuleIdList.add(relationModuleId);
            logger.debug("角色[{}]增加模块[{}]的子节点授权[{}]", roleId, moduleId, relationModuleId);
        }
        if (!CollectionUtils.isEmpty(paramList)) {
            roleModuleMapper.batchInsert(paramList);
        }
        // 添加父节点
        paramList.clear();
        relationModuleList.clear();
        packageParentList(moduleId, moduleList, relationModuleList);
        for (ModuleInfo module : relationModuleList) {
            if (existRoleModuleIdList.contains(module.getId()) || moduleId.equals(module.getId())) {
                // 已存在，则不重复添加
                continue;
            }
            RoleModule roleModule = new RoleModule();
            roleModule.setId(IDGenerator.getNextULID());
            roleModule.setRoleId(roleId);
            roleModule.setModuleId(module.getId());
            paramList.add(roleModule);
            addModuleIdList.add(module.getId());
            logger.debug("角色[{}]增加模块[{}]的父节点授权[{}]", roleId, moduleId, module.getId());
        }
        if (!CollectionUtils.isEmpty(paramList)) {
            roleModuleMapper.batchInsert(paramList);
        }
        // 事件发布（新增授权）
        Map<String, List<String>> map = new HashMap<>();
        map.put("addModule", addModuleIdList);
        this.publishEvent(BusinessOperationEnum.ADD_RELATION, SystemReturnCode.SUCCESS, role, role, map);
    }

    @Override
    @Async
    public void flushRolePermissionCache() {
        // 所有角色
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("roleGrade", 0);
        List<RoleInfoDto> list = roleInfoMapper.selectBySearch(paramMap);
        for (RoleInfoDto dto : list) {
            RoleInfo info = new RoleInfo();
            BeanUtils.copyProperties(dto, info);
            flushRolePermissionCache(info);
        }
    }

    @Override
    @Async
    public void flushRolePermissionCache(String roleId) {
        RoleInfo info = roleInfoMapper.selectByPrimaryKey(roleId);
        if (info == null) {
            logger.warn("角色[{}]不存在，无法刷新授权缓存信息", roleId);
            return;
        }
        flushRolePermissionCache(info);
    }

    /**
     * 刷新角色权限缓存
     */
    private void flushRolePermissionCache(RoleInfo role){
        List<ResourceInfoDto> list = resourceInfoMapper.listByRoleId(role.getId());
        Set<String> urls = list.stream()
                .map(ResourceInfoDto::getResourceUrl)
                .filter(StringUtil::notNull).collect(Collectors.toSet());
        RedisUtil.remove(BaseCacheKey.ROLE_PERMISSION_PREFIX + role.getId());
        RedisUtil.putSetAll(BaseCacheKey.ROLE_PERMISSION_PREFIX + role.getId(), urls, null);
        logger.info("刷新角色[{}]-[{}]权限缓存信息成功", role.getRoleName(), role.getId());
    }

    /**
     * 递归查找所有子节点
     * @param moduleId     当前节点id
     * @param moduleIdList 所有模块信息
     * @param resultList   满足条件的列表
     */
    private void packageChildrenList(String moduleId, List<ModuleInfo> moduleIdList, List<ModuleInfo> resultList) {
        for (ModuleInfo module : moduleIdList) {
            String parentId = module.getParentId() == null ? "" : module.getParentId();
            if (moduleId.equals(parentId)) {
                resultList.add(module);
                packageChildrenList(module.getId(), moduleIdList, resultList);
            }
        }
    }

    /**
     * 递归查找所有父节点
     * @param moduleId     当前节点id
     * @param moduleIdList 所有模块信息
     * @param resultList   满足条件的列表
     */
    private void packageParentList(String moduleId, List<ModuleInfo> moduleIdList, List<ModuleInfo> resultList) {
        for (ModuleInfo module : moduleIdList) {
            if (module.getId().equals(moduleId)) {
                resultList.add(module);
                if (StringUtil.isNull(module.getParentId())) {
                    return;
                }
                packageParentList(module.getParentId(), moduleIdList, resultList);
            }
        }
    }

    private void publishEvent(BusinessOperationEnum type, BaseReturnCode status, RoleInfo oldInfo, RoleInfo newInfo, Map<String, List<String>> map) {
        RemoteEvent<RoleInfo> event = new RemoteEvent<>(BusinessTypeEnum.ACCOUNT.getCode(), type.getCode(), status.getCode());
        event.setBusinessSubType("ROLE");
        event.setOldData(oldInfo);
        event.setNewData(newInfo);
        // 补充业务信息
        RoleInfo info = (newInfo == null) ? oldInfo : newInfo;
        if (info != null) {
            event.setBusinessId(info.getId());
            event.setBusinessInstituteId(info.getInstituteId());
        }
        event.setRemarks(status.getText());
        event.setExtension(map);
        eventPublisher.publishEvent(event);
    }
}
