package com.uoquo.platform.auth.service;

/**
 * 微信/企微相关服务接口
 * 处理微信OAuth、企微OAuth、TAES加解密等操作
 */
public interface WechatService {

    /**
     * 微信授权码换取 openid
     * @param code 微信授权码
     * @return openid
     */
    String exchangeWechatOpenId(String code);

    /**
     * 企微授权码换取 userid
     * @param code 企微授权码
     * @return userid
     */
    String exchangeWecomUserId(String code);

    /**
     * 运维企微授权码换取手机号（先 ticket 后 mobile）
     * @param code 企微授权码
     * @return 手机号
     */
    String exchangeOpsWecomMobile(String code);

    /**
     * 生成运维 MFA HTML 页面
     * @param success 是否成功
     * @param message 消息内容
     * @return HTML 页面内容
     */
    String opsMfaHtml(boolean success, String message);

}