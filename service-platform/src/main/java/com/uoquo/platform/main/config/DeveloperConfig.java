package com.uoquo.platform.main.config;

import com.uoquo.utils.CurrentUser;
import com.uoquo.utils.StringUtil;
import com.uoquo.web.interceptor.CheckLoginInterceptor;
import com.uoquo.web.interceptor.CheckParamInterceptor;
import com.uoquo.web.interceptor.CurrentUserInterceptorAdapter;
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
public class DeveloperConfig implements WebMvcConfigurer {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @PostConstruct
    public void setProperties() {
        log.debug("ApplicationDeveloperConfig init ...");
    }

    @Bean
    public CurrentUserInterceptorAdapter currentUserInterceptor() {
        log.warn("开发模式：CurrentUserInterceptorAdapter");
        return new CurrentUserInterceptorAdapter() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
                    throws Exception {
                // user
                CurrentUser.UserInfo user = CurrentUser.getInfo();
                user.setUserId("user-1");
                user.setUserName("admin");
                user.setRealName("管理员");
                user.setInstituteId("institute-1");
                user.setOfficeId("office-1");
                user.setCurrentRoleId("role-1");
                CurrentUser.setInfo(user);
                // app
                CurrentUser.setAppkey("dev-appkey");
                CurrentUser.setAppType("dev-appType");
                CurrentUser.setAppVersion("1.0.0");
                // 补全其他信息
                this.completionCurrentUser(request);

                return true;
            }

            @Override
            protected String getAppSecret(String appkey) {
                return "dev-secret";
            }

            @Override
            protected String getGlobalSecret() {
                return "dev-global-secret";
            }
        };
    }

    @Bean
    public CheckParamInterceptor checkParamInterceptor() {
        log.warn("开发模式：CheckParamInterceptor");
        return new CheckParamInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
                    throws Exception {
                return true;
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
                return true;
            }
        };
    }
}
