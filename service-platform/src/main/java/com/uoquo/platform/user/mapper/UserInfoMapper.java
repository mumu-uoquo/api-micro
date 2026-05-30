package com.uoquo.platform.user.mapper;

import com.uoquo.platform.user.model.dto.UserInfoDto;
import com.uoquo.platform.user.model.pojo.UserInfo;
import com.uoquo.mybatis.sensitive.SensitiveField;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface UserInfoMapper {

    /**
     * 新增
     */
    int insert(UserInfo row);

    /**
     * 删除（逻辑删）
     */
    int deleteByPrimaryKey(@Param("id") String id, @Param("deleteState") Long deleteState);

    /**
     * 修改
     */
    int updateByPrimaryKey(UserInfo row);

    /**
     * 修改：最近登录信息
     */
    int updateLastLoginInfo(UserInfo row);

    /**
     * 批量修改所属分区
     */
    int batchUpdateDepartment(@Param("oldDepartmentId") String oldDepartmentId, @Param("newDepartmentId") String newDepartmentId);

    /**
     * 单条查询：用户ID
     */
    UserInfo selectByPrimaryKey(String id);

    /**
     * 单条查询：手机号
     */
    UserInfo selectByPhone(@Param("id") String id, @SensitiveField  @Param("phone") String phone);

    /**
     * 单条查询：用户名
     */
    UserInfo selectByUserName(@Param("id") String id, @Param("userName") String userName);

    /**
     * 单条查询：机构内（工号、三方ID）
     */
    UserInfo checkByInstitute(Map<String, Object> map);

    /**
     * 单条查询：推荐码
     */
    UserInfo selectByReferralCode(String referralCode);

    /**
     * 登录查询：手机号、工号、用户名
     */
    UserInfo selectByLogin(@Param("instituteId") String instituteId, @SensitiveField @Param("account") String account);

    /**
     * 列表查询
     */
    List<UserInfoDto> selectBySearch(Map<String, Object> map);

    /**
     * 列表查询：简版（不含关联信息）
     */
    List<UserInfoDto> selectBySimple(Map<String, Object> map);

    /**
     * 列表：根据角色ID查询
     */
    List<UserInfo> listByRoleId(String roleId);
}