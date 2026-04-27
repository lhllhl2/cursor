package com.jasolar.mis.framework.mq.delay.config;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

import com.jasolar.mis.framework.mq.delay.DelayPublisher;
import com.jasolar.mis.framework.mq.delay.DelayPublisherService;

/**
 * 
 * 生产者配置, 主要用于各业务服务,在创建/删除/完成待办时发送消息到infra
 * 
 * @author galuo
 * @date 2025-04-14 15:25
 *
 */
@ConditionalOnProperty(prefix = "jasolar.mq.delay.publisher", name = "enabled", matchIfMissing = false)
public class DelayPublisherConfig {

    @Bean
    public DelayPublisher delayTaskPublisher(RabbitTemplate rabbitTemplate, DelayProperties props) {
        return new DelayPublisher(rabbitTemplate, props);
    }

    @Bean
    public DelayPublisherService delayPublisherService(DelayPublisher delayTaskPublisher) {
        return new DelayPublisherService(delayTaskPublisher);
    }
}
