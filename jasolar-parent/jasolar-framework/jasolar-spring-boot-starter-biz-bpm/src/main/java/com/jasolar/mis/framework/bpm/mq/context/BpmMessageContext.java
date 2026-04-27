package com.jasolar.mis.framework.bpm.mq.context;

import com.rabbitmq.client.Channel;
import lombok.Getter;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

import java.io.IOException;

/**
 * BPM消息处理上下文
 * 提供消息确认和拒绝的能力
 */
@Getter
public class BpmMessageContext {
    private final Channel channel;
    private final long deliveryTag;
    private final Message message;
    private boolean acknowledged = false;
    
    public BpmMessageContext(Channel channel, long deliveryTag, Message message) {
        this.channel = channel;
        this.deliveryTag = deliveryTag;
        this.message = message;
    }
    
    /**
     * 确认消息已处理成功
     */
    public void ack() throws IOException {
        if (!acknowledged) {
            channel.basicAck(deliveryTag, false);
            acknowledged = true;
        }
    }
    
    /**
     * 拒绝消息并决定是否重新入队
     * @param requeue 是否重新入队
     */
    public void reject(boolean requeue) throws IOException {
        if (!acknowledged) {
            channel.basicReject(deliveryTag, requeue);
            acknowledged = true;
        }
    }
    
    /**
     * 获取消息属性
     */
    public MessageProperties getMessageProperties() {
        return message.getMessageProperties();
    }
    
    /**
     * 获取消息是否已被确认或拒绝
     */
    public boolean isAcknowledged() {
        return acknowledged;
    }
} 