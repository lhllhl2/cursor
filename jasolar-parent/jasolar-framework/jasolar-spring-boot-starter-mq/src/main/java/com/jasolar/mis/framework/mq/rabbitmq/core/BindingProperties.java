package com.jasolar.mis.framework.mq.rabbitmq.core;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * RoutingKey和Queue的绑定关系
 * 
 * @author galuo
 * @date 2025-04-14 18:11
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BindingProperties {
    /** RoutingKey */
    private String routingKey;
    /** 绑定的队列名 */
    private String queue;

    /** 死信队列的exchange */
    private String deadLetterExchange = DeadLetterQueueProperties.DEFAULT_EXCHANGE;

    /** 死信队列的routingKey */
    private String deadLetterRoutingKey = DeadLetterQueueProperties.LOG_ROUTING_KEY;

    /**
     * RoutingKey和队列名一致
     * 
     * @param queue 队列名
     */
    public BindingProperties(String queue) {
        super();
        this.queue = queue;
        this.routingKey = queue;
    }

}
