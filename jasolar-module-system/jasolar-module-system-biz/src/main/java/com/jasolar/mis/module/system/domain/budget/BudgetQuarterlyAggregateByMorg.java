package com.jasolar.mis.module.system.domain.budget;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 预算季度聚合视图实体（按采购额/付款额分类，按原始组织）
 * 对应视图：V_BUDGET_QUARTERLY_AGGREGATE_BY_MORG
 * 说明：按采购额和付款额分类展示预算季度聚合数据，按YEAR、MORG_CODE、ERP_ASSET_TYPE聚合（不聚合组织层级）
 *
 * @author Auto
 */
@Data
public class BudgetQuarterlyAggregateByMorg {

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
     * ERP资产类型编码
     */
    private String erpAssetType;

    /**
     * ERP资产类型名称（通过 VIEW_BUDGET_MEMBER_NAME_CODE 视图关联获取）
     */
    private String erpAssetTypeName;

    // ========== 采购额 Q1季度数据 ==========

    /**
     * 采购额 Q1季度：年度预算数
     */
    private BigDecimal purchaseQ1AmountTotal;

    /**
     * 采购额 Q1季度：预算调整数
     */
    private BigDecimal purchaseQ1AmountAdj;

    /**
     * 采购额 Q1季度：总预算数（年度预算数 + 预算调整数）
     */
    private BigDecimal purchaseQ1TotalBudget;

    /**
     * 采购额 Q1季度：冻结金额
     */
    private BigDecimal purchaseQ1AmountFrozen;

    /**
     * 采购额 Q1季度：占用金额
     */
    private BigDecimal purchaseQ1AmountOccupied;

    /**
     * 采购额 Q1季度：实际金额
     */
    private BigDecimal purchaseQ1AmountActual;

    /**
     * 采购额 Q1季度：已批准实际金额（从BUDGET_LEDGER获取，状态为APPROVED或APPROVED_UPDATE的CLAIM类型数据，1-3月累积）
     */
    private BigDecimal purchaseQ1AmountActualApproved;

    /**
     * 采购额 Q1季度：可用金额
     */
    private BigDecimal purchaseQ1AmountAvailable;

    // ========== 采购额 Q2季度数据 ==========

    /**
     * 采购额 Q2季度：年度预算数（累积：Q1+Q2）
     */
    private BigDecimal purchaseQ2AmountTotal;

    /**
     * 采购额 Q2季度：预算调整数（累积：Q1+Q2）
     */
    private BigDecimal purchaseQ2AmountAdj;

    /**
     * 采购额 Q2季度：总预算数（累积：Q1+Q2）
     */
    private BigDecimal purchaseQ2TotalBudget;

    /**
     * 采购额 Q2季度：冻结金额（累积：Q1+Q2）
     */
    private BigDecimal purchaseQ2AmountFrozen;

    /**
     * 采购额 Q2季度：占用金额（累积：Q1+Q2）
     */
    private BigDecimal purchaseQ2AmountOccupied;

    /**
     * 采购额 Q2季度：实际金额（累积：Q1+Q2）
     */
    private BigDecimal purchaseQ2AmountActual;

    /**
     * 采购额 Q2季度：已批准实际金额（从BUDGET_LEDGER获取，状态为APPROVED或APPROVED_UPDATE的CLAIM类型数据，1-6月累积）
     */
    private BigDecimal purchaseQ2AmountActualApproved;

    /**
     * 采购额 Q2季度：可用金额（累积：Q1+Q2）
     */
    private BigDecimal purchaseQ2AmountAvailable;

    // ========== 采购额 Q3季度数据 ==========

    /**
     * 采购额 Q3季度：年度预算数（累积：Q1+Q2+Q3）
     */
    private BigDecimal purchaseQ3AmountTotal;

    /**
     * 采购额 Q3季度：预算调整数（累积：Q1+Q2+Q3）
     */
    private BigDecimal purchaseQ3AmountAdj;

    /**
     * 采购额 Q3季度：总预算数（累积：Q1+Q2+Q3）
     */
    private BigDecimal purchaseQ3TotalBudget;

    /**
     * 采购额 Q3季度：冻结金额（累积：Q1+Q2+Q3）
     */
    private BigDecimal purchaseQ3AmountFrozen;

    /**
     * 采购额 Q3季度：占用金额（累积：Q1+Q2+Q3）
     */
    private BigDecimal purchaseQ3AmountOccupied;

    /**
     * 采购额 Q3季度：实际金额（累积：Q1+Q2+Q3）
     */
    private BigDecimal purchaseQ3AmountActual;

    /**
     * 采购额 Q3季度：已批准实际金额（从BUDGET_LEDGER获取，状态为APPROVED或APPROVED_UPDATE的CLAIM类型数据，1-9月累积）
     */
    private BigDecimal purchaseQ3AmountActualApproved;

    /**
     * 采购额 Q3季度：可用金额（累积：Q1+Q2+Q3）
     */
    private BigDecimal purchaseQ3AmountAvailable;

    // ========== 采购额 Q4季度数据 ==========

    /**
     * 采购额 Q4季度：年度预算数（累积：Q1+Q2+Q3+Q4）
     */
    private BigDecimal purchaseQ4AmountTotal;

    /**
     * 采购额 Q4季度：预算调整数（累积：Q1+Q2+Q3+Q4）
     */
    private BigDecimal purchaseQ4AmountAdj;

    /**
     * 采购额 Q4季度：总预算数（累积：Q1+Q2+Q3+Q4）
     */
    private BigDecimal purchaseQ4TotalBudget;

    /**
     * 采购额 Q4季度：冻结金额（累积：Q1+Q2+Q3+Q4）
     */
    private BigDecimal purchaseQ4AmountFrozen;

    /**
     * 采购额 Q4季度：占用金额（累积：Q1+Q2+Q3+Q4）
     */
    private BigDecimal purchaseQ4AmountOccupied;

    /**
     * 采购额 Q4季度：实际金额（累积：Q1+Q2+Q3+Q4）
     */
    private BigDecimal purchaseQ4AmountActual;

    /**
     * 采购额 Q4季度：已批准实际金额（从BUDGET_LEDGER获取，状态为APPROVED或APPROVED_UPDATE的CLAIM类型数据，1-12月累积）
     */
    private BigDecimal purchaseQ4AmountActualApproved;

    /**
     * 采购额 Q4季度：可用金额（累积：Q1+Q2+Q3+Q4）
     */
    private BigDecimal purchaseQ4AmountAvailable;

    // ========== 付款额 Q1季度数据 ==========

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
     * 付款额 Q1季度：已批准实际金额（从BUDGET_LEDGER获取，状态为APPROVED或APPROVED_UPDATE的CLAIM类型数据，1-3月累积）
     */
    private BigDecimal paymentQ1AmountActualApproved;

    /**
     * 付款额 Q1季度：可用金额
     */
    private BigDecimal paymentQ1AmountAvailable;

    // ========== 付款额 Q2季度数据 ==========

    /**
     * 付款额 Q2季度：年度预算数（累积：Q1+Q2）
     */
    private BigDecimal paymentQ2AmountTotal;

    /**
     * 付款额 Q2季度：预算调整数（累积：Q1+Q2）
     */
    private BigDecimal paymentQ2AmountAdj;

    /**
     * 付款额 Q2季度：总预算数（累积：Q1+Q2）
     */
    private BigDecimal paymentQ2TotalBudget;

    /**
     * 付款额 Q2季度：冻结金额（累积：Q1+Q2）
     */
    private BigDecimal paymentQ2AmountFrozen;

    /**
     * 付款额 Q2季度：占用金额（累积：Q1+Q2）
     */
    private BigDecimal paymentQ2AmountOccupied;

    /**
     * 付款额 Q2季度：实际金额（累积：Q1+Q2）
     */
    private BigDecimal paymentQ2AmountActual;

    /**
     * 付款额 Q2季度：已批准实际金额（从BUDGET_LEDGER获取，状态为APPROVED或APPROVED_UPDATE的CLAIM类型数据，1-6月累积）
     */
    private BigDecimal paymentQ2AmountActualApproved;

    /**
     * 付款额 Q2季度：可用金额（累积：Q1+Q2）
     */
    private BigDecimal paymentQ2AmountAvailable;

    // ========== 付款额 Q3季度数据 ==========

    /**
     * 付款额 Q3季度：年度预算数（累积：Q1+Q2+Q3）
     */
    private BigDecimal paymentQ3AmountTotal;

    /**
     * 付款额 Q3季度：预算调整数（累积：Q1+Q2+Q3）
     */
    private BigDecimal paymentQ3AmountAdj;

    /**
     * 付款额 Q3季度：总预算数（累积：Q1+Q2+Q3）
     */
    private BigDecimal paymentQ3TotalBudget;

    /**
     * 付款额 Q3季度：冻结金额（累积：Q1+Q2+Q3）
     */
    private BigDecimal paymentQ3AmountFrozen;

    /**
     * 付款额 Q3季度：占用金额（累积：Q1+Q2+Q3）
     */
    private BigDecimal paymentQ3AmountOccupied;

    /**
     * 付款额 Q3季度：实际金额（累积：Q1+Q2+Q3）
     */
    private BigDecimal paymentQ3AmountActual;

    /**
     * 付款额 Q3季度：已批准实际金额（从BUDGET_LEDGER获取，状态为APPROVED或APPROVED_UPDATE的CLAIM类型数据，1-9月累积）
     */
    private BigDecimal paymentQ3AmountActualApproved;

    /**
     * 付款额 Q3季度：可用金额（累积：Q1+Q2+Q3）
     */
    private BigDecimal paymentQ3AmountAvailable;

    // ========== 付款额 Q4季度数据 ==========

    /**
     * 付款额 Q4季度：年度预算数（累积：Q1+Q2+Q3+Q4）
     */
    private BigDecimal paymentQ4AmountTotal;

    /**
     * 付款额 Q4季度：预算调整数（累积：Q1+Q2+Q3+Q4）
     */
    private BigDecimal paymentQ4AmountAdj;

    /**
     * 付款额 Q4季度：总预算数（累积：Q1+Q2+Q3+Q4）
     */
    private BigDecimal paymentQ4TotalBudget;

    /**
     * 付款额 Q4季度：冻结金额（累积：Q1+Q2+Q3+Q4）
     */
    private BigDecimal paymentQ4AmountFrozen;

    /**
     * 付款额 Q4季度：占用金额（累积：Q1+Q2+Q3+Q4）
     */
    private BigDecimal paymentQ4AmountOccupied;

    /**
     * 付款额 Q4季度：实际金额（累积：Q1+Q2+Q3+Q4）
     */
    private BigDecimal paymentQ4AmountActual;

    /**
     * 付款额 Q4季度：已批准实际金额（从BUDGET_LEDGER获取，状态为APPROVED或APPROVED_UPDATE的CLAIM类型数据，1-12月累积）
     */
    private BigDecimal paymentQ4AmountActualApproved;

    /**
     * 付款额 Q4季度：可用金额（累积：Q1+Q2+Q3+Q4）
     */
    private BigDecimal paymentQ4AmountAvailable;
}
