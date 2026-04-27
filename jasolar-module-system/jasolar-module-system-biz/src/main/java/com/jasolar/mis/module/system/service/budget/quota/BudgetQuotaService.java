package com.jasolar.mis.module.system.service.budget.quota;

/**
 * 预算额度 Service
 */
public interface BudgetQuotaService {

    /**
     * 将 SYSTEM_PROJECT_BUDGET 数据同步到 BUDGET_QUOTA
     *
     * @param year 预算年度
     * @return 同步结果描述
     */
    String syncQuotaDataFromOriginal(String year);

    /**
     * 补足 BUDGET_POOL_DEM_R 的 project_id
     * 按 project_code（master_project_code）和 morgCode 匹配 SYSTEM_PROJECT_BUDGET，只更新 project_id，其它不做
     * 用于预算调整单等创建的 pool 未设置 project_id 的场景
     *
     * @param year 预算年度
     * @return 补足结果描述
     */
    String complementProjectId(String year);
}

