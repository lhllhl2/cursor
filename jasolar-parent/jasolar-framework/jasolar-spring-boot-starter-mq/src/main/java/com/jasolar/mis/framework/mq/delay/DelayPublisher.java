package com.jasolar.mis.framework.mq.delay;

import org.springframework.amqp.rabbit.core.RabbitTemplate;

import com.jasolar.mis.framework.mq.delay.config.DelayProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 消息发布者
 * 
 * @author galuo
 * @date 2025-04-14 15:32
 *
 */
@Slf4j
@RequiredArgsConstructor
public class DelayPublisher {

    private final RabbitTemplate rabbitTemplate;

    private final DelayProperties props;

    /**
     * 创建延时消息
     * 
     * @param message 消息
     * @param delayTimes 延时毫秒
     */
    public void create(DelayMessage message, long delayTimes) {
        try {
            rabbitTemplate.convertAndSend(props.getExchange(), message.getRoutingKey(), message, msg -> {
                // 给消息设置延迟毫秒值
                msg.getMessageProperties().setDelayLong(delayTimes);
                return msg;
            });
            log.info("发送创建延时消息成功: exchange={}, routingKey={}, message={}", props.getExchange(), message.getRoutingKey(), message);
        } catch (Exception e) {
            log.error("发送创建延时消息失败: " + message, e);
            throw e;
        }
    }

}
