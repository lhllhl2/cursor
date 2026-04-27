package com.jasolar.mis.framework.biztask.mq;

import java.time.LocalDateTime;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.validation.annotation.Validated;

import com.jasolar.mis.framework.biztask.config.BizTaskProperties;
import com.jasolar.mis.framework.common.validation.group.Submit;
import com.jasolar.mis.module.infra.api.biztask.BizTaskStatusEnum;

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
public class BizTaskPublisher {

    private final RabbitTemplate rabbitTemplate;

    private final BizTaskProperties props;

    /**
     * 创建待办
     * 
     * @param bizTask
     */
    public void create(@Validated(Submit.class) BizTaskMessage bizTask) {
        try {
            bizTask.setStatus(BizTaskStatusEnum.PENDING.name());
            bizTask.setSubmitTime(LocalDateTime.now());
            rabbitTemplate.convertAndSend(props.getExchange(), props.getPending().getRoutingKey(), bizTask);
            log.info("发送创建业务待办消息成功: exchange={}, routingKey={}, message={}", props.getExchange(), props.getPending().getRoutingKey(),
                    bizTask);
        } catch (Exception e) {
            log.error("发送创建业务待办消息失败: " + bizTask, e);
            throw e;
        }
    }

    /**
     * 完成待办
     * 
     * @param bizTask
     */
    public void complete(@Validated BizTaskMessage bizTask) {
        try {
            bizTask.setStatus(BizTaskStatusEnum.COMPLETED.name());
            bizTask.setCompleteTime(LocalDateTime.now());
            rabbitTemplate.convertAndSend(props.getExchange(), props.getCompleted().getRoutingKey(), bizTask);
            log.info("发送完成业务待办消息成功: exchange={}, routingKey={}, message={}", props.getExchange(), props.getCompleted().getRoutingKey(),
                    bizTask);
        } catch (Exception e) {
            log.error("发送完成业务待办消息失败: " + bizTask, e);
            throw e;
        }
    }

    /**
     * 删除待办
     * 
     * @param bizTask
     */
    public void delete(@Validated BizTaskMessage bizTask) {
        try {
            bizTask.setCompleteTime(LocalDateTime.now());
            rabbitTemplate.convertAndSend(props.getExchange(), props.getDeleted().getRoutingKey(), bizTask);
            log.info("发送删除业务待办消息成功: exchange={}, routingKey={}, message={}", props.getExchange(), props.getDeleted().getRoutingKey(),
                    bizTask);
        } catch (Exception e) {
            log.error("发送删除业务待办消息失败: " + bizTask, e);
            throw e;
        }
    }
}
