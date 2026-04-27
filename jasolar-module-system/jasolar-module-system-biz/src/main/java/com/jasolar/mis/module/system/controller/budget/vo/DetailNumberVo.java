package com.jasolar.mis.module.system.controller.budget.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 明细数值信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DetailNumberVo {

    /**
     * 预算额度（amountTotal + amountAdj）
     */
    private BigDecimal amountQuota;

    /**
     * 冻结金额
     */
    private BigDecimal amountFrozen;

    /**
     * 实际金额
     */
    private BigDecimal amountActual;

    /**
     * 可用金额
     */
    private BigDecimal amountAvailable;

    /**
     * 可用预算占比
     */
    private BigDecimal availableBudgetRatio;
}


