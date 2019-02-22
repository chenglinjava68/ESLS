package com.datagroup.ESLS.shiro;

import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LifecycleBeanPostProcessorConfig {
    @Bean
    public LifecycleBeanPostProcessor lifecycleBeanPostProcessor() {
        return new LifecycleBeanPostProcessor();
    }
}
