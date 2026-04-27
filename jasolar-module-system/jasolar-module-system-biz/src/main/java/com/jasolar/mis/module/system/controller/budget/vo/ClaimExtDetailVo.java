package com.jasolar.mis.module.system.controller.budget.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * Description: 扩展的付款/报销明细信息（用于查询）
 * Author : Auto Generated
 * Date : 2025-01-XX
 * Version : 1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ClaimExtDetailVo extends ClaimDetailDetailVo {

    /**
     * 付款/报销单号
     */
    private String claimOrderNo;

    /**
     * 可用预算占比
     */
    private BigDecimal availableBudgetRatio;
}

