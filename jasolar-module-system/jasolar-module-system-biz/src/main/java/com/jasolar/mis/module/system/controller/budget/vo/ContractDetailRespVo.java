package com.jasolar.mis.module.system.controller.budget.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * Description: 合同明细响应VO（用于申请接口返回）
 * Author : Auto Generated
 * Date : 2025-12-05
 * Version : 1.0
 */
@ApiModel(description = "合同明细响应VO（用于申请接口返回）")
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ContractDetailRespVo extends ContractDetailDetailVo {

    /**
     * 校验结果
     * 参考值：0 (表示通过)
     */
    @ApiModelProperty(value = "校验结果，0表示通过", example = "0", required = false)
    private String validationResult;

    /**
     * 校验消息
     * 参考值：通过
     */
    @ApiModelProperty(value = "校验消息", example = "通过", required = false)
    private String validationMessage;

    /**
     * 可用预算比例
     */
    @ApiModelProperty(value = "可用预算比例", example = "0.88", required = false)
    @JsonProperty("availableBudgetRatio")
    private BigDecimal availableBudgetRatio;

    /**
     * 额度总额（含调整）
     */
    @ApiModelProperty(value = "额度总额（含调整）", required = false)
    @JsonProperty("amountQuota")
    private BigDecimal amountQuota;

    /**
     * 冻结金额
     */
    @ApiModelProperty(value = "冻结金额", required = false)
    @JsonProperty("amountFrozen")
    private BigDecimal amountFrozen;

    /**
     * 实际金额
     */
    @ApiModelProperty(value = "实际金额", required = false)
    @JsonProperty("amountActual")
    private BigDecimal amountActual;

    /**
     * 可用金额
     */
    @ApiModelProperty(value = "可用金额", required = false)
    @JsonProperty("amountAvailable")
    private BigDecimal amountAvailable;
}

