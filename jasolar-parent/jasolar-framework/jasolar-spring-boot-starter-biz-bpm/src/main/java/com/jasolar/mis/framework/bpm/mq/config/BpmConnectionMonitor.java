package com.jasolar.mis.framework.bpm.mq.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(prefix = "jasolar.bpm", name = "consumer-enabled", matchIfMissing = true)
public class BpmConnectionMonitor {

    private final List<SimpleMessageListenerContainer> containers;
    
    @Scheduled(fixedDelay = 60000) // 每分钟检查一次
    public void monitorContainers() {
        log.info("检查BPM消息监听容器状态...");
        
        for (SimpleMessageListenerContainer container : containers) {
            boolean running = container.isRunning();
            boolean active = container.isActive();
            int consumerCount = container.getActiveConsumerCount();
            
            log.info("监听容器状态: queue={}, running={}, active={}, consumers={}",
                    String.join(",", container.getQueueNames()),
                    running, active, consumerCount);
        }
    }
} 