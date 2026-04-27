package com.jasolar.mis.framework.bpm.mq.consumer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.amqp.support.converter.MessageConverter;

import com.jasolar.mis.framework.bpm.annotation.BpmMessageListener;
import com.jasolar.mis.framework.bpm.autoconfigure.BpmProperties;
import com.jasolar.mis.framework.bpm.handler.BpmMessageHandler;
import com.jasolar.mis.framework.bpm.mq.context.BpmMessageContext;
import com.jasolar.mis.module.bpm.api.message.dto.BaseBpmMessageDTO;
import com.rabbitmq.client.Channel;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * BPM消息监听器
 */
@Slf4j
public class SimpleBpmMessageListener implements ChannelAwareMessageListener {

    private final List<BpmMessageHandler> handlers;
    private final String queueName;
    private final String subModule;
    private final BpmProperties.ConsumerConfig config;
    private MessageConverter converter;

    // 队列名称到Handler的映射
    private final Map<String, BpmMessageHandler> queueHandlerMap = new HashMap<>();
    // 子模块到Handler的映射
    private final Map<String, BpmMessageHandler> subModuleHandlerMap = new HashMap<>();

    public SimpleBpmMessageListener(List<BpmMessageHandler> handlers, String queueName, String subModule,
            BpmProperties.ConsumerConfig config) {
        this.handlers = handlers;
        this.queueName = queueName;
        this.subModule = subModule;
        this.config = config;

        // 根据注解初始化映射关系
        initHandlerMappings();
    }

    private void initHandlerMappings() {
        // 尝试找到特定匹配队列名称的Handler
        for (BpmMessageHandler handler : handlers) {
            BpmMessageListener annotation = handler.getClass().getAnnotation(BpmMessageListener.class);
            if (annotation != null) {
                // 根据队列名称匹配
                if (StrUtil.isNotEmpty(annotation.queueName())) {
                    queueHandlerMap.put(annotation.queueName(), handler);
                    log.info("Handler {} 绑定到队列 {}", handler.getClass().getSimpleName(), annotation.queueName());
                }
                // 根据子模块匹配
                if (StrUtil.isNotEmpty(annotation.subModule())) {
                    subModuleHandlerMap.put(annotation.subModule(), handler);
                    log.info("Handler {} 绑定到子模块 {}", handler.getClass().getSimpleName(), annotation.subModule());
                }
            }
        }
    }

    @Override
    public void onMessage(Message message, Channel channel) throws Exception {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();

        // 从消息属性中获取重试次数
        Integer retryCount = (Integer) message.getMessageProperties().getHeaders().get("x-retry-count");
        int attemptCount = retryCount == null ? 0 : retryCount;

        try {
            // 记录更详细的连接信息
            log.info("收到消息: deliveryTag={}, queueName={}, channel={}, 尝试次数={}", deliveryTag, queueName, channel, attemptCount);
            log.info("连接信息: channelNumber={}, isOpen={}", channel.getChannelNumber(), channel.isOpen());

            if (converter == null) {
                log.error("消息转换器未设置，无法处理消息");
                channel.basicReject(deliveryTag, true);
                return;
            }

            // 转换消息
            Object obj = converter.fromMessage(message);
            if (!(obj instanceof BaseBpmMessageDTO)) {
                log.error("收到非BPM消息：{}", obj);
                channel.basicReject(deliveryTag, false);
                return;
            }

            BaseBpmMessageDTO bpmMessage = (BaseBpmMessageDTO) obj;
            log.info("收到BPM消息: {}", bpmMessage);

            // 检查子模块是否匹配
            if (StrUtil.isNotEmpty(subModule) && !StrUtil.equals(subModule, bpmMessage.getSubModule())) {
                log.debug("消息子模块 {} 与当前监听子模块 {} 不匹配，跳过处理", bpmMessage.getSubModule(), subModule);
                // 确认消息，但不处理
                channel.basicAck(deliveryTag, false);
                return;
            }

            // 简化处理器查找逻辑：队列名称 > 子模块 > 所有处理器
            BpmMessageHandler targetHandler = null;

            // 1. 首先尝试通过队列名称匹配
            if (StrUtil.isNotEmpty(queueName)) {
                targetHandler = queueHandlerMap.get(queueName);
            }

            // 2. 然后尝试通过子模块匹配
            if (targetHandler == null && StrUtil.isNotEmpty(bpmMessage.getSubModule())) {
                targetHandler = subModuleHandlerMap.get(bpmMessage.getSubModule());
            }

            // 3. 如果还没找到，尝试找第一个可用的处理器 (不再调用canHandle)
            if (targetHandler == null && !handlers.isEmpty()) {
                targetHandler = handlers.get(0);
            }

            // 创建消息处理上下文
            BpmMessageContext context = new BpmMessageContext(channel, deliveryTag, message);

            // 处理消息
            if (targetHandler != null) {
                log.info("使用处理器 {} 处理BPM消息", targetHandler.getClass().getSimpleName());
                try {
                    // 传入消息处理上下文
                    targetHandler.handleMessage(bpmMessage, context);

                    // 如果业务代码没有确认，则这里自动确认
                    if (!context.isAcknowledged()) {
                        channel.basicAck(deliveryTag, false);
                        log.debug("消息处理成功，已自动确认: {}", deliveryTag);
                    }
                } catch (Exception e) {
                    log.error("处理BPM消息失败", e);
                    // 如果业务代码没有确认，则自动拒绝消息并重新入队
                    if (!context.isAcknowledged()) {
                        boolean requeue = config.isRequeueOnFail();
                        channel.basicReject(deliveryTag, requeue);
                        log.warn("消息处理失败，已拒绝并{}重新入队: {}", requeue ? "" : "不", deliveryTag);
                    }
                }
            } else {
                // 没有处理器，确认消息
                log.warn("未找到处理器处理消息: {}", bpmMessage);
                channel.basicAck(deliveryTag, false);
            }

            // // 如果处理消息成功，手动确认
            // channel.basicAck(deliveryTag, false);
            log.info("消息处理成功，已确认: deliveryTag={}", deliveryTag);
        } catch (Exception e) {
            log.error("处理消息异常: deliveryTag={}, 错误={}", deliveryTag, e.getMessage(), e);

            // 记录异常后，仍确保发送响应，避免连接挂起
            try {
                // 增加重试次数
                attemptCount++;

                if (config.isRequeueOnFail() && attemptCount < config.getMaxRetries()) {
                    // 在重新入队前设置重试次数
                    message.getMessageProperties().getHeaders().put("x-retry-count", attemptCount);

                    channel.basicNack(deliveryTag, false, true);
                    log.warn("消息处理失败，重新入队: deliveryTag={}, 尝试次数={}/{}", deliveryTag, attemptCount, config.getMaxRetries());
                } else {
                    channel.basicNack(deliveryTag, false, false);
                    log.warn("消息处理失败，丢弃: deliveryTag={}, 尝试次数={}/{}", deliveryTag, attemptCount, config.getMaxRetries());
                }
            } catch (Exception ackEx) {
                log.error("消息确认失败: {}", ackEx.getMessage(), ackEx);
            }

            // 重新抛出异常，让容器处理
            throw e;
        }
    }

    /**
     * 设置消息转换器
     */
    public void setMessageConverter(MessageConverter converter) {
        this.converter = converter;
    }
}