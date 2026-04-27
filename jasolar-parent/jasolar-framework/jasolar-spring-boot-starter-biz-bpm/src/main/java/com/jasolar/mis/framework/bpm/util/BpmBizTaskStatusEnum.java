package com.jasolar.mis.framework.bpm.util;

/**
 * BPM审批任务表状态, 对应字典bpm_biz_task_status.
 * 
 * @author galuo
 * @date 2025-03-25 18:28
 *
 */
public enum BpmBizTaskStatusEnum {

    /** 待办 */
    PENDING,

    /** 审批通过 */
    APPROVED,

    /** 审批拒绝 */
    REJECTED,

    /** 审批退回, 审批人退回到指定的审批节点, 从被退回的审批人开始重新走审批流 */
    RETURNED,

    /** 取消,此任务处理人无需处理 */
    CANCELLED,

    /** 任务挂起, 主要用于审批退回到申请人时, 将非退回审批人之外的其他人的任务挂起, 申请人提交后重新提交到退回前的审批人 */
    SUSPENDED

}
