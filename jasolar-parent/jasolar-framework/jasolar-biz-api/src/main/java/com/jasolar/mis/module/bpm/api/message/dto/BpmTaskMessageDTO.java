package com.jasolar.mis.module.bpm.api.message.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * BPM任务消息DTO
 * 用于任务级别的事件消息，如任务分配、完成等
 */
@SuppressWarnings("serial")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
public class BpmTaskMessageDTO extends BaseBpmMessageDTO {

    /**
     * 任务ID
     */
    private String taskId;

    /**
     * 任务定义Key
     */
    private String taskDefinitionKey;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 当前任务处理人工号
     */
    private String currentTaskAssigneeUserId;

    /**
     * 当前任务处理人工号
     */
    private String currentTaskAssigneeUserNo;

    /**
     * 当前任务处理人昵称
     */
    private String currentTaskAssigneeUserNick;

    /** 被委托ID */
    private Long delegateeId;
    /** 被委托人工号 */
    private String delegateeNo;
    /** 被委托人名称 */
    private String delegateeName;

    /**
     * 审批意见
     */
    private String comment;

    /**
     * 是否审批通过（任务审批情况）
     */
    private Boolean approved;

}