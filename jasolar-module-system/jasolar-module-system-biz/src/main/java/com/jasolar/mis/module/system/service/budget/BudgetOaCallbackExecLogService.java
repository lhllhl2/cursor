package com.jasolar.mis.module.system.service.budget;

import com.jasolar.mis.module.system.domain.budget.BudgetOaCallbackExecLog;

/**
 * OA回调执行日志服务
 */
public interface BudgetOaCallbackExecLogService {

    /**
     * 记录OA回调执行日志
     *
     * @param execLog 日志实体
     */
    void record(BudgetOaCallbackExecLog execLog);
}
