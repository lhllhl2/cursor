package com.jasolar.mis.module.system.service.budget.data;

/**
 * 预算数据 Service 接口
 *
 * @author Auto Generated
 * @date 2025-01-XX
 */
public interface BudgetDataService {

    /**
     * 模拟生成预算数据
     * 
     * @return 生成结果
     */
    String simulateBudgetData();

    /**
     * 模拟生成预算数据（不包含项目维度）
     * 
     * @return 生成结果
     */
    String simulateBudgetDataWithoutProject();

    /**
     * 模拟生成资金类型预算数据
     * 
     * @return 生成结果
     */
    String simulateCapitalTypeBudgetData();

    /**
     * 同步项目预算数据
     * 从 DATAINTEGRATION.VIEW_BUDGET_TO_CONTROL 视图同步到 JASOLAR_BUDGET.SYSTEM_PROJECT_BUDGET 表
     * 
     * @param year 年份
     * @return 同步结果
     */
    String syncProjectBudgetNew(String year);
}

