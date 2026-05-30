package com.uoquo.scheduler.common.feign;

import com.uoquo.cloud.feign.FeignHeaderInterceptor;
import com.uoquo.utils.CurrentUser;
import com.uoquo.utils.StringUtil;
import com.uoquo.web.BaseCacheKey;
import com.uoquo.web.exception.AppkeyInvalidException;
import com.uoquo.utils.spring.RedisUtil;
import feign.RequestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

/**
 * 扩充Feign请求头拦截器<br>
 * 默认添加当前应用的appkey等参数
 * @author xuhz
 */
public class SchedulerFeignHeaderInterceptor extends FeignHeaderInterceptor {
    protected Logger log = LoggerFactory.getLogger(this.getClass());

    @Value("${app.name}")
    private String appName;

    @Value("${spring.application.index:${app.name:}}")
    private String appIndex;

    @Override
    public void apply(RequestTemplate template) {
        // 当前应用的APPKEY
        if (StringUtil.isNull(CurrentUser.getAppkey())) {
            CurrentUser.setAppkey(appName);
            String appSecret  = getAppSecret(CurrentUser.getAppkey());
            CurrentUser.setAppSecret(appSecret);
        }
        // 全局通讯秘钥
        if (StringUtil.isNull(CurrentUser.getGlobalSecret())) {
            String globalSecret = getGlobalSecret();
            CurrentUser.setGlobalSecret(globalSecret);
        }
        // 补全请求随机数
        if (StringUtil.isNull(CurrentUser.getNonce())) {
            CurrentUser.setNonce(StringUtil.getRandomString(16, 5));
        }
        // 若没有传递硬件ID，则使用当前应用ID
        if (StringUtil.isNull(CurrentUser.getDeviceId())) {
            CurrentUser.setDeviceId(appIndex);
        }
        // 补全用户信息
        if (CurrentUser.getInfo().getUserId() == null) {
            CurrentUser.getInfo().setUserId("system");
            CurrentUser.getInfo().setUserName(appName);
        }
        super.apply(template);
    }

    /**
     * 获取应用密钥.
     */
    protected String getAppSecret(String appkey) {
        if (StringUtil.isNull(appkey)) {
            return null;
        }
        String secret = RedisUtil.getLocalCache(BaseCacheKey.APPKEY_SECRET_PREFIX + appkey, String.class);
        if (StringUtil.isNull(secret)) {
            log.error("[appkey={}]没有缓存的secret", appkey);
            throw new AppkeyInvalidException();
        }
        return secret;
    }

    /**
     * 获取全局密钥.
     */
    protected String getGlobalSecret() {
        String secret = RedisUtil.getLocalCache(BaseCacheKey.GLOBAL_SECRET, String.class);
        if (StringUtil.isNull(secret)) {
            log.error("没有缓存的全局secret");
            throw new AppkeyInvalidException();
        }
        return secret;
    }
}
