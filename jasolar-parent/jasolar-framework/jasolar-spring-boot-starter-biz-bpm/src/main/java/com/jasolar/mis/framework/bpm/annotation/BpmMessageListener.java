package com.jasolar.mis.framework.bpm.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * BPM消息监听器注解
 * 用于标识消息处理器，并配置队列和路由属性
 *
 * <p>使用此注解的类必须实现 {@link com.jasolar.mis.framework.bpm.handler.BpmMessageHandler} 接口</p>
 *
 * <p>示例:
 * <pre>
 * &#64;BpmMessageListener(
 *     subModule = "order",
 *     routingKeys = "order.created,order.updated",
 *     concurrency = 3
 * )
 * public class OrderMessageHandler implements BpmMessageHandler {
 *     &#64;Override
 *     public void handleMessage(BaseBpmMessageDTO message, BpmMessageContext context) {
 *         // 处理消息
 *         context.ack(); // 确认消息
 *     }
 * }
 * </pre>
 * </p>
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface BpmMessageListener {

    /**
     * 子模块名称，用于构建队列名和路由键 默认情况下使用 {queuePrefix}.{currentModule}.{subModule} 格式
     * 如果指定了queueName，则不会使用子模块名
     */
    String subModule() default "";

    /**
     * 队列名称，默认情况下使用 {queuePrefix}.{currentModule}.{subModule} 格式
     * 如果指定了队列名称，则使用指定的队列名
     * 如果队列名称以 {queuePrefix} 开头，则会自动添加 {currentModule} 前缀
     */
    String queueName() default "";

    /**
     * 路由键，支持通配符，多个路由键使用逗号分隔
     */
    String routingKeys() default "";

    /**
     * 消费者并发数量
     */
    int concurrency() default 1;

    /**
     * 是否使用手动确认模式
     */
    boolean manualAck() default true;

    /**
     * 处理失败时是否重新入队
     */
    boolean requeueOnFail() default true;

    /**
     * 最大重试次数，-1表示无限重试
     */
    int maxRetries() default 3;
} 