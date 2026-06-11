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
//		for (String key : Config.getKeys()) {
//			System.setProperty(key, Config.getString(key));
//		}
		System.setProperty("app.path", Config.APP_PATH); // 程序根目录（主要用于配置文件中）
		System.setProperty("spring.cloud.inetutils.preferred-networks", Config.getString("app.preferred.networks"));
		// 如果没有指定运行模式，则采用配置文件中的运行模式
		String activeType = System.getProperty("spring.profiles.active");
		if (StringUtil.isNull(activeType)) {
			System.setProperty("spring.profiles.active", Config.getString("app.type", "prod"));
		}
		SpringApplication.run(GatewayApplication.class, args);
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		// 1. 免校验URL（含：header必传参数校验、签名校验、登录校验）
		// 文件下载（因为下载码本身已经作为临时校验，为了保证某些无法携带请求头的情况能正常下载，因此放开校验）
		RedisUtil.putSetItem(BaseCacheKey.GLOBAL_UNSIGNED, "/api/dfs/v1/file/download/transfer", null);
		// 2. 免登陆URL
		// 文件上传下载
		RedisUtil.putSetItem(BaseCacheKey.GLOBAL_PERMISSION, "/api/dfs/v1/file/download/transfer", null);
		RedisUtil.putSetItem(BaseCacheKey.GLOBAL_PERMISSION, "/api/dfs/v1/file/upload/transfer", null);

		// 3. 初始数据
		if (!RedisUtil.exist(BaseCacheKey.GLOBAL_TIMEOUT)) {
			RedisUtil.put(BaseCacheKey.GLOBAL_TIMEOUT, 30, null);
		}
	}
}
