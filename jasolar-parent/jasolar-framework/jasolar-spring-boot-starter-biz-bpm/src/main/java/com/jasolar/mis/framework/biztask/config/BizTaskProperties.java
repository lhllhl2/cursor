package com.jasolar.mis.framework.biztask.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.jasolar.mis.framework.mq.rabbitmq.core.BindingProperties;

import lombok.Data;

/**
 * 业务待办的配置参数
 * 
 * @author galuo
 * @date 2025-04-14 13:59
 *
 */
@ConfigurationProperties(prefix = "jasolar.biztask")
@Data
public class BizTaskProperties {

    public static final String DEFAULT_QUEUE_PENDING = "biztask.pendings";
    public static final String DEFAULT_QUEUE_COMPLETED = "biztask.completeds";
    public static final String DEFAULT_QUEUE_DELETED = "biztask.deleteds";

    /** 是否开启业务待办MQ配置 */
    private boolean enabled = true;

    /** EXCHANGE, 待办使用direct exchange */
    private String exchange = "biztask.direct";

    /** 待办消息发布者 */
    private Publisher publisher = new Publisher();

    /** 待办消息接收者 */
    private Consumer consumer = new Consumer();

    /** 创建待办的RoutingKey */
    private BindingProperties pending = new BindingProperties(DEFAULT_QUEUE_PENDING);
    /** 待办完成后变成已办的RoutingKey */
    private BindingProperties completed = new BindingProperties(DEFAULT_QUEUE_COMPLETED);
    /** 删除待办的RoutingKey */
    private BindingProperties deleted = new BindingProperties(DEFAULT_QUEUE_DELETED);

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
        private boolean enabled = true;

    }

    /**
     * 
     * 消息发布者的配置,主要用于infra服务,在infra中生成业务待办
     * 
     * @author galuo
     * @date 2025-04-14 14:17
     *
     */
    @Data
    public static class Consumer {
        /** 是否启用消息消费者,默认为false,需要在infra项目下开启 */
        private boolean enabled = false;

        /** 消息消费失败是否需要重新入队, 默认为false */
        private boolean requeue = false;

    }

}
