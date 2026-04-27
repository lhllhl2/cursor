package com.jasolar.mis.module.system.controller.budget.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * Description: 扩展的付款/报销合同明细关联信息
 * Author : Auto Generated
 * Date : 2025-01-XX
 * Version : 1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ClaimContractExtDetailVo extends ClaimContractDetailVo {

    /**
     * 合同金额
     */
    private BigDecimal contractAmount;

    /**
     * 管理组织编码
     */
    private String managementOrg;

    /**
     * 预算科目编码
     */
    private String budgetSubjectCode;

    /**
     * 主数据项目编码
     */
    private String masterProjectCode;
}

