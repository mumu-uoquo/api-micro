package com.uoquo.platform.role.service;

import com.uoquo.platform.role.model.dto.RoleInfoDto;
import com.uoquo.platform.role.model.param.RoleInfoParam;
import com.uoquo.platform.role.model.param.RoleListParam;
import com.uoquo.mybatis.page.PageResult;

import java.util.List;

public interface RoleInfoService {

    /**
     * 新增角色
     */
    String addRoleInfo(RoleInfoParam param);

    /**
     * 修改角色
     */
    void updateRoleInfo(RoleInfoParam param);

    /**
     * 删除角色
     */
    void deleteRole(String id);

    /**
     * 获取角色详情
     */
    RoleInfoDto getRoleInfo(String roleId);

    /**
     * 列表查询角色（指定机构，或当前登录人机构）
     * 备注：主要用于普通机构管理角色、超管创建普通机构的用户
     */
    List<RoleInfoDto> listRoleInfoByInstitute(RoleListParam param);

    /**
     * 分页查询角色（仅用于超管）
     * 备注：不做角色分组的限制，主要用于超管管理所有角色
     */
    PageResult<RoleInfoDto> listRoleInfoByPage(RoleListParam param);

    /**
     * 更新：角色的模块授权
     * 备注：传入完整的模块列表，完全按传入模块进行授权
     */
    void updateRoleRelationModule(String roleId, List<String> moduleIds);

    /**
     * 更新：角色的模块授权<br>
     * 备注：入参的模块id作为父级id，当关联表中存在该id，则对其以及子全部做删除操作，当表中无该父id时，对其以及子做新增操作（如果子已经存在，则不处理）
     */
    void updateRoleRelationModule(String roleId, String moduleId);

    /**
     * 刷新角色权限缓存（全部）
     */
    void flushRolePermissionCache();

    /**
     * 刷新角色权限缓存（指定角色）
     */
    void flushRolePermissionCache(String roleId);
}
