package com.jasolar.mis.module.system.domain.budget;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 预算付款（CLAIM）月度聚合视图实体
 * 对应视图：V_BUDGET_CLAIM_MONTHLY_AGGREGATE
 * 说明：聚合 BUDGET_LEDGER 表中 BIZ_TYPE='CLAIM' 的付款数据，将12个月的付款额（AMOUNT_AVAILABLE）按维度聚合展示
 * 维度：YEAR, MORG_CODE, BUDGET_SUBJECT_CODE, MASTER_PROJECT_CODE, ERP_ASSET_TYPE, IS_INTERNAL
 *
 * @author Auto
 */
@Data
public class BudgetClaimMonthlyAggregate {

    /**
     * 年度
     */
    private String year;

    /**
     * 管理组织编码
     */
    private String morgCode;

    /**
     * 预算科目编码
     */
    private String budgetSubjectCode;

    /**
     * 主数据项目编码
     */
    private String masterProjectCode;

    /**
     * ERP资产类型编码
     */
    private String erpAssetType;

    /**
     * 是否内部项目
     */
    private String isInternal;

    /**
     * EHR组织名称（通过 EHR_ORG_MANAGE_R 关联获取）
     */
    private String ehrName;

    /**
     * ERP科目名称（通过 SUBJECT_INFO 关联获取）
     */
    private String erpAcctNm;

    /**
     * 项目名称（通过 PROJECT_CONTROL_R 关联获取）
     */
    private String projectName;

    // ========== 1月付款额 ==========

    /**
     * 1月付款额（AMOUNT_AVAILABLE）
     */
    private BigDecimal m1AmountAvailable;

    // ========== 2月付款额 ==========

    /**
     * 2月付款额（AMOUNT_AVAILABLE）
     */
    private BigDecimal m2AmountAvailable;

    // ========== 3月付款额 ==========

    /**
     * 3月付款额（AMOUNT_AVAILABLE）
     */
    private BigDecimal m3AmountAvailable;

    // ========== 4月付款额 ==========

    /**
     * 4月付款额（AMOUNT_AVAILABLE）
     */
    private BigDecimal m4AmountAvailable;

    // ========== 5月付款额 ==========

    /**
     * 5月付款额（AMOUNT_AVAILABLE）
     */
    private BigDecimal m5AmountAvailable;

    // ========== 6月付款额 ==========

    /**
     * 6月付款额（AMOUNT_AVAILABLE）
     */
    private BigDecimal m6AmountAvailable;

    // ========== 7月付款额 ==========

    /**
     * 7月付款额（AMOUNT_AVAILABLE）
     */
    private BigDecimal m7AmountAvailable;

    // ========== 8月付款额 ==========

    /**
     * 8月付款额（AMOUNT_AVAILABLE）
     */
    private BigDecimal m8AmountAvailable;

    // ========== 9月付款额 ==========

    /**
     * 9月付款额（AMOUNT_AVAILABLE）
     */
    private BigDecimal m9AmountAvailable;

    // ========== 10月付款额 ==========

    /**
     * 10月付款额（AMOUNT_AVAILABLE）
     */
    private BigDecimal m10AmountAvailable;

    // ========== 11月付款额 ==========

    /**
     * 11月付款额（AMOUNT_AVAILABLE）
     */
    private BigDecimal m11AmountAvailable;

    // ========== 12月付款额 ==========

    /**
     * 12月付款额（AMOUNT_AVAILABLE）
     */
    private BigDecimal m12AmountAvailable;
}

