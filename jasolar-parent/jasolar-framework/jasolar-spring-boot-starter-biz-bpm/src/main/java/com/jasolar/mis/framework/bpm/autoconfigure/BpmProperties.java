package com.jasolar.mis.framework.bpm.autoconfigure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.jasolar.mis.framework.mq.rabbitmq.core.DeadLetterQueueProperties;

import cn.hutool.core.util.StrUtil;
import lombok.Data;

/**
 * BPM消息配置属性
 */
@ConfigurationProperties(prefix = "jasolar.bpm")
@Data
public class BpmProperties {

    /**
     * 是否启用BPM消息功能
     */
    private boolean enabled = true;

    /**
     * 是否启用消息发布者
     */
    private boolean publisherEnabled = true;

    /**
     * 是否启用消息消费者
     */
    private boolean consumerEnabled = true;

    /**
     * 交换机名称
     */
    private String exchangeName = "bpm.topic";

    /**
     * 队列名称前缀
     */
    private String queuePrefix = "bpm.mq";

    /**
     * 路由键前缀
     * 用于区分不同环境或项目的消息
     */
    private String routingKeyPrefix = "bpm.mq";

    /**
     * 当前服务模块名称
     */
    private String currentModule;

    /**
     * 消费配置
     */
    private List<ConsumerConfig> consumers = new ArrayList<>();

    /**
     * 消费者配置
     */
    @Data
    public static class ConsumerConfig {
        /**
         * 子模块，可选（推荐配置）
         * 会自动添加到队列名末尾，如：BPM.order-service.purchase
         * 代表业务领域细分，如采购订单、销售订单等
         */
        private String subModule;

        /**
         * 路由键模式，支持通配符
         * 例如：ORDER.purchase、*.purchase 等
         * 多个路由键使用逗号分隔
         * 不需要手动添加前缀，框架会自动处理
         */
        private String routingKeys = ""; // 默认为空，会根据子模块生成

        /**
         * 队列名称
         * 如果不指定，会自动生成为 {queuePrefix}.{currentModule}.{subModule}
         * 通常不需要手动设置，除非需要特殊命名
         */
        private String queueName;

        /**
         * 消费者数量
         */
        private int concurrency = 1;

        /**
         * 是否启用手动确认模式
         */
        private boolean manualAck = true;

        /**
         * 消息处理失败是否重新入队
         */
        private boolean requeueOnFail = true;

        /**
         * 最大重试次数，超过后消息将被丢弃或发送到死信队列
         * 设为-1表示无限重试
         */
        private int maxRetries = 3;

        /** 死信队列的exchange */
        private String deadLetterExchange = DeadLetterQueueProperties.DEFAULT_EXCHANGE;

        /** 死信队列的routingKey */
        private String deadLetterRoutingKey = DeadLetterQueueProperties.LOG_ROUTING_KEY;

        /**
         * 获取路由键列表，添加前缀处理
         */
        public List<String> getRoutingKeysList() {
            if (StrUtil.isEmpty(routingKeys)) {
                return Collections.emptyList();
            }
            return Arrays.asList(routingKeys.split(","));
        }
    }
}