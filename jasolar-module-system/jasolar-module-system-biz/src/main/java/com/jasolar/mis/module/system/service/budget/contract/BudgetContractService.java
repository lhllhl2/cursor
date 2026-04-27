package com.jasolar.mis.module.system.service.budget.contract;

import com.jasolar.mis.module.system.controller.budget.vo.BudgetContractApplyParams;
import com.jasolar.mis.module.system.controller.budget.vo.BudgetContractRenewParams;
import com.jasolar.mis.module.system.controller.budget.vo.BudgetContractRenewRespVo;
import com.jasolar.mis.module.system.controller.budget.vo.BudgetContractRespVo;

/**
 * 预算合同 Service
 *
 * @author Auto Generated
 * @date 2025-01-XX
 */
public interface BudgetContractService {

    /**
     * 处理采购合同的申请操作
     *
     * @param budgetContractApplyParams 预算合同申请参数
     * @return 预算合同响应
     */
    BudgetContractRespVo apply(BudgetContractApplyParams budgetContractApplyParams);

    /**
     * 处理采购合同的审批及撤回操作
     *
     * @param budgetContractRenewParams 预算合同审批/撤回参数
     * @return 预算合同审批/撤回响应
     */
    BudgetContractRenewRespVo authOrCancel(BudgetContractRenewParams budgetContractRenewParams);
}

