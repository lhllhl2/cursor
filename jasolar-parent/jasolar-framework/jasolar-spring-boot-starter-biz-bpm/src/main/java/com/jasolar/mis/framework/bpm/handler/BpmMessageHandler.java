package com.jasolar.mis.framework.bpm.handler;

import java.io.IOException;

import org.springframework.transaction.annotation.Transactional;

import com.jasolar.mis.framework.bpm.mq.context.BpmMessageContext;
import com.jasolar.mis.module.bpm.api.message.dto.BaseBpmMessageDTO;

/**
 * BPM消息处理器接口
 * 各个服务需要实现该接口来处理接收到的BPM消息
 */
public interface BpmMessageHandler {

    /**
     * 处理BPM消息(自动ACK)
     * 
     * @param message BPM消息
     */
    @Transactional(rollbackFor = Exception.class)
    void handleMessage(BaseBpmMessageDTO message);

    /**
     * 处理BPM消息（新方法，支持消息确认）
     * 
     * @param message BPM消息
     * @param context 消息处理上下文，用于消息确认
     */
    @Transactional(rollbackFor = Exception.class)
    default void handleMessage(BaseBpmMessageDTO message, BpmMessageContext context) throws IOException {
        // 默认调用旧方法，实现向后兼容
        handleMessage(message);

        // 成功处理后自动确认
        context.ack();
    }
}