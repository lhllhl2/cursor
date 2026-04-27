package com.jasolar.mis.framework.bpm.autoconfigure;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.jasolar.mis.framework.bpm.mq.config.BpmRabbitMQConfig;
import com.jasolar.mis.framework.bpm.mq.config.BpmRabbitMQInitializer;
import com.jasolar.mis.framework.bpm.mq.publisher.BpmMessagePublisher;
import com.jasolar.mis.framework.bpm.util.BpmUtils;
import com.jasolar.mis.framework.redis.lock.RedisLockExecutor;
import com.jasolar.mis.module.bpm.api.BpmProcessInstanceApi;

import lombok.extern.slf4j.Slf4j;

/**
 * BPM 自动配置类
 */
@Configuration
@EnableConfigurationProperties(BpmProperties.class)
@ConditionalOnProperty(prefix = "jasolar.bpm", name = "enabled", matchIfMissing = true)
@Import({ BpmRabbitMQConfig.class, BpmRabbitMQInitializer.class })
@Slf4j
@EnableScheduling
public class BpmAutoConfiguration {

    /** 自动配置构造函数，用于记录日志 */
    public BpmAutoConfiguration() {
        log.info("==================================创建BPM自动配置==================================");
    }

    /**
     * 消息发布服务
     */
    @Bean
    @ConditionalOnMissingBean
    // @ConditionalOnBean(RabbitTemplate.class)
    @ConditionalOnProperty(prefix = "jasolar.bpm", name = "publisher-enabled", matchIfMissing = false)
    public BpmMessagePublisher bpmMessagePublisher(RabbitTemplate rabbitTemplate, BpmProperties properties) {
        log.info("初始化BPM消息发布服务");
        return new BpmMessagePublisher(rabbitTemplate, properties);
    }

    /**
     * 配置BPM服务的FeignClient
     * 
     * @author galuo
     * @date 2025-04-02 09:39
     *
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(prefix = "jasolar.bpm", name = "consumer-enabled", matchIfMissing = true)
    @EnableFeignClients(clients = { BpmProcessInstanceApi.class })
    static class BpmApiFeignConfiguration {

    }

    /**
     * 为BpmUtils注入分布式锁执行对象
     * 
     * @param lockExecutor
     * @return
     */
    @Bean
    public BpmUtils BpmUtils(RedisLockExecutor lockExecutor) {
        return new BpmUtils(lockExecutor);
    }

}