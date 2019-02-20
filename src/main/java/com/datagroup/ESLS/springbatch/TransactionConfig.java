package com.datagroup.ESLS.springbatch;

import com.alibaba.druid.pool.DruidDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

//@Configuration
//@Lazy
//@EnableTransactionManagement(order = 8)
//public class TransactionConfig {
//    @Bean
//    @ConfigurationProperties(prefix="spring.datasource.other")
//    public DataSource getMyDataSource(){
//        return DataSourceBuilder.create().build();
//    }
//    @Bean
//    public PlatformTransactionManager txManager(DataSource dataSource) {
//        DataSourceTransactionManager dataSourceTransactionManager = new DataSourceTransactionManager(dataSource);
//        dataSourceTransactionManager.setGlobalRollbackOnParticipationFailure(false);
//        return dataSourceTransactionManager;
//    }
//}