package com.jasolar.mis.framework.mq.rabbitmq.config;

import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import com.jasolar.mis.framework.mq.rabbitmq.core.BindingProperties;
import com.jasolar.mis.framework.mq.rabbitmq.core.DeadLetterQueueProperties;

/**
 * 用于配置死信队列
 * 
 * @author galuo
 * @date 2025-05-13 12:36
 *
 */
@Configuration
@ConditionalOnProperty(prefix = "jasolar.mq.dead", name = "enabled", matchIfMissing = true)
@ConditionalOnClass(name = "org.springframework.amqp.rabbit.core.RabbitTemplate")
public class DeadLetterQueueAutoConfig {

    @Bean
    @ConditionalOnMissingBean(name = "deadLetterExchange")
    @Order(-100)
    public TopicExchange deadLetterExchange(DeadLetterQueueProperties props, AmqpAdmin admin) {
        TopicExchange exchange = new TopicExchange(props.getExchange());
        admin.declareExchange(exchange);

        if (props.getBindings() != null) {
            for (BindingProperties binding : props.getBindings()) {
                Queue queue = QueueBuilder.durable(binding.getQueue()).build();
                admin.declareQueue(queue);
                Binding bind = BindingBuilder.bind(queue).to(exchange).with(binding.getRoutingKey());
                admin.declareBinding(bind);

            }
        }

        // infra中记录日志的死信队列
        BindingProperties binding = props.getLog();
        Queue queue = QueueBuilder.durable(binding.getQueue()).build();
        admin.declareQueue(queue);
        Binding bind = BindingBuilder.bind(queue).to(exchange).with(binding.getRoutingKey());
        admin.declareBinding(bind);

        return exchange;
    }
}
