package com.jasolar.mis.framework.biztask.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;

import com.jasolar.mis.framework.biztask.BizTaskConsumerService;
import com.jasolar.mis.framework.biztask.mq.BizTaskConsumer;
import com.jasolar.mis.framework.mq.rabbitmq.core.DeadLetterQueueProperties;

/**
 * 消费者配置, 主要用于infra服务, 在infra服务中监听业务发送的消息创建或完成业务待办.
 * 消息的监听放到infra业务中进行实现
 * 
 * @author galuo
 * @date 2025-04-14 14:28
 *
 */
@ConditionalOnProperty(prefix = "jasolar.biztask.consumer", name = "enabled", matchIfMissing = false)
public class BizTaskConsumerConfig {

    @Bean
    @Order(Integer.MAX_VALUE)
    public BizTaskConsumer bizTaskConsumer(BizTaskConsumerService bizTaskConsumerService, MessageConverter messageConverter,
            BizTaskProperties props, @Qualifier("bizTaskBindings") List<Binding> bizTaskBindings) {
        return new BizTaskConsumer(bizTaskConsumerService, messageConverter, props);
    }

    @Bean("bizTaskBindings")
    @Order(1)
    public List<Binding> bizTaskBindings(DirectExchange bizTaskExchange, BizTaskProperties props, AmqpAdmin admin,
            DeadLetterQueueProperties dlxProps) {

        boolean dlx = dlxProps.isEnabled();

        Queue pendingQueue = (!dlx ? QueueBuilder.durable(props.getPending().getQueue())
                : QueueBuilder.durable(props.getPending().getQueue()).deadLetterExchange(props.getPending().getDeadLetterExchange())
                        .deadLetterRoutingKey(props.getPending().getDeadLetterRoutingKey())).build();
        Queue completedQueue = (!dlx ? QueueBuilder.durable(props.getCompleted().getQueue())
                : QueueBuilder.durable(props.getCompleted().getQueue()).deadLetterExchange(props.getPending().getDeadLetterExchange())
                        .deadLetterRoutingKey(props.getPending().getDeadLetterRoutingKey())).build();
        Queue deletedQueue = (!dlx ? QueueBuilder.durable(props.getDeleted().getQueue())
                : QueueBuilder.durable(props.getDeleted().getQueue()).deadLetterExchange(props.getPending().getDeadLetterExchange())
                        .deadLetterRoutingKey(props.getPending().getDeadLetterRoutingKey())).build();

        admin.declareQueue(pendingQueue);
        admin.declareQueue(completedQueue);
        admin.declareQueue(deletedQueue);

        Binding pending = BindingBuilder.bind(pendingQueue).to(bizTaskExchange).with(props.getPending().getRoutingKey());
        Binding completed = BindingBuilder.bind(completedQueue).to(bizTaskExchange).with(props.getCompleted().getRoutingKey());
        Binding deleted = BindingBuilder.bind(deletedQueue).to(bizTaskExchange).with(props.getDeleted().getRoutingKey());

        List<Binding> bindings = Arrays.asList(pending, completed, deleted);
        bindings.forEach(binding -> admin.declareBinding(binding));
        return bindings;
    }

}
