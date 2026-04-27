package com.jasolar.mis.module.system.domain.budget;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 预算季度明细视图实体
 * 对应视图：V_BUDGET_QUARTERLY_DETAIL
 * 说明：预算季度明细视图（不聚合组织和科目维度），保持原始的组织和科目
 *
 * @author Auto
 */
@Data
public class BudgetQuarterlyDetail {

    /**
     * 年度
     */
    private String year;

    /**
     * 组织编码（原始组织，不聚合）
     */
    private String morgCode;

    /**
     * 组织名称
     */
    private String morgName;

    /**
     * CUST1编码（原始CUST1，不聚合）
     */
    private String cust1Cd;

    /**
     * CUST1名称
     */
    private String cust1Name;

    /**
     * 科目编码（原始科目，不聚合）
     */
    private String accountSubjectCode;

    /**
     * 科目名称
     */
    private String accountSubjectName;

    // ========== Q1季度数据 ==========

    /**
     * Q1季度：年度预算数
     */
    private BigDecimal q1AmountTotal;

    /**
     * Q1季度：预算调整数
     */
    private BigDecimal q1AmountAdj;

    /**
     * Q1季度：总预算数（年度预算数 + 预算调整数）
     */
    private BigDecimal q1TotalBudget;

    /**
     * Q1季度：冻结金额
     */
    private BigDecimal q1AmountFrozen;

    /**
     * Q1季度：占用金额
     */
    private BigDecimal q1AmountOccupied;

    /**
     * Q1季度：实际金额
     */
    private BigDecimal q1AmountActual;

    /**
     * Q1季度：已批准实际金额（从BUDGET_LEDGER获取，状态为APPROVED或APPROVED_UPDATE的CLAIM类型数据）
     */
    private BigDecimal q1AmountActualApproved;

    /**
     * Q1季度：可用金额
     */
    private BigDecimal q1AmountAvailable;

    // ========== Q2季度数据 ==========

    /**
     * Q2季度：年度预算数（累积：Q1+Q2）
     */
    private BigDecimal q2AmountTotal;

    /**
     * Q2季度：预算调整数（累积：Q1+Q2）
     */
    private BigDecimal q2AmountAdj;

    /**
     * Q2季度：总预算数（累积：Q1+Q2）
     */
    private BigDecimal q2TotalBudget;

    /**
     * Q2季度：冻结金额（累积：Q1+Q2）
     */
    private BigDecimal q2AmountFrozen;

    /**
     * Q2季度：占用金额（累积：Q1+Q2）
     */
    private BigDecimal q2AmountOccupied;

    /**
     * Q2季度：实际金额（累积：Q1+Q2）
     */
    private BigDecimal q2AmountActual;

    /**
     * Q2季度：已批准实际金额（从BUDGET_LEDGER获取，状态为APPROVED或APPROVED_UPDATE的CLAIM类型数据，累积1-6月）
     */
    private BigDecimal q2AmountActualApproved;

    /**
     * Q2季度：可用金额（累积：Q1+Q2）
     */
    private BigDecimal q2AmountAvailable;

    // ========== Q3季度数据 ==========

    /**
     * Q3季度：年度预算数（累积：Q1+Q2+Q3）
     */
    private BigDecimal q3AmountTotal;

    /**
     * Q3季度：预算调整数（累积：Q1+Q2+Q3）
     */
    private BigDecimal q3AmountAdj;

    /**
     * Q3季度：总预算数（累积：Q1+Q2+Q3）
     */
    private BigDecimal q3TotalBudget;

    /**
     * Q3季度：冻结金额（累积：Q1+Q2+Q3）
     */
    private BigDecimal q3AmountFrozen;

    /**
     * Q3季度：占用金额（累积：Q1+Q2+Q3）
     */
    private BigDecimal q3AmountOccupied;

    /**
     * Q3季度：实际金额（累积：Q1+Q2+Q3）
     */
    private BigDecimal q3AmountActual;

    /**
     * Q3季度：已批准实际金额（从BUDGET_LEDGER获取，状态为APPROVED或APPROVED_UPDATE的CLAIM类型数据，累积1-9月）
     */
    private BigDecimal q3AmountActualApproved;

    /**
     * Q3季度：可用金额（累积：Q1+Q2+Q3）
     */
    private BigDecimal q3AmountAvailable;

    // ========== Q4季度数据 ==========

    /**
     * Q4季度：年度预算数（累积：Q1+Q2+Q3+Q4）
     */
    private BigDecimal q4AmountTotal;

    /**
     * Q4季度：预算调整数（累积：Q1+Q2+Q3+Q4）
     */
    private BigDecimal q4AmountAdj;

    /**
     * Q4季度：总预算数（累积：Q1+Q2+Q3+Q4）
     */
    private BigDecimal q4TotalBudget;

    /**
     * Q4季度：冻结金额（累积：Q1+Q2+Q3+Q4）
     */
    private BigDecimal q4AmountFrozen;

    /**
     * Q4季度：占用金额（累积：Q1+Q2+Q3+Q4）
     */
    private BigDecimal q4AmountOccupied;

    /**
     * Q4季度：实际金额（累积：Q1+Q2+Q3+Q4）
     */
    private BigDecimal q4AmountActual;

    /**
     * Q4季度：已批准实际金额（从BUDGET_LEDGER获取，状态为APPROVED或APPROVED_UPDATE的CLAIM类型数据，累积1-12月）
     */
    private BigDecimal q4AmountActualApproved;

    /**
     * Q4季度：可用金额（累积：Q1+Q2+Q3+Q4）
     */
    private BigDecimal q4AmountAvailable;
}
