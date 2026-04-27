package com.jasolar.mis.framework.bpm.mq.publisher;

import org.springframework.context.ApplicationEvent;

import com.jasolar.mis.module.bpm.api.message.dto.BaseBpmMessageDTO;

import lombok.Getter;

/**
 * 流程MQ消息发布. 用于在事务提交后再发送MQ消息. 暂时不需要使用
 * 
 * @author galuo
 * @date 2025-05-20 18:46
 *
 */
@SuppressWarnings("serial")
@Getter
public class BpmMessageEvent extends ApplicationEvent {

    private final String exchange;

    private final String routingKey;

    public BpmMessageEvent(BaseBpmMessageDTO source, String exchange, String routingKey) {
        super(source);
        this.exchange = exchange;
        this.routingKey = routingKey;
    }

    @Override
    public BaseBpmMessageDTO getSource() {
        return (BaseBpmMessageDTO) super.getSource();
    }

}
