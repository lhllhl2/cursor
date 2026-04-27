package com.jasolar.mis.framework.mq.delay.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.jasolar.mis.framework.mq.rabbitmq.core.BindingProperties;

import lombok.Data;

/**
 * 延时的配置参数
 * 
 *
 */
@ConfigurationProperties(prefix = "jasolar.mq.delay")
@Data
public class DelayProperties {

    /** 是否开启延时MQ配置 */
    private boolean enabled = false;

    /** EXCHANGE, 待办使用CustomExchange */
    private String exchange = "mq.delayed";

    /** 待办消息发布者 */
    private Publisher publisher = new Publisher();

    /** 创建延时的的RoutingKey */
    private List<BindingProperties> bindings;

    /**
     * 消息发布者的配置,用于每个业务服务,默认开启
     * 
     * @author galuo
     * @date 2025-04-14 14:16
     *
     */
    @Data
    public static class Publisher {
        /** 是否待办消息发布者 */
        private boolean enabled = false;
    }

}
