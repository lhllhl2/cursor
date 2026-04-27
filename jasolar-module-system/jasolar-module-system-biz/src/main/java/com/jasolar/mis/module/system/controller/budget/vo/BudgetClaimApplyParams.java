package com.jasolar.mis.module.system.controller.budget.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.ToString;

/**
 * Description: 预算付款/报销申请参数
 * Author : Auto Generated
 * Date : 2025-01-XX
 * Version : 1.0
 */
@ApiModel(description = "预算付款/报销申请参数")
@Data
@ToString
public class BudgetClaimApplyParams {

    /**
     * ESB信息
     */
    @ApiModelProperty(value = "ESB信息", required = true)
    @NotNull(message = "ESB信息不能为空")
    @Valid
    private ESBInfoParams esbInfo;

    /**
     * 付款/报销申请请求信息
     */
    @ApiModelProperty(value = "付款/报销申请请求信息", required = true)
    @NotNull(message = "付款/报销申请请求信息不能为空")
    @Valid
    private ClaimApplyReqInfoParams claimApplyReqInfo;
}

