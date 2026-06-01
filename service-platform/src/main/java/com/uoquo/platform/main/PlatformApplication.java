package com.uoquo.platform.main;

import com.uoquo.platform.main.config.DeveloperConfig;
import com.uoquo.platform.role.model.dto.ResourceInfoDto;
import com.uoquo.platform.role.service.ResourceInfoService;
import com.uoquo.platform.role.service.RoleInfoService;
import com.uoquo.platform.system.service.AppInfoService;
import com.uoquo.utils.StringUtil;
import com.uoquo.utils.ThreadPoolUtil;
import com.uoquo.web.ServiceApplication;

import com.uoquo.web.BaseCacheKey;
import com.uoquo.utils.spring.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.*;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@EnableDiscoveryClient
@SpringBootApplication
// 排除 DeveloperConfig 的自动扫描，采用 ApplicationConfig 的 @Import 导入
@ComponentScan(excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = DeveloperConfig.class
))
public class PlatformApplication extends ServiceApplication {

    /**
     * 主程序入口.
     */
    public static void main(String[] args) {
        init();
        // 启动应用
        SpringApplication application = new SpringApplication(PlatformApplication.class);
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

    @Autowired
    private RoleInfoService roleInfoService;

    @Autowired
    private AppInfoService appInfoService;

    @Autowired
    private ResourceInfoService resourceInfoService;


    @Override
    public void run(ApplicationArguments args) throws Exception {
        // 缓存字典、缓存配置、缓存菜单、缓存角色
//        ThreadPoolUtil.execute(()->{
//            log.info("auto print.");
//        }, "10/10 * * * * ?");
        // 缓存配置（同步）

        // 缓存应用信息
        ThreadPoolUtil.executeOnce(()->{
            try {
                appInfoService.flushAppInfoCache();
                log.info("flush app info cache success.");
            } catch (Exception e) {
                log.error("flush app info cache error.", e);
            }
        });
        // 缓存应用授权
        ThreadPoolUtil.executeOnce(()->{
            try {
                appInfoService.flushAppPermissionCache();
                log.info("flush app permission cache success.");
            } catch (Exception e) {
                log.error("flush app permission cache error.", e);
            }
        });
        // 缓存角色授权
        ThreadPoolUtil.executeOnce(()->{
            try {
                roleInfoService.flushRolePermissionCache();
                log.info("flush role permission cache success.");
            } catch (Exception e) {
                log.error("flush role permission cache error.", e);
            }
        });
        // 缓存所有资源
        ThreadPoolUtil.executeOnce(()->{
            try {
                flushResourceCache();
                log.info("flush resource cache success.");
            } catch (Exception e) {
                log.error("flush resource cache error.", e);
            }
        });
    }

    /**
     * 缓存所有资源
     */
    private void flushResourceCache() {
        List<ResourceInfoDto> list = resourceInfoService.listAllResource();
        Set<String> allUrls = list.stream()
                .filter(item -> StringUtil.notNull(item.getResourceUrl()))
                .map(ResourceInfoDto::getResourceUrl)
                .collect(Collectors.toSet());
        RedisUtil.remove(BaseCacheKey.GLOBAL_ALL_RESOURCE);
        RedisUtil.putSetAll(BaseCacheKey.GLOBAL_ALL_RESOURCE, allUrls, null);
    }
}
