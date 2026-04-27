package com.jasolar.mis.framework.bpm.mq.publisher;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import com.jasolar.mis.framework.bpm.autoconfigure.BpmProperties;
import com.jasolar.mis.module.bpm.api.message.dto.BaseBpmMessageDTO;
import com.jasolar.mis.module.bpm.api.message.dto.BpmTaskMessageDTO;

import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * BPM消息发布服务
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BpmMessagePublisher {

    private final RabbitTemplate rabbitTemplate;

    private final BpmProperties properties;

    // @Setter
    // private ApplicationEventPublisher applicationEventPublisher;

    /**
     * 发布BPM消息
     */
    public void publishMessage(BaseBpmMessageDTO message) {
        // 验证消息
        if (!validateMessage(message)) {
            log.error("BPM消息验证失败，无法发送: {}", message);
            return;
        }

        // 使用配置类中的方法生成一致的路由键
        String routingKey;
        if (StrUtil.isEmpty(message.getSubModule())) {
            // 只发送到模块，移除末尾的通配符
            routingKey = getRoutingKeyModule(properties, message.getModule().getName());
            routingKey = routingKey.substring(0, routingKey.length() - 2); // 移除 ".#"
        } else {
            // 发送到特定子模块
            routingKey = getRoutingKeySubmodule(properties, message.getModule().getName(), message.getSubModule());
        }

        // 发送消息, 需要再事务提交后使用回调进行发送
        try {
            rabbitTemplate.convertAndSend(properties.getExchangeName(), routingKey, message);
            log.info("发送BPM消息成功: routingKey={}, message={}", routingKey, message);
        } catch (Exception e) {
            log.error("发送BPM消息失败", e);
            throw e;
        }

        // applicationEventPublisher.publishEvent(new BpmMessageEvent(properties.getExchangeName(), routingKey, message));
    }

    /**
     * 消息内容必要字段校验
     *
     * @param message 消息内容
     * @return true: 校验通过 false: 校验不通过
     */
    protected boolean validateMessage(BaseBpmMessageDTO message) {
        if (message == null) {
            log.error("消息为空");
            return false;
        }
        if (message.getModule() == null) {
            log.error("消息目标服务为空");
            return false;
        }
        if (StrUtil.isEmpty(message.getBusinessKey())) {
            log.error("业务的唯一标识不能为空");
            return false;
        }
        if (StrUtil.isEmpty(message.getProcessInstanceId())) {
            log.error("消息流程实例ID为空");
            return false;
        }
        if (StrUtil.isEmpty(message.getProcessDefinitionKey())) {
            log.error("消息流程定义Key为空");
            return false;
        }
        if (StrUtil.isEmpty(message.getStartUserNo())) {
            log.error("消息发起人工号为空");
            return false;
        }
        if (StrUtil.isEmpty(message.getStartUserNick())) {
            log.error("消息发起人昵称为空");
            return false;
        }

        // 针对任务消息的特定校验
        if (message instanceof BpmTaskMessageDTO taskMessage) {
            if (StrUtil.isEmpty(taskMessage.getTaskId())) {
                log.error("任务消息的任务ID为空");
                return false;
            }
            if (StrUtil.isEmpty(taskMessage.getTaskName())) {
                log.error("任务消息的任务名称为空");
                return false;
            }
            if (StrUtil.isEmpty(taskMessage.getTaskDefinitionKey())) {
                log.error("任务消息的任务定义Key为空");
                return false;
            }
        }

        return true;
    }

    // 添加引用BpmRabbitMQConfig中的方法
    private String getRoutingKeyModule(BpmProperties properties, String module) {
        return properties.getRoutingKeyPrefix() + "." + module + ".#";
    }

    private String getRoutingKeySubmodule(BpmProperties properties, String module, String subModule) {
        return properties.getRoutingKeyPrefix() + "." + module + "." + subModule;
    }
}