/**
 * Copyright (c) 2025, www.uoquo.com All Rights Reserved.
 * 注意：本内容仅限于内部传阅，禁止外泄
 */
package com.uoquo.gateway.main;

import com.uoquo.utils.Config;
import com.uoquo.utils.StringUtil;
import com.uoquo.web.BaseCacheKey;
import com.uoquo.utils.spring.RedisUtil;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class GatewayApplication implements ApplicationRunner {

	public static void main(String[] args) {
		System.setProperty("app.path", Config.APP_PATH); // 程序根目录（主要用于配置文件中）
		System.setProperty("spring.cloud.inetutils.preferred-networks", Config.getString("app.preferred.networks"));
		// 如果没有指定运行模式，则采用配置文件中的运行模式
		String activeType = System.getProperty("spring.profiles.active");
		if (StringUtil.isNull(activeType)) {
			System.setProperty("spring.profiles.active", Config.getString("app.type", "prod"));
		}

		try {
            SpringApplication application = new SpringApplication(GatewayApplication.class);
            application.run(args);
		} catch (Throwable ex) {
			// 启动失败（如 Redis/Nacos 等外部依赖不可用导致 ApplicationContext 启动异常）时，
			// Lettuce/Netty 的非守护线程会一直挂住 JVM，导致“进程在但服务已死”的假死状态。
			// 这里强制退出，让容器编排/守护进程能感知失败并重新拉起。
			System.exit(1);
		}
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		// 初始数据
		if (!RedisUtil.exist(BaseCacheKey.GLOBAL_TIMEOUT)) {
			RedisUtil.put(BaseCacheKey.GLOBAL_TIMEOUT, 30, null);
		}
	}
}
