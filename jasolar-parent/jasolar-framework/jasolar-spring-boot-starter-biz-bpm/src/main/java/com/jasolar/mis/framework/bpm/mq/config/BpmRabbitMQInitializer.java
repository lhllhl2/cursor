package com.jasolar.mis.framework.bpm.mq.config;

import java.util.List;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

/**
 * BPM消息队列初始化器
 * 负责在应用启动后声明队列和绑定关系
 */
@Configuration
@ConditionalOnProperty(prefix = "jasolar.bpm", name = "enabled", matchIfMissing = true)
@Slf4j
public class BpmRabbitMQInitializer {

    private final ConnectionFactory connectionFactory;
    private final TopicExchange bpmTopicExchange;
    private final List<Queue> bpmQueues;
    private final List<Binding> bpmBindings;
    
    static {
        System.out.println("BpmRabbitMQInitializer 类被加载");
    }

    public BpmRabbitMQInitializer(ConnectionFactory connectionFactory, TopicExchange bpmTopicExchange, List<Queue> bpmQueues, List<Binding> bpmBindings) {
        this.connectionFactory = connectionFactory;
        this.bpmTopicExchange = bpmTopicExchange;
        this.bpmQueues = bpmQueues;
        this.bpmBindings = bpmBindings;
        System.out.println("BpmRabbitMQInitializer 构造函数被调用");
    }

    /**
     * 初始化时显式声明队列和绑定关系
     */
    @PostConstruct
    public void init() {
        // 避免重复初始化
        synchronized (BpmRabbitMQInitializer.class) {
            if (initialized) {
                log.info("BPM消息队列已经初始化，跳过");
                return;
            }
            
            try {
                RabbitAdmin rabbitAdmin = new RabbitAdmin(connectionFactory);
                
                // 声明交换机
                rabbitAdmin.declareExchange(bpmTopicExchange);
                log.info("声明交换机: {}", bpmTopicExchange.getName());
                
                // 声明队列
                if (bpmQueues != null && !bpmQueues.isEmpty()) {
                    for (Queue queue : bpmQueues) {
                        rabbitAdmin.declareQueue(queue);
                        log.info("声明队列: {}", queue.getName());
                    }
                } else {
                    log.warn("未配置BPM队列，跳过队列声明");
                }
                
                // 声明绑定关系
                if (bpmBindings != null && !bpmBindings.isEmpty()) {
                    for (Binding binding : bpmBindings) {
                        rabbitAdmin.declareBinding(binding);
                        log.info("声明绑定: {}", binding);
                    }
                } else {
                    log.warn("未配置BPM绑定关系，跳过绑定声明");
                }
                
                log.info("BPM消息队列和绑定关系初始化完成");
                
                initialized = true;
            } catch (Exception e) {
                log.error("初始化BPM消息队列失败", e);
            }
        }
    }

    // 添加标记
    private static volatile boolean initialized = false;

    @Bean
    @ConditionalOnProperty(prefix = "jasolar.bpm", name = "enabled", matchIfMissing = true)
    public BpmRabbitMQInitializer bpmRabbitMQInitializer(
            ConnectionFactory connectionFactory,
            TopicExchange bpmTopicExchange,
            List<Queue> bpmQueues,
            List<Binding> bpmBindings) {
        return new BpmRabbitMQInitializer(connectionFactory, bpmTopicExchange, bpmQueues, bpmBindings);
    }
} 