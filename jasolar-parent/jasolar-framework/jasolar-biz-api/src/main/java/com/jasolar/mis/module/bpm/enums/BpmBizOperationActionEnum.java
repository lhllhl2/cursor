package com.jasolar.mis.module.bpm.enums;

/**
 * 审批历史中的签核动作, 参加字典: bpm_biz_operation_action
 * 
 * @author galuo
 * @date 2025-06-10 16:49
 *
 */
public enum BpmBizOperationActionEnum {

    /** 申请人提交 */
    SUBMIT,
    /** 申请人撤回 */
    RECALL,
    /** 批准 */
    APPROVAL,
    /** 驳回 */
    REJECT,
    /** 退回 */
    RETURN,
    /** 转办 */
    TRANSFER,
    /** 加签 */
    ADD_SIGN,
    /** 减签 */
    DELETE_SIGN;

}
