package com.uoquo.platform.auth.service;

import java.util.List;

import com.uoquo.platform.auth.model.dto.TokenDto;
import com.uoquo.platform.auth.model.dto.UserAuthDto;
import com.uoquo.platform.auth.model.param.AccountLoginParam;
import com.uoquo.platform.auth.model.param.CaptchaParam;
import com.uoquo.platform.auth.model.param.CredentialBindParam;
import com.uoquo.platform.auth.model.param.CredentialLoginParam;
import com.uoquo.platform.auth.model.param.PhoneCaptchaParam;
import com.uoquo.platform.auth.model.param.RegisterParam;
import com.uoquo.platform.auth.model.param.ResetPasswordParam;
import com.uoquo.platform.auth.model.param.SmsLoginParam;
import com.uoquo.platform.role.model.dto.ModuleTreeDto;
import com.uoquo.web.BaseReturnCode;

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
    UserAuthDto userLogin(AccountLoginParam param, String clientIp);

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
    TokenDto appLogin(AccountLoginParam param, String clientIp);

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
     * 获取图形验证码
     * <ul>
     *     <li>scene=login    - 密码多次出错后触发，需检查 FLAG</li>
     *     <li>scene=register - 注册场景，直接生成</li>
     *     <li>scene=phone    - 获取短信码前的人机验证，直接生成</li>
     * </ul>
     * @return 验证码图片 Base64，无需验证码时返回空字符串
     */
    String getCaptcha(CaptchaParam param, String clientIp);

    /**
     * 发送手机短信验证码
     * <ul>
     *     <li>scene=login      - 短信码登录</li>
     *     <li>scene=register   - 用户注册</li>
     * </ul>
     * 若请求中携带图形验证码，则先校验后再发送。
     */
    String sendPhoneCaptcha(PhoneCaptchaParam param, String clientIp);

    /**
     * 手机号短信码登录
     *
     * @param param    短信码登录参数（phone + smsCode）
     * @param clientIp 客户端 IP
     * @return 用户认证信息
     */
    UserAuthDto smsLogin(SmsLoginParam param, String clientIp);

    /**
     * 第三方凭证登录
     * <ul>
     *     <li>已绑定：返回完整 UserAuthDto</li>
     *     <li>未绑定：返回仅含 accessToken=tempToken 的 UserAuthDto，需进行凭证绑定</li>
     * </ul>
     *
     * @param param    凭证登录参数（credentialType + credentialValue）
     * @param clientIp 客户端 IP
     * @return 用户认证信息（已绑定）或含 tempToken 的最小 UserAuthDto（未绑定）
     */
    UserAuthDto credentialLogin(CredentialLoginParam param, String clientIp);

    /**
     * 凭证绑定（账号密码验证 + 写入凭证 + 完成登录）
     * <ul>
     *     <li>从 Redis 读取 BIND_TEMP:{tempToken}，验证有效性</li>
     *     <li>校验账号密码，复用与 userLogin 相同的密码哈希校验和连续失败锁定逻辑</li>
     *     <li>验证成功后写入凭证，删除临时 Token，完成登录返回完整 UserAuthDto</li>
     * </ul>
     *
     * @param param    凭证绑定参数（account + password + tempToken）
     * @param clientIp 客户端 IP
     * @return 完整用户认证信息
     */
    UserAuthDto credentialBind(CredentialBindParam param, String clientIp);

    /**
     * 密码找回（手机号 + 短信码验证 + 重置密码）
     * <ul>
     *     <li>通过手机号查找用户，以 userId 为 TOTP 密钥校验短信码</li>
     *     <li>校验通过后重置密码，不需要旧密码</li>
     * </ul>
     *
     * @param param    密码找回参数（phone + smsCode + newPassword）
     * @param clientIp 客户端 IP
     */
    void resetPassword(ResetPasswordParam param, String clientIp);

    /**
     * 用户注册（需系统开启注册开关）
     * <ul>
     *     <li>校验系统是否开启注册（sys.register.enable）</li>
     *     <li>以手机号为 TOTP 密钥校验短信码</li>
     *     <li>创建用户</li>
     * </ul>
     *
     * @param param    注册参数
     * @param clientIp 客户端 IP
     */
    void register(RegisterParam param, String clientIp);
}
