package com.uoquo.platform.auth.service;

import com.uoquo.platform.auth.model.dto.TokenDto;
import com.uoquo.platform.auth.model.dto.UserAuthDto;
import com.uoquo.platform.auth.model.param.UserLoginParam;
import com.uoquo.platform.role.model.dto.ModuleTreeDto;
import com.uoquo.web.BaseReturnCode;

import java.util.List;

public interface AuthService {

    /**
     * 用戶登录
     * 备注
     * <ol>
     *     <li>密码TOTP时间因子进行AES加密</li>
     *     <li>token有效期：30分钟</li>
     *     <li>刷新码有效期：7天</li>
     * </ol>
     */
    UserAuthDto userLogin(UserLoginParam param, String clientIp);

    /**
     * 验证TOTP动态码（完成登录）
     *
     * @param tempToken 临时Token（登录成功后生成）
     * @param totpCode  TOTP动态码
     * @return 用户认证信息（包含正式Token）
     */
    UserAuthDto totpLogin(String tempToken, String totpCode);

    /**
     * 用戶通过刷新token登录（主要用于APP等移动端）
     * <ol>
     *     <li>只能用一次，无论验证成功，都将失效</li>
     *     <li>只能是相同的设备、应用使用</li>
     *     <li>仅返回token，不返回对应的用户信息，防止泄露</li>
     * </ol>
     */
    TokenDto userRefreshLogin(String refreshToken, String roleId, String clientIp);

    /**
     * 三方认证：appkey、secret校验<br>
     * 备注
     * <ol>
     *     <li>secret通过TOPT时间因子加密</li>
     *     <li>token有效期：2小时</li>
     *     <li>刷新码有效期：7天</li>
     * </ol>
     */
    TokenDto appLogin(UserLoginParam param, String clientIp);

    /**
     * 三方认证：刷新码<br>
     * 备注
     * <ol>
     *     <li>只能用一次，无论验证成功，都将失效</li>
     *     <li>只能是相同的应用使用</li>
     * </ol>
     */
    TokenDto appRefreshLogin(String refreshToken, String clientIp);

    /**
     * 用户登出
     */
    void logout(String token, String appkey, BaseReturnCode status);

    /**
     * 当前登录用户信息
     */
    UserAuthDto getUserInfo();

    /**
     * 角色切换<br/>
     * 根据AppId的授权根模块过滤
     */
    List<ModuleTreeDto> getPermissionByRoleId(String roleId);

    /**
     * 获取验证码
     * @return 验证码图片（Base64）
     */
    String getCaptcha(UserLoginParam param, String clientIp);
}
