package com.jasolar.mis.module.system.controller.budget.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * Description: 预算金额返回对象
 * Author : Auto Generated
 * Date : 2025-01-XX
 * Version : 1.0
 */
@Data
@ToString
@ApiModel("预算金额返回对象")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BudgetAmountVo {

    /**
     * 总金额
     */
    @ApiModelProperty(value = "总金额")
    private BigDecimal amountTotal;

    /**
     * 年度总金额
     */
    @ApiModelProperty(value = "年度总金额")
    private BigDecimal amountYearTotal;

    /**
     * 调整金额
     */
    @ApiModelProperty(value = "调整金额")
    private BigDecimal amountAdj;

    /**
     * 冻结金额
     */
    @ApiModelProperty(value = "冻结金额")
    private BigDecimal amountFrozen;

    /**
     * 占用金额
     */
    @ApiModelProperty(value = "占用金额")
    private BigDecimal amountOccupied;

    /**
     * 实际金额
     */
    @ApiModelProperty(value = "实际金额")
    private BigDecimal amountActual;

    /**
     * 已批准实际金额（从BUDGET_LEDGER获取，状态为APPROVED或APPROVED_UPDATE的CLAIM类型数据）
     */
    @ApiModelProperty(value = "已批准实际金额")
    private BigDecimal amountActualApproved;

    /**
     * 可用金额
     */
    @ApiModelProperty(value = "可用金额")
    private BigDecimal amountAvailable;
}

