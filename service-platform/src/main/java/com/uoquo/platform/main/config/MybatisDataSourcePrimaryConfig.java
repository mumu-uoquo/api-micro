/**
 * Copyright (c) 2025, www.uoquo.com All Rights Reserved.
 * 注意：本内容仅限于内部传阅，禁止外泄
 */
package com.uoquo.platform.main.config;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;
import java.util.Arrays;

/**
 * 数据源（主）
 */
@Configuration
@MapperScan(basePackages = MybatisDataSourcePrimaryConfig.PACKAGE, sqlSessionFactoryRef = MybatisDataSourcePrimaryConfig.NAME_SESSION_FACTORY)
public class MybatisDataSourcePrimaryConfig {
    private final Logger log = LoggerFactory.getLogger(getClass());
    static final String PACKAGE = "com.uoquo.**.mapper";
    static final String PREFIX = "spring.datasource.druid.primary";

    static final String NAME_DATASOURCE       = "primaryDataSource";
    static final String NAME_TRANSACTION      = "primaryTransactionManager";
    static final String NAME_SESSION_FACTORY  = "primarySqlSessionFactory";
    static final String NAME_SESSION_TEMPLATE = "primarySqlSessionTemplate";

    @Primary
    @ConfigurationProperties(prefix = PREFIX)
    @Bean(name = NAME_DATASOURCE)
    public DataSource primaryDataSource() {
        return DruidDataSourceBuilder.create().build();
    }

    @Primary
    @Bean(name = NAME_TRANSACTION)
    public DataSourceTransactionManager primaryTransactionManager(@Qualifier(NAME_DATASOURCE) DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Primary
    @Bean(name = NAME_SESSION_FACTORY)
    public SqlSessionFactory primarySqlSessionFactory(@Qualifier(NAME_DATASOURCE) DataSource dataSource, Interceptor[] interceptors) throws Exception {
        log.info("MybatisDataSourceOneConfig interceptors:{}", Arrays.asList(interceptors));

        final SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);
        sessionFactory.setPlugins(interceptors);
        return sessionFactory.getObject();
    }

    @Primary
    @Bean(name = NAME_SESSION_TEMPLATE)
    public SqlSessionTemplate primarySqlSessionTemplate(
            @Qualifier(NAME_SESSION_FACTORY) SqlSessionFactory sqlSessionFactory) throws Exception {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

}
