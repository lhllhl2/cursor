package com.jasolar.mis.module.system.domain.budget;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 预算项目季度聚合视图实体（按投资额/付款额分类）
 * 对应视图：V_BUDGET_PROJECT_QUARTERLY_AGGREGATE
 * 说明：按投资额和付款额分类展示预算项目季度聚合数据，按YEAR、EHR_PRJ_CD、MORG_CODE聚合
 * 投资额：Q1-Q4四个季度的金额聚合相加
 * 付款额：每个季度分开展示（Q1、Q2、Q3、Q4）
 *
 * @author Auto
 */
@Data
public class BudgetProjectQuarterlyAggregate {

    /**
     * 年度
     */
    private String year;

    /**
     * EHR组织编码@项目编码（控制层级）
     */
    private String ehrPrjCd;

    /**
     * 组织编码（来自BUDGET_QUOTA.MORG_CODE）
     */
    private String morgCode;

    /**
     * 组织名称（通过MORG_CODE关联EHR_ORG_MANAGE_R.ORG_CD获取）
     */
    private String morgName;

    /**
     * 项目编码（控制层级）
     */
    private String prjCd;

    /**
     * 项目名称
     */
    private String prjName;

    // ========== 投资额数据（Q1-Q4聚合相加） ==========

    /**
     * 投资额：去年使用预算数（去年同一维度的 INVESTMENT_AMOUNT_FROZEN + INVESTMENT_AMOUNT_OCCUPIED + INVESTMENT_AMOUNT_ACTUAL）
     */
    private BigDecimal investmentLastYearUsed;

    /**
     * 投资额：年度预算数（Q1-Q4聚合）
     */
    private BigDecimal investmentAmountTotal;

    /**
     * 投资额：预算调整数（Q1-Q4聚合）
     */
    private BigDecimal investmentAmountAdj;

    /**
     * 投资额：总预算数（年度预算数 + 预算调整数，Q1-Q4聚合）
     */
    private BigDecimal investmentTotalBudget;

    /**
     * 投资额：冻结金额（Q1-Q4聚合）
     */
    private BigDecimal investmentAmountFrozen;

    /**
     * 投资额：占用金额（Q1-Q4聚合）
     */
    private BigDecimal investmentAmountOccupied;

    /**
     * 投资额：实际金额（Q1-Q4聚合）
     */
    private BigDecimal investmentAmountActual;

    /**
     * 投资额：已批准金额（全年1-12月，来自BUDGET_LEDGER）
     */
    private BigDecimal investmentAmountActualApproved;

    /**
     * 投资额：可用金额（Q1-Q4聚合）
     */
    private BigDecimal investmentAmountAvailable;

    // ========== 付款额 Q1季度数据 ==========

    /**
     * 付款额：去年使用预算数（去年同一维度的 PAYMENT_Q4_AMOUNT_FROZEN + PAYMENT_Q4_AMOUNT_OCCUPIED + PAYMENT_Q4_AMOUNT_ACTUAL）
     */
    private BigDecimal paymentLastYearUsed;

    /**
     * 付款额 Q1季度：年度预算数
     */
    private BigDecimal paymentQ1AmountTotal;

    /**
     * 付款额 Q1季度：预算调整数
     */
    private BigDecimal paymentQ1AmountAdj;

    /**
     * 付款额 Q1季度：总预算数（年度预算数 + 预算调整数）
     */
    private BigDecimal paymentQ1TotalBudget;

    /**
     * 付款额 Q1季度：冻结金额
     */
    private BigDecimal paymentQ1AmountFrozen;

    /**
     * 付款额 Q1季度：占用金额
     */
    private BigDecimal paymentQ1AmountOccupied;

    /**
     * 付款额 Q1季度：实际金额
     */
    private BigDecimal paymentQ1AmountActual;

    /**
     * 付款额 Q1季度：已批准金额（1-3月，来自BUDGET_LEDGER）
     */
    private BigDecimal paymentQ1AmountActualApproved;

    /**
     * 付款额 Q1季度：可用金额
     */
    private BigDecimal paymentQ1AmountAvailable;

    // ========== 付款额 Q2季度数据 ==========

    /**
     * 付款额 Q2季度：年度预算数
     */
    private BigDecimal paymentQ2AmountTotal;

    /**
     * 付款额 Q2季度：预算调整数
     */
    private BigDecimal paymentQ2AmountAdj;

    /**
     * 付款额 Q2季度：总预算数（年度预算数 + 预算调整数）
     */
    private BigDecimal paymentQ2TotalBudget;

    /**
     * 付款额 Q2季度：冻结金额
     */
    private BigDecimal paymentQ2AmountFrozen;

    /**
     * 付款额 Q2季度：占用金额
     */
    private BigDecimal paymentQ2AmountOccupied;

    /**
     * 付款额 Q2季度：实际金额
     */
    private BigDecimal paymentQ2AmountActual;

    /**
     * 付款额 Q2季度：已批准金额（1-6月累积，来自BUDGET_LEDGER）
     */
    private BigDecimal paymentQ2AmountActualApproved;

    /**
     * 付款额 Q2季度：可用金额
     */
    private BigDecimal paymentQ2AmountAvailable;

    // ========== 付款额 Q3季度数据 ==========

    /**
     * 付款额 Q3季度：年度预算数
     */
    private BigDecimal paymentQ3AmountTotal;

    /**
     * 付款额 Q3季度：预算调整数
     */
    private BigDecimal paymentQ3AmountAdj;

    /**
     * 付款额 Q3季度：总预算数（年度预算数 + 预算调整数）
     */
    private BigDecimal paymentQ3TotalBudget;

    /**
     * 付款额 Q3季度：冻结金额
     */
    private BigDecimal paymentQ3AmountFrozen;

    /**
     * 付款额 Q3季度：占用金额
     */
    private BigDecimal paymentQ3AmountOccupied;

    /**
     * 付款额 Q3季度：实际金额
     */
    private BigDecimal paymentQ3AmountActual;

    /**
     * 付款额 Q3季度：已批准金额（1-9月累积，来自BUDGET_LEDGER）
     */
    private BigDecimal paymentQ3AmountActualApproved;

    /**
     * 付款额 Q3季度：可用金额
     */
    private BigDecimal paymentQ3AmountAvailable;

    // ========== 付款额 Q4季度数据 ==========

    /**
     * 付款额 Q4季度：年度预算数
     */
    private BigDecimal paymentQ4AmountTotal;

    /**
     * 付款额 Q4季度：预算调整数
     */
    private BigDecimal paymentQ4AmountAdj;

    /**
     * 付款额 Q4季度：总预算数（年度预算数 + 预算调整数）
     */
    private BigDecimal paymentQ4TotalBudget;

    /**
     * 付款额 Q4季度：冻结金额
     */
    private BigDecimal paymentQ4AmountFrozen;

    /**
     * 付款额 Q4季度：占用金额
     */
    private BigDecimal paymentQ4AmountOccupied;

    /**
     * 付款额 Q4季度：实际金额
     */
    private BigDecimal paymentQ4AmountActual;

    /**
     * 付款额 Q4季度：已批准金额（1-12月累积，来自BUDGET_LEDGER）
     */
    private BigDecimal paymentQ4AmountActualApproved;

    /**
     * 付款额 Q4季度：可用金额
     */
    private BigDecimal paymentQ4AmountAvailable;
}

