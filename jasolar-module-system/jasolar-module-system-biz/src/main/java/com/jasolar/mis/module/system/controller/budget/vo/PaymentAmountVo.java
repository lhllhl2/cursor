package com.jasolar.mis.module.system.controller.budget.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * Description: 付款额返回对象（包含12个月金额）
 * Author : Auto Generated
 * Date : 2025-01-XX
 * Version : 1.0
 */
@Data
@ToString
@ApiModel("付款额返回对象")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentAmountVo {

    /**
     * 1月金额
     */
    @ApiModelProperty(value = "1月金额")
    private BigDecimal amount01;

    /**
     * 2月金额
     */
    @ApiModelProperty(value = "2月金额")
    private BigDecimal amount02;

    /**
     * 3月金额
     */
    @ApiModelProperty(value = "3月金额")
    private BigDecimal amount03;

    /**
     * 4月金额
     */
    @ApiModelProperty(value = "4月金额")
    private BigDecimal amount04;

    /**
     * 5月金额
     */
    @ApiModelProperty(value = "5月金额")
    private BigDecimal amount05;

    /**
     * 6月金额
     */
    @ApiModelProperty(value = "6月金额")
    private BigDecimal amount06;

    /**
     * 7月金额
     */
    @ApiModelProperty(value = "7月金额")
    private BigDecimal amount07;

    /**
     * 8月金额
     */
    @ApiModelProperty(value = "8月金额")
    private BigDecimal amount08;

    /**
     * 9月金额
     */
    @ApiModelProperty(value = "9月金额")
    private BigDecimal amount09;

    /**
     * 10月金额
     */
    @ApiModelProperty(value = "10月金额")
    private BigDecimal amount10;

    /**
     * 11月金额
     */
    @ApiModelProperty(value = "11月金额")
    private BigDecimal amount11;

    /**
     * 12月金额
     */
    @ApiModelProperty(value = "12月金额")
    private BigDecimal amount12;

    /**
     * 全年合计
     */
    @ApiModelProperty(value = "全年合计")
    private BigDecimal totalAmount;
}

