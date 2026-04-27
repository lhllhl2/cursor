package com.jasolar.mis.framework.mq.delay;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 业务中使用的Service
 * 
 * @author galuo
 * @date 2025-04-14 18:01
 *
 */
@Slf4j
@RequiredArgsConstructor
public class DelayPublisherService {

    private final DelayPublisher delayPublisher;

    /**
     * 发送延时消息
     * 
     * @param message 消息
     * @param delayTimes 延时毫秒
     */
    public void create(DelayMessage message, long delayTimes) {
        delayPublisher.create(message, delayTimes);
    }

    /**
     * 发送延时消息
     * 
     * @param message 消息
     * @param localDateTime 消息延时到的时间
     */
    public void create(DelayMessage message, LocalDateTime localDateTime) {
        long delayMillis;
        LocalDateTime now = LocalDateTime.now();
        if (localDateTime != null && localDateTime.isAfter(now)) {
            delayMillis = ChronoUnit.MILLIS.between(now, localDateTime);
        } else {
            log.warn("延时时间已过或未设置, 延时时间：{}", localDateTime);
            // 如果报价截止时间已过，设置一个较短的延迟时间（例如1分钟）
            delayMillis = (long)60 * 1000;
        }

        create(message, delayMillis);
    }

}
