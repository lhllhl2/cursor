package com.jasolar.mis.module.system.threadpool;

import com.jasolar.mis.module.system.threadpool.factory.JasolarThreadFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 20/08/2025 17:47
 * Version : 1.0
 */
@Configuration
public class ThreadPoolConfig {

    public static final String AICHAT_EXECUTOR = "jasolarExecutor";

    @Bean(AICHAT_EXECUTOR)
    @Primary
    public ThreadPoolTaskExecutor mallchatExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("jasolar-executor-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());//满了调用线程执行，认为重要任务
        executor.setThreadFactory(new JasolarThreadFactory(executor));
        executor.initialize();
        return executor;
    }

}
