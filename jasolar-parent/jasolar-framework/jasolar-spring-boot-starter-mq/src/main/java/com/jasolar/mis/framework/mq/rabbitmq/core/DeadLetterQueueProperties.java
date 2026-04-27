package com.jasolar.mis.framework.mq.rabbitmq.core;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * 死信队列的配置
 * 
 * @author galuo
 * @date 2025-05-13 13:12
 *
 */
@ConfigurationProperties(prefix = "jasolar.mq.dead")
@Data
public class DeadLetterQueueProperties {

    /** 死信队列默认的exchange名称, 会在infra中监听记录错误日志 */
    public static final String DEFAULT_EXCHANGE = "dead.topic";

    /** 死信队列默认路由, 会在infra中监听记录错误日志 */
    public static final String LOG_ROUTING_KEY = "dead.log";

    /** 死信队列默认的队列, 会在infra中监听记录错误日志 */
    public static final String LOG_QUEUE = LOG_ROUTING_KEY;

    /** 是否开启死信队列的配置 */
    private boolean enabled = true;

    /** TopicExchange 的名称 */
    private String exchange = DEFAULT_EXCHANGE;

    /** Queue和RoutingKey的绑定 */
    private List<BindingProperties> bindings;

    /** infra记录死信队列日志的配置 */
    private BindingProperties log = new BindingProperties(LOG_QUEUE);

}
