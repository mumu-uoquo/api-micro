package com.uoquo.platform.user.service;

import com.uoquo.platform.auth.model.dto.TotpDto;
import com.uoquo.platform.user.model.dto.GroupDto;
import com.uoquo.platform.user.model.dto.UserInfoDto;
import com.uoquo.platform.user.model.dto.UserRoleDto;
import com.uoquo.platform.user.model.param.*;
import com.uoquo.mybatis.page.PageResult;

import java.util.List;

public interface UserInfoService {

    /**
     * 添加用户信息
     */
    void addUserInfo(UserAddParam param);

    /**
     * 修改用户信息<br>
     * 注：由于查询时某些字段有脱敏处理，防止脱敏后的数据覆盖原数据，所以当这些字段含有脱敏字符时，则认为没有修改
     */
    void updateUserInfo(UserUpdateParam param);

    /**
     * 修改用户密码
     */
    void updateUserPassword(ChangePasswordParam param, boolean validateOldPassword);

    /**
     * 重置密码（密码找回场景，无需校验旧密码）
     *
     * @param userId      用户ID
     * @param rawPassword 新密码明文（解密后）
     */
    void resetPassword(String userId, String rawPassword);

    /**
     * 修改状态
     */
    void updateState(UserStateParam param);

    /**
     * 修改用户头像
     */
    void updateUserAvatar(String userId, String avatar);

    /**
     * 删除用户信息
     */
    void deleteUser(String id);

    /**
     * 单条查询
     */
    UserInfoDto getUserInfo(String userId);

    /**
     * 分页查询<br>
     * 全部信息，包含关联的角色、分组等扩展信息
     */
    PageResult<UserInfoDto> listUserInfo(UserListParam param);

    /**
     * 按范围查询<br>
     * 注：仅范围基本信息，不含关联的角色、分组等扩展信息
     */
    PageResult<UserInfoDto> listUserByRange(UserListByRangeParam param);

    /**
     * 列表：根据用户查询角色
     */
    List<UserRoleDto> listRoleInfoByUserId(String userId);

    /**
     * 获取所有用户组
     */
    List<GroupDto> listGroupByInstituteId(String instituteId);

    /**
     * 获取所在用户组
     */
    List<GroupDto> listGroupByUserId(String userId);

    /**
     * 获取TOTP绑定二维码
     *
     * @param userId 用户ID
     * @return 二维码信息（URI、Base64图片、密钥）
     */
    TotpDto getTotpQrCode(String userId);

    /**
     * 绑定TOTP秘钥
     *
     * @param mfaCode 动态码、密钥
     * @param userId 用户ID
     */
    void bindTotp(String mfaCode, String userId);

    /**
     * 更新用户真实姓名
     *
     * @param userId   用户ID
     * @param realName 真实姓名
     */
    void updateRealName(String userId, String realName);

    /**
     * 发送手机验证码
     *
     * @param phone  手机号
     * @param userId 用户ID（用作 TOTP 密钥，而非 phone）
     */
    String sendPhoneCaptcha(String phone, String userId);

    /**
     * 更换手机号
     *
     * @param userId 用户ID
     * @param param  手机号及验证码
     */
    void updatePhone(String userId, UpdatePhoneParam param);

    /**
     * 发送邮箱验证码
     *
     * @param email 邮箱
     */
    String sendEmailCaptcha(String email);

    /**
     * 更换邮箱
     *
     * @param userId 用户ID
     * @param param  邮箱及验证码
     */
    void updateEmail(String userId, UpdateEmailParam param);
}
