package com.demo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class DemoConf {
    @Bean
    public ThreadPoolTaskExecutor demoExecutor() {
        ThreadPoolTaskExecutor demoExecutor = new ThreadPoolTaskExecutor();
        demoExecutor.setCorePoolSize(5);
        demoExecutor.setMaxPoolSize(10);
        demoExecutor.setQueueCapacity(100);
        return demoExecutor;
    }
}
