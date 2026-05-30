package com.uoquo.platform.role.service.impl;

import com.uoquo.cloud.events.RemoteEvent;
import com.uoquo.platform.common.BusinessOperationEnum;
import com.uoquo.platform.common.BusinessTypeEnum;
import com.uoquo.platform.common.DictionaryCodeEnum;
import com.uoquo.platform.role.mapper.ModuleInfoMapper;
import com.uoquo.platform.role.mapper.ModuleResourceMapper;
import com.uoquo.platform.role.mapper.RoleModuleMapper;
import com.uoquo.platform.role.model.dto.ModuleInfoDto;
import com.uoquo.platform.role.model.dto.ModuleTreeDto;
import com.uoquo.platform.role.model.param.ModuleInfoParam;
import com.uoquo.platform.role.model.param.ModuleResourceParam;
import com.uoquo.platform.role.model.pojo.ModuleInfo;
import com.uoquo.platform.role.model.pojo.ModuleParam;
import com.uoquo.platform.role.model.pojo.ModuleResource;
import com.uoquo.platform.role.model.pojo.RoleModule;
import com.uoquo.platform.role.service.ModuleInfoService;
import com.uoquo.utils.CurrentUser;
import com.uoquo.utils.IDGenerator;
import com.uoquo.utils.StringUtil;
import com.uoquo.platform.common.BaseConstant;
import com.uoquo.web.BaseReturnCode;
import com.uoquo.web.SystemReturnCode;
import com.uoquo.web.events.UoquoEventPublisher;
import com.uoquo.web.exception.ForbiddenException;
import com.uoquo.web.exception.ParamErrorException;
import com.uoquo.web.exception.ResourceNotFoundException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ModuleInfoServiceImpl implements ModuleInfoService {

    Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ModuleInfoMapper moduleInfoMapper;

    @Autowired
    private ModuleResourceMapper moduleResourceMapper;

    @Autowired
    private RoleModuleMapper roleModuleMapper;

    @Autowired
    private UoquoEventPublisher eventPublisher;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addModule(ModuleInfoParam param) {
        // 1. 基本校验
        int count = moduleInfoMapper.checkCodeIsExist(param.getModuleCode());
        if (count > 0) {
            throw new ParamErrorException(String.format("模块编码 [%s] 已经存在", param.getModuleCode()));
        }
        count = moduleInfoMapper.checkNameIsExist(null, param.getParentId(), param.getModuleName());
        if (count > 0) {
            throw new ParamErrorException(String.format("模块名称 [%s] 已经存在", param.getModuleName()));
        }
        // 2. 保存数据
        ModuleInfo info = new ModuleInfo();
        BeanUtils.copyProperties(param, info);
        info.setId(IDGenerator.getNextULID());
        // 填充默认值
        if (info.getSortIdx() == null) {
            info.setSortIdx(99);
        }
        if (info.getVisible() == null) {
            info.setVisible(true);
        }
        if (DictionaryCodeEnum.MODULE_TYPE_MENU.getCode().equals(param.getModuleType())) {
            // 菜单：默认填充菜单名称
            if (StringUtil.isNull(param.getMenuName())) {
                info.setMenuName(param.getModuleName());
            }
            if (StringUtil.isNull(param.getUrl())) {
                info.setUrl(param.getPath());
            }
        } else {
            info.setMenuName("");
        }
        info.setParams(this.formatModuleParm(param.getParams()));
        info.setDeleteState(BaseConstant.NOT_DELETED);
        info.setCreateTime(new Date());
        info.setCreateUser(CurrentUser.getInfo().getUserId());
        moduleInfoMapper.insert(info);
        // 3. 保存授权信息（仅内置角色）
        List<RoleModule> roleModuleList = new ArrayList<>();
        if (param.getRoleIdList() != null) {
            roleModuleList = param.getRoleIdList().stream().map(roleId -> {
                RoleModule roleModule = new RoleModule();
                roleModule.setId(IDGenerator.getNextULID());
                roleModule.setRoleId(roleId);
                roleModule.setModuleId(info.getId());
                return roleModule;
            }).collect(Collectors.toList());
        }
        if (!roleModuleList.isEmpty()) {
            roleModuleMapper.batchInsert(roleModuleList);
        }
        // 4. 发布事件（新增模块）
        Map<String, List<String>> map = new HashMap<>();
        // 添加的授权
        if (!roleModuleList.isEmpty()) {
            map.put("addRole", param.getRoleIdList());
        }
        this.publishEvent(BusinessOperationEnum.CREATE, SystemReturnCode.SUCCESS, null, info, map);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateModuleInfo(@NotNull ModuleInfoParam param) {
        // 1. 基本校验
        ModuleInfo old = moduleInfoMapper.selectByPrimaryKey(param.getId());
        if (old == null) {
            throw new ResourceNotFoundException("模块不存在");
        }
        // 父节点不改变，编码不改变
        int count = moduleInfoMapper.checkNameIsExist(param.getId(), old.getParentId(), param.getModuleName());
        if (count > 0) {
            throw new ParamErrorException(String.format("模块名称 [%s] 已经存在", param.getModuleName()));
        }
        // 2. 保存数据
        ModuleInfo info = new ModuleInfo();
        BeanUtils.copyProperties(param, info);
        // 填充默认值
        if (info.getSortIdx() == null) {
            info.setSortIdx(99);
        }
        if (info.getVisible() == null) {
            info.setVisible(true);
        }
        if (DictionaryCodeEnum.MODULE_TYPE_MENU.getCode().equals(param.getModuleType())) {
            // 菜单：默认填充菜单名称
            if (StringUtil.isNull(param.getMenuName())) {
                info.setMenuName(param.getModuleName());
            }
            if (StringUtil.isNull(param.getUrl())) {
                info.setUrl(param.getPath());
            }
        } else {
            info.setMenuName("");
        }
        info.setParams(this.formatModuleParm(param.getParams()));
        info.setUpdateTime(new Date());
        info.setUpdateUser(CurrentUser.getInfo().getUserId());
        moduleInfoMapper.updateByPrimaryKey(info);
        // 3. 保存授权信息（仅内置角色）
        List<String> oldRoleList = roleModuleMapper.listRoleIdByModuleId(param.getId(), DictionaryCodeEnum.ROLE_TYPE_INNER.getCode());
        List<String> addRoleList = new ArrayList<>();
        List<String> delRoleList = new ArrayList<>();
        List<RoleModule> roleModuleList = new ArrayList<>();
        if (param.getRoleIdList() != null) {
            // 新增的授权
            roleModuleList = param.getRoleIdList().stream()
                    .filter(roleId -> !oldRoleList.contains(roleId))
                    .map(roleId -> {
                addRoleList.add(roleId);
                RoleModule roleModule = new RoleModule();
                roleModule.setId(IDGenerator.getNextULID());
                roleModule.setRoleId(roleId);
                roleModule.setModuleId(info.getId());
                return roleModule;
            }).collect(Collectors.toList());
            // 删除的授权
            oldRoleList.stream()
                    .filter(roleId -> !param.getRoleIdList().contains(roleId))
                    .forEach(roleId -> {
                        delRoleList.add(roleId);
                        roleModuleMapper.deleteByRoleIdAndModuleId(roleId, info.getId());
                    });
        }
        if (!roleModuleList.isEmpty()) {
            roleModuleMapper.batchInsert(roleModuleList);
        }
        // 4. 发布事件（更新模块）
        ModuleInfo newInfo = moduleInfoMapper.selectByPrimaryKey(param.getId());
        Map<String, List<String>> map = new HashMap<>();
        // 添加的授权
        if (!addRoleList.isEmpty()) {
            map.put("addRole", addRoleList);
        }
        // 删除的授权
        if (!delRoleList.isEmpty()) {
            map.put("delRole", delRoleList);
        }
        this.publishEvent(BusinessOperationEnum.UPDATE, SystemReturnCode.SUCCESS, old, newInfo, map);
    }

    /**
     * 对参数进行格式化处理
     * 去除key为空的参数
     */
    private List<ModuleParam>  formatModuleParm(List<ModuleParam> list) {
        return list.stream().filter(param -> StringUtil.notNull(param.getKey())).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteModule(String id) {
        // 1. 有子模块时不允许删除
        List<ModuleInfo> subList = moduleInfoMapper.listByParentId(id);
        if (!subList.isEmpty()) {
            throw new ForbiddenException("存在子内容，不能直接删除，请先删除子内容再操作");
        }
        // 2. 删除数据
        // 获取要删除的模块信息
        ModuleInfo old = moduleInfoMapper.selectByPrimaryKey(id);
        // 删除模块（物理删除）
        moduleInfoMapper.deleteByPrimaryKey(id);
        // 删除关联的授权信息
        roleModuleMapper.deleteByModuleId(id);
        // 删除关联的资源信息
        moduleResourceMapper.deleteByModuleId(id);
        // 3. 发布事件（删除模块）
        this.publishEvent(BusinessOperationEnum.DELETE, SystemReturnCode.SUCCESS, old, null, null);
    }

    @Override
    public ModuleInfoDto selectByPrimaryKey(String id) {
        ModuleInfo info = moduleInfoMapper.selectByPrimaryKey(id);
        if (info == null) {
            throw new ResourceNotFoundException("模块不存在");
        }
        // 对象转换
        ModuleInfoDto dto = new ModuleInfoDto();
        BeanUtils.copyProperties(info, dto);
        // 填充授权的内置角色，用于编辑
        List<String> list = roleModuleMapper.listRoleIdByModuleId(info.getId(), DictionaryCodeEnum.ROLE_TYPE_INNER.getCode());
        dto.setRoleIdList(list);
        return dto;
    }

    @Override
    public List<ModuleInfoDto> listModuleByRoot() {
        List<ModuleInfo> list = moduleInfoMapper.listByParentId(null);
        // 对象转换
        List<ModuleInfoDto> result = new ArrayList<>();
        for (ModuleInfo info : list) {
            ModuleInfoDto dto = new ModuleInfoDto();
            BeanUtils.copyProperties(info, dto);
            result.add(dto);
        }
        return result;
    }

    @Override
    public List<ModuleInfoDto> listModuleByRoleId(String roleId) {
        List<ModuleInfo> list = moduleInfoMapper.listByRoleId(roleId);
        // 对象转换
        List<ModuleInfoDto> result = new ArrayList<>();
        for (ModuleInfo info : list) {
            ModuleInfoDto dto = new ModuleInfoDto();
            BeanUtils.copyProperties(info, dto);
            result.add(dto);
        }
        return result;
    }

    @Override
    public List<ModuleTreeDto> listModuleTreeByAll() {
        // 查询所有模块信息
        List<ModuleInfo> list = moduleInfoMapper.listBySearch(null);
        // 转换为树形结构
        return convertModuleList2Tree(null, list);
    }

    @Override
    public List<ModuleTreeDto> listModuleTreeByRoleId(String roleId, String parentId) {
        // 查询指定角色的模块信息
        List<ModuleInfo> list = moduleInfoMapper.listByRoleId(roleId);
        // 转换为树形结构
        return convertModuleList2Tree(parentId, list);
    }

    @Override
    public String addModuleResource(String moduleId, String resourceId) {
        // 1. 保存数据
        ModuleResource moduleResource = new ModuleResource();
        moduleResource.setId(IDGenerator.getNextULID());
        moduleResource.setModuleId(moduleId);
        moduleResource.setResourceId(resourceId);
        moduleResourceMapper.insert(moduleResource);
        // 2. 发布事件（新增模块与资源关联关系）
        ModuleInfo info = moduleInfoMapper.selectByPrimaryKey(moduleId);
        Map<String, List<String>> map = new HashMap<>();
        map.put("addResource", List.of(resourceId));
        this.publishEvent(BusinessOperationEnum.ADD_RELATION, SystemReturnCode.SUCCESS, info, info, map);
        return moduleResource.getId();
    }

    @Override
    public int batchInsertRelationResource(ModuleResourceParam param) {
        // 1. 保存数据
        List<ModuleResource> list = new ArrayList<>();
        for (String resourceId : param.getResourceIdList()) {
            ModuleResource permission = new ModuleResource();
            permission.setId(IDGenerator.getNextULID());
            permission.setModuleId(param.getModuleId());
            permission.setResourceId(resourceId);
            list.add(permission);
        }
        int count = moduleResourceMapper.batchInsert(list);
        // 2. 发布事件（新增模块与资源关联关系）
        ModuleInfo info = moduleInfoMapper.selectByPrimaryKey(param.getModuleId());
        Map<String, List<String>> map = new HashMap<>();
        map.put("addResource", param.getResourceIdList());
        this.publishEvent(BusinessOperationEnum.ADD_RELATION, SystemReturnCode.SUCCESS, info, info, map);
        return count;
    }

    @Override
    public ModuleResource deleteRelationResourceByPrimaryKey(String id) {
        // 1. 基本校验
        ModuleResource info = moduleResourceMapper.selectByPrimaryKey(id);
        if (info == null) {
            throw new ResourceNotFoundException("关联关系不存在");
        }
        if (logger.isDebugEnabled()) {
            logger.debug("删除模块[{}]与资源[{}]的关联关系", info.getModuleId(), info.getResourceId());
        }
        // 2. 删除数据
        moduleResourceMapper.deleteByPrimaryKey(id);
        // 3. 发布事件（删除模块与资源关联关系）
        ModuleInfo module = moduleInfoMapper.selectByPrimaryKey(info.getModuleId());
        Map<String, List<String>> map = new HashMap<>();
        map.put("delResource", List.of(info.getResourceId()));
        this.publishEvent(BusinessOperationEnum.DEL_RELATION, SystemReturnCode.SUCCESS, module, module, map);
        return info;
    }

    /**
     * 将模块列表转换为树形结构
     */
    private List<ModuleTreeDto> convertModuleList2Tree(String parentId, final List<ModuleInfo> list) {
        // 查找父级节点
        List<ModuleTreeDto> rootResult = list.stream()
                .filter(info -> {
                    if (StringUtil.isNull(parentId)) {
                        // 没指定父级节点时，查找根节点
                        return StringUtil.isNull(info.getParentId());
                    } else {
                        // 指定父级节点时，查找子节点
                        return parentId.equals(info.getParentId());
                    }
                }).map(info -> {
                    ModuleTreeDto dto = new ModuleTreeDto();
                    BeanUtils.copyProperties(info, dto);
                    return dto;
                }).sorted(Comparator.comparing(ModuleTreeDto::getSortIdx)).collect(Collectors.toList());
        // 组装下级模块
        for (ModuleTreeDto rootDto : rootResult) {
            List<ModuleTreeDto> children = findChildModuleTree(rootDto.getId(), list);
            rootDto.setChildren(children);
        }
        return rootResult;
    }

    /**
     * 组装下级模块
     */
    private List<ModuleTreeDto> findChildModuleTree(String parentId, final List<ModuleInfo> list) {
        List<ModuleTreeDto> resultList = new ArrayList<>();
        List<ModuleInfo> tmpList = list.stream().filter(item -> parentId.equals(item.getParentId())).collect(Collectors.toList());
        tmpList.forEach(item -> {
            // 拼接返回内容
            ModuleTreeDto dto = new ModuleTreeDto();
            BeanUtils.copyProperties(item, dto);
            // 查找子节点
            List<ModuleTreeDto> children = findChildModuleTree(item.getId(), list);
            children.sort(Comparator.comparing(ModuleTreeDto::getSortIdx));
            dto.setChildren(children);
            resultList.add(dto);
        });
        resultList.sort(Comparator.comparing(ModuleTreeDto::getSortIdx));
        return resultList;
    }

    private void publishEvent(BusinessOperationEnum type, BaseReturnCode status, ModuleInfo oldInfo, ModuleInfo newInfo, Map<String, List<String>> map) {
        RemoteEvent<ModuleInfo> event = new RemoteEvent<>(BusinessTypeEnum.SYSTEM.getCode(), type.getCode(), status.getCode());
        event.setBusinessSubType("MODULE");
        event.setOldData(oldInfo);
        event.setNewData(newInfo);
        // 补充业务信息
        ModuleInfo info = (newInfo == null) ? oldInfo : newInfo;
        if (info != null) {
            event.setBusinessId(info.getId());
        }
        event.setRemarks(status.getText());
        event.setExtension(map);
        eventPublisher.publishEvent(event);
    }
}
