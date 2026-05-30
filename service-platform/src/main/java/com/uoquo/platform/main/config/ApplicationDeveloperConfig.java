package com.uoquo.platform.main.config;

import com.uoquo.utils.CurrentUser;
import com.uoquo.web.BaseCacheKey;
import com.uoquo.utils.spring.RedisUtil;
import com.uoquo.web.interceptor.CheckLoginInterceptor;
import com.uoquo.web.interceptor.CheckParamInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * TODO 开发测试时，免签校验，免登录校验
 * 此处 AutoConfigureBefore 和 AutoConfigureOrder 不生效，据说需要将类放到不能自动扫描的地方
 * <a href="https://www.cnblogs.com/Chenjiabing/p/14035711.html">使用AutoConfigureBefore指定配置类顺序没生效</a>
 * <a href="https://blog.csdn.net/BlackBtuWhite/article/details/134362856">SpringBoot自动装配定义先后顺序失效原因极其解析</a>
 */
@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
//@AutoConfigureBefore(CloudConfig.class)
//@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
@ConditionalOnProperty(name = "app.type", havingValue = "dev")
public class ApplicationDeveloperConfig implements WebMvcConfigurer {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @PostConstruct
    public void setProperties() {
        log.debug("ApplicationDeveloperConfig init ...");
    }

    @Bean
    public CheckParamInterceptor checkParamInterceptor() {
        log.warn("开发模式：CheckParamInterceptor");
        return new CheckParamInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
                    throws Exception {
                String appSecret = getAppSecret(CurrentUser.getAppkey());
                CurrentUser.setAppSecret(appSecret);
                return true;
            }

            private String getAppSecret(String appid) {
                String secret = RedisUtil.getLocalCache(BaseCacheKey.APPKEY_SECRET_PREFIX + appid, String.class);
                return secret;
            }
        };
    }

    @Bean
    public CheckLoginInterceptor checkLoginInterceptor() {
        log.warn("开发模式：CheckLoginInterceptor");
        return new CheckLoginInterceptor(){
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
                    throws Exception {
                CurrentUser.getInfo().setUserId("34N82P6K7WPRMBRS");
                CurrentUser.setAppkey("K4X3Z5W9H6Q0J7Q4");
                return true;
            }
        };
    }
}
