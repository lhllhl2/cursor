package com.jasolar.mis.framework.mq.delay.config;

import java.util.List;
import java.util.Map;

import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.CustomExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;

import com.jasolar.mis.framework.mq.rabbitmq.core.BindingProperties;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import lombok.extern.slf4j.Slf4j;

/**
 * 业务待办 自动配置类
 */
@Configuration
@Slf4j
@EnableConfigurationProperties(DelayProperties.class)
@Import({ DelayPublisherConfig.class })
@ConditionalOnProperty(prefix = "jasolar.mq.delay", name = "enabled", matchIfMissing = false)
public class DelayAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "delayExchange")
    public CustomExchange delayExchange(DelayProperties props) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("x-delayed-type", "topic");
        return new CustomExchange(props.getExchange(), "x-delayed-message", true, false, params);
    }

    @Bean("delayBindings")
    @Order(1)
    public List<Binding> delayBindings(CustomExchange delayExchange, DelayProperties props, AmqpAdmin admin) {
        List<Binding> bindings = Lists.newArrayList();
        if (props.getBindings() != null) {
            for (BindingProperties binding : props.getBindings()) {
                Queue queue = QueueBuilder.durable(binding.getQueue()).build();
                admin.declareQueue(queue);

                Binding bind = BindingBuilder.bind(queue).to(delayExchange).with(binding.getRoutingKey()).and(Maps.newHashMap());
                admin.declareBinding(bind);
                bindings.add(bind);
            }
        }
        return bindings;
    }

}