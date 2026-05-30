package com.uoquo.scheduler.main;

import com.uoquo.scheduler.main.config.ApplicationDeveloperConfig;
import com.uoquo.web.ServiceApplication;
import com.uoquo.web.BaseCacheKey;
import com.uoquo.utils.spring.RedisUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.*;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@EnableDiscoveryClient
@SpringBootApplication
// 排除 ApplicationDeveloperConfig 的自动扫描，采用 ApplicationConfig 的 @Import 导入
@ComponentScan(excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = ApplicationDeveloperConfig.class
))
public class SchedulerApplication extends ServiceApplication {

    @Value("${app.name}")
    private String appName;

    /**
     * 主程序入口.
     */
    public static void main(String[] args) {
        init();
        // 启动应用
        SpringApplication application = new SpringApplication(SchedulerApplication.class);
        // 常用监听器（预留，暂无实际用途）
        application.addListeners((ApplicationListener<ApplicationStartingEvent>) event -> {
            System.out.println("application starting event"); // 系统启动时，此时logback还没有初始化，因此不能使用log
        });
        application.addListeners((ApplicationListener<ApplicationEnvironmentPreparedEvent>) event -> {
            log.info("application environment prepared event"); // 加载完properties，打印banner之前
        });
        application.addListeners((ApplicationListener<ApplicationPreparedEvent>) event -> {
            log.info("application prepared event"); // 启动过程中多次执行
        });
        application.addListeners((ApplicationListener<ApplicationStartedEvent>) event -> {
            log.info("application started event");  // 系统main运行完成（JVM running 后, ApplicationRunner.run前）
        });
        application.addListeners((ApplicationListener<ApplicationReadyEvent>) event -> {
            log.info("application ready event");   // 系统启动完毕后（ApplicationRunner.run后）
        });
        // 启动
        application.run(args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // 设置当前服务的appKey，用于feign调用
        RedisUtil.put(BaseCacheKey.APPKEY_SECRET_PREFIX + appName, appName, null);
    }
}
