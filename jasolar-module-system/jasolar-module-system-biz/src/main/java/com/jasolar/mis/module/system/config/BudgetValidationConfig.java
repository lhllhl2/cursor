package com.jasolar.mis.module.system.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * 预算校验开关配置
 * 用于控制申请/审批接口是否执行预算余额校验。
 * <ul>
 *   <li>budgetValidationMode = 0：不走预算校验，直接扣减/占预算</li>
 *   <li>其它值或空：保持原有预算校验逻辑</li>
 * </ul>
 *
 * @author jasolar
 */
@Configuration
public class BudgetValidationConfig {

    /**
     * 预算校验模式
     * 0 = 跳过预算校验，直接扣减/占预算；其它值或未配置 = 执行预算校验
     */
    @Value("${budget.validation.mode:1}")
    private Integer budgetValidationMode;

    /**
     * 是否跳过预算校验（仅当配置为 0 时跳过）
     *
     * @return true 表示跳过预算校验、直接扣减/占预算；false 表示执行原有预算校验逻辑
     */
    public boolean isSkipBudgetValidation() {
        return budgetValidationMode != null && budgetValidationMode == 0;
    }

    public Integer getBudgetValidationMode() {
        return budgetValidationMode;
    }

    public void setBudgetValidationMode(Integer budgetValidationMode) {
        this.budgetValidationMode = budgetValidationMode;
    }
}
