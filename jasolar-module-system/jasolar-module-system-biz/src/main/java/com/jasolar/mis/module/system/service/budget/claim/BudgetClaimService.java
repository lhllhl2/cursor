package com.jasolar.mis.module.system.service.budget.claim;

import com.jasolar.mis.module.system.controller.budget.vo.BudgetClaimApplyParams;
import com.jasolar.mis.module.system.controller.budget.vo.BudgetClaimRenewParams;
import com.jasolar.mis.module.system.controller.budget.vo.BudgetClaimRenewRespVo;
import com.jasolar.mis.module.system.controller.budget.vo.BudgetClaimRespVo;

/**
 * 预算付款/报销 Service
 */
public interface BudgetClaimService {

    /**
     * 付款/报销申请
     */
    BudgetClaimRespVo apply(BudgetClaimApplyParams budgetClaimApplyParams);

    /**
     * 付款/报销审批或撤回
     */
    BudgetClaimRenewRespVo authOrCancel(BudgetClaimRenewParams budgetClaimRenewParams);
}

