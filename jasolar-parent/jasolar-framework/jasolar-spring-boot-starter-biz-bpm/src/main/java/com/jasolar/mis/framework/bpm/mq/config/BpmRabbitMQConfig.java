package com.jasolar.mis.framework.bpm.mq.config;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.annotation.Order;

import com.jasolar.mis.framework.bpm.annotation.BpmMessageListener;
import com.jasolar.mis.framework.bpm.autoconfigure.BpmProperties;
import com.jasolar.mis.framework.bpm.handler.BpmMessageHandler;
import com.jasolar.mis.framework.bpm.mq.consumer.SimpleBpmMessageListener;
import com.jasolar.mis.framework.mq.rabbitmq.core.DeadLetterQueueProperties;

import cn.hutool.core.util.StrUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * BPM RabbitMQ 配置
 */
@Configuration(value = "bpmRabbitMQConfig")
@Slf4j
public class BpmRabbitMQConfig {

    /**
     * BPM Topic交换机 - 无论是发布者还是消费者都需要
     */
    @Bean
    @ConditionalOnProperty(prefix = "jasolar.bpm", name = "enabled", matchIfMissing = true)
    public TopicExchange bpmTopicExchange(BpmProperties properties) {
        return new TopicExchange(properties.getExchangeName(), true, false);
    }

    /**
     * 从所有处理器中扫描@BpmQueueListener注解，构建队列配置
     */
    @Bean(name = "bpmQueueConfigurations")
    @Order(1)
    @ConditionalOnProperty(prefix = "jasolar.bpm", name = "consumer-enabled", matchIfMissing = true)
    public List<BpmProperties.ConsumerConfig> scanQueueConfigurations(List<BpmMessageHandler> handlers, BpmProperties properties) {

        log.info("正在扫描@BpmQueueListener注解，收集队列配置...");

        // 合并配置集合
        List<BpmProperties.ConsumerConfig> allConfigs = new ArrayList<>();

        // 先处理配置文件中的配置
        Map<String, BpmProperties.ConsumerConfig> subModuleConfigMap = new HashMap<>();
        Map<String, BpmProperties.ConsumerConfig> queueNameConfigMap = new HashMap<>();

        // 索引现有配置
        if (!properties.getConsumers().isEmpty()) {
            for (BpmProperties.ConsumerConfig config : properties.getConsumers()) {
                allConfigs.add(config);

                // 建立子模块和队列名映射
                if (StrUtil.isNotEmpty(config.getSubModule())) {
                    subModuleConfigMap.put(config.getSubModule(), config);
                }

                if (StrUtil.isNotEmpty(config.getQueueName())) {
                    queueNameConfigMap.put(config.getQueueName(), config);
                }
            }
            log.info("从配置文件读取到{}个队列配置", properties.getConsumers().size());
        }

        // 处理注解配置
        if (!handlers.isEmpty()) {
            log.info("发现{}个消息处理器，开始扫描注解", handlers.size());

            for (BpmMessageHandler handler : handlers) {
                log.info("handler: {}", handler.getClass());
                BpmMessageListener annotation = handler.getClass().getAnnotation(BpmMessageListener.class);

                if (annotation != null) {
                    boolean configFound = false;
                    BpmProperties.ConsumerConfig existingConfig = null;

                    // 按优先级查找匹配的配置:
                    // 1. 匹配队列名
                    if (StrUtil.isNotEmpty(annotation.queueName())) {
                        existingConfig = queueNameConfigMap.get(annotation.queueName());
                        if (existingConfig != null) {
                            configFound = true;
                            log.info("Handler {} 匹配到队列名配置: {}", handler.getClass().getSimpleName(), annotation.queueName());
                        }
                    }

                    // 2. 匹配子模块
                    if (!configFound && StrUtil.isNotEmpty(annotation.subModule())) {
                        existingConfig = subModuleConfigMap.get(annotation.subModule());
                        if (existingConfig != null) {
                            configFound = true;
                            log.info("Handler {} 匹配到子模块配置: {}", handler.getClass().getSimpleName(), annotation.subModule());
                        }
                    }

                    // 合并或新建配置
                    if (configFound) {
                        // 智能合并：注解值优先，但不覆盖非默认的YAML配置
                        mergeAnnotationConfig(existingConfig, annotation);
                        log.info("合并Handler {} 的注解配置到现有配置: {}", handler.getClass().getSimpleName(), existingConfig);
                    } else {
                        // 创建新配置
                        BpmProperties.ConsumerConfig newConfig = createConfigFromAnnotation(annotation);
                        allConfigs.add(newConfig);

                        // 更新映射
                        if (StrUtil.isNotEmpty(newConfig.getSubModule())) {
                            subModuleConfigMap.put(newConfig.getSubModule(), newConfig);
                        }
                        if (StrUtil.isNotEmpty(newConfig.getQueueName())) {
                            queueNameConfigMap.put(newConfig.getQueueName(), newConfig);
                        }

                        log.info("从注解创建新队列配置: handler={}, subModule={}, queueName={}", handler.getClass().getSimpleName(),
                                newConfig.getSubModule(), newConfig.getQueueName());
                    }
                }
            }
        }

        log.info("最终收集到{}个队列配置", allConfigs.size());
        return allConfigs;
    }

    /**
     * 智能合并注解配置到现有配置
     * 规则: 注解中非默认值优先于配置文件中的默认值
     */
    private void mergeAnnotationConfig(BpmProperties.ConsumerConfig config, BpmMessageListener annotation) {
        // 子模块合并
        if (StrUtil.isEmpty(config.getSubModule()) && StrUtil.isNotEmpty(annotation.subModule())) {
            config.setSubModule(annotation.subModule());
        }

        // 队列名合并
        if (StrUtil.isEmpty(config.getQueueName()) && StrUtil.isNotEmpty(annotation.queueName())) {
            config.setQueueName(annotation.queueName());
        }

        // 路由键合并
        if (StrUtil.isEmpty(config.getRoutingKeys()) && StrUtil.isNotEmpty(annotation.routingKeys())) {
            config.setRoutingKeys(annotation.routingKeys());
        }

        // 只合并注解中的非默认值
        if (annotation.concurrency() != 1) {
            config.setConcurrency(annotation.concurrency());
        }

        if (!annotation.manualAck()) {
            config.setManualAck(false);
        }

        if (!annotation.requeueOnFail()) {
            config.setRequeueOnFail(false);
        }

        if (annotation.maxRetries() != 3) {
            config.setMaxRetries(annotation.maxRetries());
        }
    }

    /**
     * 从注解创建新的配置对象
     */
    private BpmProperties.ConsumerConfig createConfigFromAnnotation(BpmMessageListener annotation) {
        BpmProperties.ConsumerConfig config = new BpmProperties.ConsumerConfig();
        config.setSubModule(annotation.subModule());
        config.setQueueName(annotation.queueName());
        config.setRoutingKeys(annotation.routingKeys());
        config.setConcurrency(annotation.concurrency());
        config.setManualAck(annotation.manualAck());
        config.setRequeueOnFail(annotation.requeueOnFail());
        config.setMaxRetries(annotation.maxRetries());
        return config;
    }

    /**
     * 配置消息队列 - 基于扫描的所有配置
     */
    @Bean
    @Order(2)
    @ConditionalOnProperty(prefix = "jasolar.bpm", name = "consumer-enabled", matchIfMissing = true)
    public List<Queue> bpmQueues(@Qualifier("bpmQueueConfigurations") List<BpmProperties.ConsumerConfig> configs, BpmProperties properties,
            DeadLetterQueueProperties dlxProps) {

        List<Queue> queues = new ArrayList<>();

        if (configs.isEmpty()) {
            log.warn("未发现任何队列配置，将不会创建任何队列");
            return queues;
        }

        // 创建队列
        for (BpmProperties.ConsumerConfig config : configs) {
            String queueName = getQueueName(properties, config);
            QueueBuilder builder = QueueBuilder.durable(queueName);
            if (dlxProps.isEnabled()) {
                // 异常后加入死信队列记录日志
                builder.deadLetterExchange(config.getDeadLetterExchange()).deadLetterRoutingKey(config.getDeadLetterRoutingKey());
            }
            Queue queue = builder.build();
            queues.add(queue);
            log.info("创建BPM消息队列: {}", queueName);
        }

        return queues;
    }

    /**
     * 配置队列绑定关系 - 使用收集的所有配置
     */
    @Bean
    @ConditionalOnProperty(prefix = "jasolar.bpm", name = "consumer-enabled", matchIfMissing = true)
    public List<Binding> bpmBindings(@Qualifier("bpmQueueConfigurations") List<BpmProperties.ConsumerConfig> configs,
            BpmProperties properties, @Qualifier("bpmTopicExchange") TopicExchange exchange,
            @Qualifier("bpmQueues") List<Queue> bpmQueues) {

        List<Binding> bindings = new ArrayList<>();

        int index = 0;
        for (BpmProperties.ConsumerConfig config : configs) {
            Queue queue = bpmQueues.get(index++);

            // 获取用户配置的路由键列表
            List<String> routingKeys = config.getRoutingKeysList();

            // 如果用户没有指定路由键且有子模块，则使用子模块特定路由键
            if (routingKeys.isEmpty() && StrUtil.isNotEmpty(config.getSubModule())) {
                // 使用已有的方法生成子模块级别的路由键
                // 由于方法需要模块名，使用通配符
                String subModuleRoute = getRoutingKeySubmodule(properties.getRoutingKeyPrefix(), properties.getCurrentModule(),
                        config.getSubModule());
                bindings.add(BindingBuilder.bind(queue).to(exchange).with(subModuleRoute));
                log.info("绑定队列 {} 到交换机 {}, 路由键(子模块精确): {}", queue.getName(), exchange.getName(), subModuleRoute);
            } else {
                // 处理用户指定的路由键
                for (String routingKey : routingKeys) {
                    // 如果用户配置的路由键已经包含了前缀，则直接使用
                    String fullRoutingKey = routingKey;
                    if (!routingKey.startsWith(properties.getRoutingKeyPrefix() + ".")
                            && !routingKey.equals(properties.getRoutingKeyPrefix())) {
                        // 用户没有指定前缀，添加全局前缀
                        fullRoutingKey = properties.getRoutingKeyPrefix() + "." + routingKey;
                    }

                    bindings.add(BindingBuilder.bind(queue).to(exchange).with(fullRoutingKey));
                    log.info("绑定队列 {} 到交换机 {}, 路由键(用户指定): {}", queue.getName(), exchange.getName(), fullRoutingKey);
                }
            }
        }

        return bindings;
    }

    /**
     * 配置消息监听容器 - 确保所有依赖已初始化
     */
    @Bean
    @Order(4)
    @ConditionalOnProperty(prefix = "jasolar.bpm", name = "consumer-enabled", matchIfMissing = true)
    @DependsOn("bpmRabbitMQInitializer")
    public List<SimpleMessageListenerContainer> bpmListenerContainers(ConnectionFactory connectionFactory,
            @Qualifier("bpmQueueConfigurations") List<BpmProperties.ConsumerConfig> configs, BpmProperties properties,
            /* @Qualifier("bpmMessageConverter") */ MessageConverter converter, List<BpmMessageHandler> handlers) {

        List<SimpleMessageListenerContainer> containers = new ArrayList<>();

        // 队列名称到Handler的映射
        Map<String, BpmMessageHandler> queueHandlerMap = new HashMap<>();
        Map<String, BpmMessageHandler> subModuleHandlerMap = new HashMap<>();

        // 注解配置集合 - 从处理器中读取注解
        List<ConsumerDefinition> annotationConfigs = new ArrayList<>();

        // 检查处理器
        if (handlers.isEmpty()) {
            log.warn("未找到BpmMessageHandler实现，消息将无法被处理！");
        } else {
            log.info("注入了{}个BpmMessageHandler实现", handlers.size());

            // 处理注解信息
            for (BpmMessageHandler handler : handlers) {
                log.info("正在处理Handler: {}", handler.getClass().getName());
                BpmMessageListener annotation = handler.getClass().getAnnotation(BpmMessageListener.class);

                if (annotation != null) {
                    // 从注解创建消费者定义
                    ConsumerDefinition definition = new ConsumerDefinition();
                    definition.setHandler(handler);
                    definition.setAnnotation(annotation);

                    // 转换注解到属性
                    if (StrUtil.isNotEmpty(annotation.subModule())) {
                        subModuleHandlerMap.put(annotation.subModule(), handler);
                        definition.setSubModule(annotation.subModule());
                        log.info("将Handler {} 与子模块 {} 关联", handler.getClass().getSimpleName(), annotation.subModule());
                    }

                    if (StrUtil.isNotEmpty(annotation.queueName())) {
                        queueHandlerMap.put(annotation.queueName(), handler);
                        definition.setQueueName(annotation.queueName());
                        log.info("将Handler {} 与队列名 {} 关联", handler.getClass().getSimpleName(), annotation.queueName());
                    }

                    // 添加到注解配置集合
                    annotationConfigs.add(definition);
                } else {
                    log.warn("Handler {} 没有@BpmQueueListener注解", handler.getClass().getSimpleName());
                }
            }
        }

        // 处理配置文件中的消费者配置
        List<BpmProperties.ConsumerConfig> combinedConfigs = new ArrayList<>();

        // 如果配置文件中有消费者配置，则添加到组合配置
        if (!properties.getConsumers().isEmpty()) {
            combinedConfigs.addAll(properties.getConsumers());
        }

        // 如果有注解配置，且配置文件中没有对应的配置，则添加到组合配置
        for (ConsumerDefinition annotationConfig : annotationConfigs) {
            boolean found = false;

            // 检查是否已在配置文件中存在
            for (BpmProperties.ConsumerConfig config : properties.getConsumers()) {
                if (annotationConfig.getSubModule().equals(config.getSubModule())) {
                    found = true;
                    break;
                }
            }

            // 如果配置文件中没有，则从注解创建
            if (!found) {
                BpmProperties.ConsumerConfig config = new BpmProperties.ConsumerConfig();
                config.setSubModule(annotationConfig.getSubModule());
                config.setQueueName(annotationConfig.getQueueName());
                config.setRoutingKeys(annotationConfig.getAnnotation().routingKeys());
                config.setConcurrency(annotationConfig.getAnnotation().concurrency());
                config.setManualAck(annotationConfig.getAnnotation().manualAck());
                config.setRequeueOnFail(annotationConfig.getAnnotation().requeueOnFail());
                config.setMaxRetries(annotationConfig.getAnnotation().maxRetries());

                combinedConfigs.add(config);
                log.info("从注解创建消费者配置: subModule={}, queueName={}", config.getSubModule(), config.getQueueName());
            }
        }

        // 如果没有任何配置，则发出警告
        if (combinedConfigs.isEmpty()) {
            log.warn("未找到任何消费者配置，既没有配置类定义也没有注解定义");
            return containers;
        }

        // // 创建队列和绑定关系
        // List<Queue> queues = new ArrayList<>();
        // List<Binding> bindings = new ArrayList<>();

        for (BpmProperties.ConsumerConfig config : combinedConfigs) {
            // 生成队列名称
            String queueName = getQueueName(properties, config);

            // // 创建队列
            // Queue queue = QueueBuilder.durable(queueName).build();
            // queues.add(queue);
            // log.info("创建BPM消息队列: {}", queueName);
            //
            // // 创建绑定关系
            // TopicExchange exchange = new TopicExchange(properties.getExchangeName(), true, false);
            //
            // // 获取用户配置的路由键列表
            // List<String> routingKeys = new ArrayList<>();
            // if (StrUtil.isNotEmpty(config.getRoutingKeys())) {
            // routingKeys = Arrays.asList(config.getRoutingKeys().split(","));
            // }
            //
            // // 如果用户没有指定路由键且有子模块，则使用子模块特定路由键
            // if (routingKeys.isEmpty() && StrUtil.isNotEmpty(config.getSubModule())) {
            // String subModuleRoute = getRoutingKeySubmodule(properties.getRoutingKeyPrefix(), properties.getCurrentModule(),
            // config.getSubModule());
            // bindings.add(BindingBuilder.bind(queue).to(exchange).with(subModuleRoute));
            // log.info("绑定队列 {} 到交换机 {}, 路由键: {}", queue.getName(), exchange.getName(), subModuleRoute);
            // } else {
            // // 处理用户指定的路由键
            // for (String routingKey : routingKeys) {
            // String fullRoutingKey = routingKey;
            // if (!routingKey.startsWith(properties.getRoutingKeyPrefix() + ".")) {
            // fullRoutingKey = properties.getRoutingKeyPrefix() + "." + routingKey;
            // }
            //
            // bindings.add(BindingBuilder.bind(queue).to(exchange).with(fullRoutingKey));
            // log.info("绑定队列 {} 到交换机 {}, 路由键: {}", queue.getName(), exchange.getName(), fullRoutingKey);
            // }
            // }

            // 使用RabbitAdmin声明队列和绑定
            // RabbitAdmin rabbitAdmin = new RabbitAdmin(connectionFactory);
            // rabbitAdmin.declareExchange(exchange);
            // rabbitAdmin.declareQueue(queue);
            // for (Binding binding : bindings) {
            // rabbitAdmin.declareBinding(binding);
            // }

            // 创建监听容器
            SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory);
            container.setQueueNames(queueName);
            container.setConcurrentConsumers(config.getConcurrency());

            // 设置消费者标签生成策略
            container.setConsumerTagStrategy(s -> {
                String env = System.getProperty("spring.profiles.active", "unknown");
                String hostName;
                try {
                    hostName = InetAddress.getLocalHost().getHostName();
                } catch (Exception e) {
                    hostName = "unknown-host";
                }

                return String.format("bpm-%s-%s-%s-%s-%d", properties.getCurrentModule(), config.getSubModule(), env, hostName,
                        System.currentTimeMillis() % 10000);
            });

            // 配置手动确认模式
            container.setAcknowledgeMode(config.isManualAck() ? AcknowledgeMode.MANUAL : AcknowledgeMode.AUTO);

            // 创建消息监听器并配置到容器
            List<BpmMessageHandler> specificHandlers = findHandlersForConfig(config, handlers, queueHandlerMap, subModuleHandlerMap);

            SimpleBpmMessageListener listener = new SimpleBpmMessageListener(specificHandlers, queueName, config.getSubModule(), config);
            listener.setMessageConverter(converter);
            container.setMessageListener(listener);

            // 设置容器恢复参数
            container.setMissingQueuesFatal(false);
            container.setRecoveryInterval(3000);
            container.setPrefetchCount(1);

            // 显式启动容器
            container.start();

            containers.add(container);
            log.info("创建BPM消息监听容器，监听队列: {}", queueName);
        }

        return containers;
    }

    /**
     * 为配置找到合适的处理器
     */
    private List<BpmMessageHandler> findHandlersForConfig(BpmProperties.ConsumerConfig config, List<BpmMessageHandler> allHandlers,
            Map<String, BpmMessageHandler> queueHandlerMap, Map<String, BpmMessageHandler> subModuleHandlerMap) {

        List<BpmMessageHandler> specificHandlers = new ArrayList<>();

        // 优先使用子模块匹配
        if (StrUtil.isNotEmpty(config.getSubModule())) {
            BpmMessageHandler handler = subModuleHandlerMap.get(config.getSubModule());
            if (handler != null) {
                specificHandlers.add(handler);
                log.info("使用子模块 [{}] 专用处理器: {}", config.getSubModule(), handler.getClass().getSimpleName());
                return specificHandlers;
            }
        }

        // 其次使用队列名匹配
        if (StrUtil.isNotEmpty(config.getQueueName())) {
            BpmMessageHandler handler = queueHandlerMap.get(config.getQueueName());
            if (handler != null) {
                specificHandlers.add(handler);
                log.info("使用队列名 [{}] 专用处理器: {}", config.getQueueName(), handler.getClass().getSimpleName());
                return specificHandlers;
            }
        }

        // 最后使用所有处理器
        log.info("未找到专用处理器，使用所有处理器");
        specificHandlers.addAll(allHandlers);
        return specificHandlers;
    }

    /**
     * 消费者定义 - 用于保存注解信息
     */
    @Data
    private static class ConsumerDefinition {
        private BpmMessageHandler handler;
        private BpmMessageListener annotation;
        private String subModule = "";
        private String queueName = "";

        // getters and setters...
    }

    /**
     * 获取队列名称 - 简化版本
     */
    private String getQueueName(BpmProperties properties, BpmProperties.ConsumerConfig config) {
        // 1. 如果用户指定了队列名，直接使用
        if (StrUtil.isNotEmpty(config.getQueueName())) {
            return config.getQueueName();
        }

        // 2. 否则生成一个简单的队列名：前缀.当前服务名[.子模块]
        StringBuilder queueName = new StringBuilder(properties.getQueuePrefix());

        // 添加当前服务名
        if (StrUtil.isNotEmpty(properties.getCurrentModule())) {
            queueName.append(".").append(properties.getCurrentModule());
        } else {
            queueName.append(".default"); // 防止队列名冲突
        }

        // 添加子模块(如果有)
        if (StrUtil.isNotEmpty(config.getSubModule())) {
            queueName.append(".").append(config.getSubModule());
        }

        return queueName.toString();
    }

    /**
     * 获取子模块级别的路由键
     */
    private String getRoutingKeySubmodule(String prefix, String module, String subModule) {
        return prefix + "." + module + "." + subModule;
    }

    @Bean
    @Order(3)
    @ConditionalOnProperty(prefix = "jasolar.bpm", name = "enabled", matchIfMissing = true)
    public BpmRabbitMQInitializer bpmRabbitMQInitializer(ConnectionFactory connectionFactory,
            @Qualifier("bpmTopicExchange") TopicExchange bpmTopicExchange, @Qualifier("bpmQueues") List<Queue> bpmQueues,
            @Qualifier("bpmBindings") List<Binding> bpmBindings) {
        return new BpmRabbitMQInitializer(connectionFactory, bpmTopicExchange, bpmQueues, bpmBindings);
    }
}