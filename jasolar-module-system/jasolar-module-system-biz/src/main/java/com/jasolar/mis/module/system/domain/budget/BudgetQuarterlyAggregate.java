package com.jasolar.mis.module.system.domain.budget;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 预算季度聚合视图实体
 * 对应视图：V_BUDGET_QUARTERLY_AGGREGATE_OPTIMIZED
 * 说明：将预算数据按年度聚合，将4个季度的数据合并到一条记录，并聚合末级数据到控制层级
 *
 * @author Auto
 */
@Data
public class BudgetQuarterlyAggregate {

    /**
     * 年度
     */
    private String year;

    /**
     * ERP科目编码（格式：EHR_CD@CUST1_CD-ACCT_CD）
     */
    private String ehrErpAcctCd;

    /**
     * 控制层级EHR编码
     */
    private String controlEhrCode;

    /**
     * 控制层级EHR名称
     */
    private String controlEhrName;

    /**
     * 控制层级CUST1编码
     */
    private String controlCust1Cd;

    /**
     * 控制层级CUST1名称
     */
    private String controlCust1Name;

    /**
     * 控制层级科目编码
     */
    private String controlAccountSubjectCode;

    /**
     * 控制层级科目名称
     */
    private String controlAccountSubjectName;

    /**
     * 去年使用预算数（去年同一维度的Q4_AMOUNT_FROZEN+Q4_AMOUNT_OCCUPIED+Q4_AMOUNT_ACTUAL）
     */
    private BigDecimal lastYearUsedBudget;

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
     * Q1季度：已批准实际金额（CLAIM、头表已批准、POOL_DIMENSION_KEY 非空；经组织/科目映射；当季 1-3 月 BUDGET_LEDGER.AMOUNT 汇总）
     */
    private BigDecimal q1AmountActualApproved;

    /**
     * Q1季度：可用金额
     */
    private BigDecimal q1AmountAvailable;

    // ========== Q2季度数据 ==========

    /**
     * Q2季度：年度预算数
     */
    private BigDecimal q2AmountTotal;

    /**
     * Q2季度：预算调整数
     */
    private BigDecimal q2AmountAdj;

    /**
     * Q2季度：总预算数（年度预算数 + 预算调整数）
     */
    private BigDecimal q2TotalBudget;

    /**
     * Q2季度：冻结金额
     */
    private BigDecimal q2AmountFrozen;

    /**
     * Q2季度：占用金额
     */
    private BigDecimal q2AmountOccupied;

    /**
     * Q2季度：实际金额
     */
    private BigDecimal q2AmountActual;

    /**
     * Q2季度：已批准实际（视图中为 Q1+Q2 当季额累加展示，与发生数列展示口径一致）
     */
    private BigDecimal q2AmountActualApproved;

    /**
     * Q2季度：可用金额
     */
    private BigDecimal q2AmountAvailable;

    // ========== Q3季度数据 ==========

    /**
     * Q3季度：年度预算数
     */
    private BigDecimal q3AmountTotal;

    /**
     * Q3季度：预算调整数
     */
    private BigDecimal q3AmountAdj;

    /**
     * Q3季度：总预算数（年度预算数 + 预算调整数）
     */
    private BigDecimal q3TotalBudget;

    /**
     * Q3季度：冻结金额
     */
    private BigDecimal q3AmountFrozen;

    /**
     * Q3季度：占用金额
     */
    private BigDecimal q3AmountOccupied;

    /**
     * Q3季度：实际金额
     */
    private BigDecimal q3AmountActual;

    /**
     * Q3季度：已批准实际（视图中为 Q1+Q2+Q3 当季额累加展示）
     */
    private BigDecimal q3AmountActualApproved;

    /**
     * Q3季度：可用金额
     */
    private BigDecimal q3AmountAvailable;

    // ========== Q4季度数据 ==========

    /**
     * Q4季度：年度预算数
     */
    private BigDecimal q4AmountTotal;

    /**
     * Q4季度：预算调整数
     */
    private BigDecimal q4AmountAdj;

    /**
     * Q4季度：总预算数（年度预算数 + 预算调整数）
     */
    private BigDecimal q4TotalBudget;

    /**
     * Q4季度：冻结金额
     */
    private BigDecimal q4AmountFrozen;

    /**
     * Q4季度：占用金额
     */
    private BigDecimal q4AmountOccupied;

    /**
     * Q4季度：实际金额
     */
    private BigDecimal q4AmountActual;

    /**
     * Q4季度：已批准实际（视图中为全年当季额累加展示）
     */
    private BigDecimal q4AmountActualApproved;

    /**
     * Q4季度：可用金额
     */
    private BigDecimal q4AmountAvailable;
}

