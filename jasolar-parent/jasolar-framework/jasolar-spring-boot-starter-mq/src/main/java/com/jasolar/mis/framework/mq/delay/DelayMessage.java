package com.jasolar.mis.framework.mq.delay;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Data;

/**
 * 基础待办消息
 * 
 * @author galuo
 * @date 2025-04-14 13:47
 *
 */
@Data
@SuppressWarnings("serial")
public class DelayMessage implements Serializable {

    /** 业务id */
    private Long bizId;

    /** 业务编号 */
    private String bizNo;

    /** 业务类型 */
    private String bizType;

    /** RoutingKey */
    private String routingKey;

    /** 延迟时间 */
    private LocalDateTime delayTime;
}
