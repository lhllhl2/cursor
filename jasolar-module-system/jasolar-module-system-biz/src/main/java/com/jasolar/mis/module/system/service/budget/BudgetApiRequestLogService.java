package com.jasolar.mis.module.system.service.budget;

import com.jasolar.mis.module.system.domain.budget.BudgetApiRequestLog;

/**
 * 预算接口请求报文记录服务接口
 */
public interface BudgetApiRequestLogService {

    /**
     * 记录接口请求报文
     *
     * @param apiRequestLog 请求日志实体
     */
    void recordRequestLog(BudgetApiRequestLog apiRequestLog);
}

