package com.jasolar.mis.module.system.service.budget.application;

import com.jasolar.mis.module.system.controller.budget.vo.BudgetApplicationParams;
import com.jasolar.mis.module.system.controller.budget.vo.BudgetApplicationRespVo;
import com.jasolar.mis.module.system.controller.budget.vo.BudgetRenewParams;
import com.jasolar.mis.module.system.controller.budget.vo.BudgetRenewRespVo;

/**
 * 预算申请 Service
 */
public interface BudgetApplicationService {

    /**
     * 处理预算申请
     */
    BudgetApplicationRespVo apply(BudgetApplicationParams budgetApplicationParams);

    /**
     * 处理预算申请审批/驳回/撤销
     */
    BudgetRenewRespVo renew(BudgetRenewParams budgetRenewParams);
}

