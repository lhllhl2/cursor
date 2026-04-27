package com.jasolar.mis.framework.biztask.mq;

import java.io.IOException;
import java.util.function.Function;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.converter.MessageConverter;

import com.jasolar.mis.framework.biztask.BizTaskConsumerService;
import com.jasolar.mis.framework.biztask.config.BizTaskProperties;
import com.rabbitmq.client.Channel;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 业务待办的MQ消费者,在infra中进行实现
 * 
 * @author galuo
 * @date 2025-04-14 16:00
 *
 */
@Slf4j
@RequiredArgsConstructor
public class BizTaskConsumer {

    private final BizTaskConsumerService bizTaskConsumerService;

    private final MessageConverter messageConverter;

    private final BizTaskProperties props;

    /**
     * 
     * 创建待办消息处理
     * 
     * @param message
     * @param channel
     * @throws IOException
     */
    @RabbitListener(queues = "${fiifoxconn.biztask.consumer.queue.pending:" + BizTaskProperties.DEFAULT_QUEUE_PENDING + "}")
    public void assignTask(Message message, Channel channel) throws IOException {
        this.doTask(message, channel, task -> bizTaskConsumerService.assignTask(task));
    }

    /**
     * 完成待办
     * 
     * @param bizTask
     */
    @RabbitListener(queues = "${fiifoxconn.biztask.consumer.queue.completed:" + BizTaskProperties.DEFAULT_QUEUE_COMPLETED + "}")
    public void completeTask(Message message, Channel channel) throws IOException {
        this.doTask(message, channel, task -> bizTaskConsumerService.completeTask(task));
    }

    /**
     * 删除待办
     * 
     * @param bizTask
     */
    @RabbitListener(queues = "${fiifoxconn.biztask.consumer.queue.deleted:" + BizTaskProperties.DEFAULT_QUEUE_DELETED + "}")
    public void deleteTask(Message message, Channel channel) throws IOException {
        this.doTask(message, channel, task -> bizTaskConsumerService.deleteTask(task));
    }

    /**
     * 处理消息
     * 
     * @param message
     * @param channel
     * @param consumer
     * @throws IOException
     */
    protected void doTask(Message message, Channel channel, Function<BizTaskMessage, Boolean> consumer) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        Object msg = messageConverter.fromMessage(message);
        if (!(msg instanceof BizTaskMessage)) {
            log.warn("接收到的业务待办消息有误,不能转换为BizTaskMessage");
            channel.basicReject(deliveryTag, false);
            return;
        }

        boolean requeue = props.getConsumer().isRequeue();
        BizTaskMessage bizTaskMessage = (BizTaskMessage) msg;
        try {
            boolean success = consumer.apply(bizTaskMessage);
            if (!success) {
                log.warn("业务待办消息处理失败，已拒绝{}: {}", (requeue ? "并重新入队" : ""), deliveryTag);
                channel.basicReject(deliveryTag, requeue);
            } else {
                channel.basicAck(deliveryTag, false);
            }
        } catch (Exception ex) {
            log.error("业务待办消息处理异常，已拒绝" + (requeue ? "并重新入队" : "") + ":" + deliveryTag, ex);
            channel.basicReject(deliveryTag, requeue);
        }
    }

}
