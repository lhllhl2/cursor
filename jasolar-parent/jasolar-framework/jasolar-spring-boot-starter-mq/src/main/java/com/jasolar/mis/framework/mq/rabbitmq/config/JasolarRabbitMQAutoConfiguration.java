package com.jasolar.mis.framework.mq.rabbitmq.config;

import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import com.jasolar.mis.framework.mq.rabbitmq.core.DeadLetterQueueProperties;

import lombok.extern.slf4j.Slf4j;

/**
 * RabbitMQ 消息队列配置类
 *
 * @author zhaohuang
 */
@AutoConfiguration
@Slf4j
@ConditionalOnClass(name = "org.springframework.amqp.rabbit.core.RabbitTemplate")
@EnableConfigurationProperties(DeadLetterQueueProperties.class)
public class JasolarRabbitMQAutoConfiguration {

    /**
     * Jackson2JsonMessageConverter Bean：使用 jackson 序列化消息
     */
    @Bean
    public MessageConverter createMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

}
