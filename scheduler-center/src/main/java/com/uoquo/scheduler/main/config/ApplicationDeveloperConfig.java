package com.uoquo.scheduler.main.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.annotation.PostConstruct;

/**
 * 开发测试专用的Bean
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

}
