package com.jasolar.mis.module.system.service.budget.adjust;

import com.jasolar.mis.module.system.controller.budget.vo.BudgetAdjustApplyParams;
import com.jasolar.mis.module.system.controller.budget.vo.BudgetAdjustRenewParams;
import com.jasolar.mis.module.system.controller.budget.vo.BudgetAdjustRespVo;
import com.jasolar.mis.module.system.controller.budget.vo.BudgetAdjustRenewRespVo;

/**
 * 预算调整 Service
 * 
 * @author jasolar
 */
public interface BudgetAdjustService {

    /**
     * 处理预算调整申请
     * 
     * @param budgetAdjustApplyParams 预算调整申请参数
     * @return 预算调整响应
     */
    BudgetAdjustRespVo apply(BudgetAdjustApplyParams budgetAdjustApplyParams);

    /**
     * 处理预算调整审批/撤回
     * 
     * @param budgetAdjustRenewParams 预算调整审批/撤回参数
     * @return 预算调整审批/撤回响应
     */
    BudgetAdjustRenewRespVo authOrCancel(BudgetAdjustRenewParams budgetAdjustRenewParams);
}

