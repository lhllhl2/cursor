package com.jasolar.mis.module.bpm.api.message.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * BPM流程消息DTO
 * 用于流程级别的事件消息，如流程启动、结束等
 */
@SuppressWarnings("serial")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
public class BpmProcessMessageDTO extends BaseBpmMessageDTO {
    
    
    /**
     * 是否审批通过
     */
    private Boolean approved;
    
    /**
     * 流程拒绝原因或返回消息
     */
    private String returnMessage;
} 