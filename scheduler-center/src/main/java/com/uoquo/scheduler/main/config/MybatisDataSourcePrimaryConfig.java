package com.uoquo.scheduler.main.config;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

/**
 * 数据源 1
 */
@Configuration
@MapperScan(basePackages = MybatisDataSourcePrimaryConfig.PACKAGE, sqlSessionFactoryRef = MybatisDataSourcePrimaryConfig.NAME_SESSION_FACTORY)
public class MybatisDataSourcePrimaryConfig {
    static final String PACKAGE = "com.uoquo.**.mapper";
    static final String PREFIX = "spring.datasource.druid.primary";

    static final String NAME_DATASOURCE = "ds1DataSource";
    static final String NAME_SESSION_FACTORY = "ds1SqlSessionFactory";
    static final String NAME_SESSION_TEMPLATE = "ds1SqlSessionTemplate";
    static final String NAME_TRANSACTION = "ds1TransactionManager";

    @Autowired
    private Interceptor[] interceptors;
    @Primary
    @ConfigurationProperties(prefix = PREFIX)
    @Bean(name = NAME_DATASOURCE)
    public DataSource monitorDataSource() {
        return DruidDataSourceBuilder.create().build();
    }

    @Primary
    @Bean(name = NAME_TRANSACTION)
    public DataSourceTransactionManager monitorTransactionManager(@Qualifier(NAME_DATASOURCE) DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Primary
    @Bean(name = NAME_SESSION_FACTORY)
    public SqlSessionFactory monitorSqlSessionFactory(@Qualifier(NAME_DATASOURCE) DataSource dataSource) throws Exception {
        final SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);
        sessionFactory.setPlugins(interceptors);
        return sessionFactory.getObject();
    }

    @Primary
    @Bean(name = NAME_SESSION_TEMPLATE)
    public SqlSessionTemplate monitorSqlSessionTemplate(
        @Qualifier(NAME_SESSION_FACTORY) SqlSessionFactory sqlSessionFactory) throws Exception {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

}
