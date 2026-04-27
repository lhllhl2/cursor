package com.jasolar.mis.framework.biztask.config;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

import com.jasolar.mis.framework.biztask.BizTaskPublisherService;
import com.jasolar.mis.framework.biztask.mq.BizTaskPublisher;

/**
 * 
 * 生产者配置, 主要用于各业务服务,在创建/删除/完成待办时发送消息到infra
 * 
 * @author galuo
 * @date 2025-04-14 15:25
 *
 */
@ConditionalOnProperty(prefix = "jasolar.biztask.publisher", name = "enabled", matchIfMissing = true)
public class BizTaskPublisherConfig {

    @Bean
    public BizTaskPublisher bizTaskPublisher(RabbitTemplate rabbitTemplate, BizTaskProperties props) {
        return new BizTaskPublisher(rabbitTemplate, props);
    }

    @Bean
    public BizTaskPublisherService bizTaskPublisherService(BizTaskPublisher bizTaskPublisher) {
        return new BizTaskPublisherService(bizTaskPublisher);
    }
}
