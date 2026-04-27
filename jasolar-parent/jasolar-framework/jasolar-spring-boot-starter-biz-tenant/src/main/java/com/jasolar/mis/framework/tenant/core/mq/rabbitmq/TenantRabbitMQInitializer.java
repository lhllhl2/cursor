package com.jasolar.mis.framework.tenant.core.mq.rabbitmq;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * 多租户的 RabbitMQ 初始化器
 *
 * @author zhaohuang
 */
public class TenantRabbitMQInitializer implements BeanPostProcessor {

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof RabbitTemplate template) {
            RabbitTemplate rabbitTemplate = template;
            rabbitTemplate.addBeforePublishPostProcessors(new TenantRabbitMQMessagePostProcessor());
        }
        return bean;
    }

}