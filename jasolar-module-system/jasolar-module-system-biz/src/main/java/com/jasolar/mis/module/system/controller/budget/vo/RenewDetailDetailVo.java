package com.jasolar.mis.module.system.controller.budget.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Description: 续期明细信息
 * Author : Auto Generated
 * Date : 2025-01-XX
 * Version : 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RenewDetailDetailVo {

    /**
     * 需求明细行号
     * 参考值：XQMX001
     */
    @NotBlank(message = "需求明细行号不能为空")
    private String demandDetailLineNo;

    /**
     * 需求金额
     * 参考值：100.22
     */
    @NotNull(message = "需求金额不能为空")
    private BigDecimal demandAmount;

    /**
     * 币种
     * 参考值：CNY
     */
    @NotBlank(message = "币种不能为空")
    private String currency;
}

