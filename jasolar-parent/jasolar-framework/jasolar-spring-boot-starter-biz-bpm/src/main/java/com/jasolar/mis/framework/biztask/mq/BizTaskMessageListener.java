package com.jasolar.mis.framework.biztask.mq;

import java.util.function.Function;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.amqp.support.converter.MessageConverter;

import com.rabbitmq.client.Channel;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class BizTaskMessageListener implements ChannelAwareMessageListener {

    /** 消费消息的函数 */
    private final Function<BizTaskMessage, Boolean> consumer;

    private final MessageConverter messageConverter;

    @Override
    public void onMessage(Message message, Channel channel) throws Exception {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        Object msg = messageConverter.fromMessage(message);
        if (!(msg instanceof BizTaskMessage)) {
            log.warn("消息格式有误,不能转换为: {}", BizTaskMessage.class.getName());
            channel.basicReject(deliveryTag, false);
            return;
        }
        BizTaskMessage bizTaskMessage = (BizTaskMessage) msg;
        
        consumer.apply(bizTaskMessage);

    }

}
