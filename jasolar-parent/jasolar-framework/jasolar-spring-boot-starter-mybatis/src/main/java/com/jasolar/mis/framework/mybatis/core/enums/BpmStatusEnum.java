package com.jasolar.mis.framework.mybatis.core.enums;

/**
 * 常用的审批状态.对应字典bpm_proc_status. 如果业务中有其他状态, 需要单独定义字典和枚举
 * 
 * @author galuo
 * @date 2025-03-25 18:24
 *
 */
public enum BpmStatusEnum {

    /** 草稿 */
    DRAFT,

    /** 审批中, 审批人退回到某一个节点状态也为审批中 */
    APPROVING,

    /** 审批通过 */
    APPROVED,

    /** 审批退回到申请人, 提交后流程重新回到原退回节点 */
    RETURNED,

    /** 审批拒绝 */
    REJECTED;

}