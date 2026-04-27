package com.jasolar.mis.module.bpm.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 流程操作枚举
 */
@Getter
@AllArgsConstructor
public enum BpmActionEnum {

    /** 新的任务并分配了处理人 */
    TASK_ASSIGN("TASK_ASSIGN", "任务分配"),
    /** 任务审批通过 */
    TASK_APPROVE("TASK_APPROVE", "任务通过"),

    /** 审批拒绝 */
    TASK_REJECT("TASK_REJECT", "任务拒绝"),
    // TASK_DELEGATE("TASK_DELEGATE", "任务委派"),

    // /** 处理人将自己的任务转派给其他人员,此时不会增加任务,ACT中直接修改了任务的处理人. 因此回调时也仅进行任务修改 */
    // TASK_TRANSFER("TASK_TRANSFER", "任务转派"),

    /** 审批人将任务退回给申请人, 申请人处理后回到审批人 */
    TASK_RETURN("TASK_RETURN", "任务退回"),

    /** 适用于或签的任务,当其中一个人处理后,其他处理人的任务需要取消 */
    TASK_CANCEL("TASK_CANCEL", "任务取消"),
    // TASK_ADD_SIGN("TASK_ADD_SIGN", "任务加签"),
    // TASK_DELETE_SIGN("TASK_DELETE_SIGN", "任务减签"),

    // 流程相关

    /** 新建流程时回调 */
    PROCESS_START("PROCESS_START", "流程发起"),

    /** 流程审批通过时回调 */
    PROCESS_APPROVED("PROCESS_APPROVED", "流程通过"),

    /** 流程审批拒绝时回调 */
    PROCESS_REJECT("PROCESS_REJECT", "流程拒绝"),

    /** 审批人将任务退回给申请人,此时流程暂停 */
    PROCESS_RETURN("PROCESS_RETURN", "流程退回"),

    /** 申请人进行流程撤回,将流程修改为草稿状态 */
    PROCESS_CANCEL("PROCESS_CANCEL", "流程撤回");

    /**
     * 操作标识
     */
    private final String action;

    /**
     * 操作描述
     */
    private final String desc;

    public Boolean is(BpmActionEnum action) {
        return action != null && this.action.equals(action.action);
    }

}