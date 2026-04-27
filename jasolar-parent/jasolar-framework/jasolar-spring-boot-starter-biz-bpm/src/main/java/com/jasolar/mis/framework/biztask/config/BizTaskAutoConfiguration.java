package com.jasolar.mis.framework.biztask.config;

import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.jasolar.mis.framework.mq.rabbitmq.config.DeadLetterQueueAutoConfig;

import lombok.extern.slf4j.Slf4j;

/**
 * 业务待办 自动配置类
 */
@Configuration
@Slf4j
@EnableConfigurationProperties(BizTaskProperties.class)
@Import({ BizTaskPublisherConfig.class, BizTaskConsumerConfig.class })
@ConditionalOnProperty(prefix = "jasolar.biztask", name = "enabled", matchIfMissing = true)
@AutoConfigureAfter(DeadLetterQueueAutoConfig.class)
public class BizTaskAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "bizTaskExchange")
    public DirectExchange bizTaskExchange(BizTaskProperties props, AmqpAdmin admin) {
        DirectExchange exchange = new DirectExchange(props.getExchange());
        admin.declareExchange(exchange);
        return exchange;
    }

}