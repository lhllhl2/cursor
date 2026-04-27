package com.jasolar.mis.module.system.controller.budget.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.ToString;

/**
 * Description: 预算季度明细查询参数
 * Author : Auto Generated
 * Date : 2025-01-XX
 * Version : 1.0
 */
@Data
@ToString
@ApiModel("预算季度明细查询参数")
public class BudgetBalanceDetailQueryParams {

    /**
     * 年度（必传）
     */
    @NotBlank(message = "年度不能为空")
    @ApiModelProperty(value = "年度", required = true, example = "2025")
    private String year;

    /**
     * 控制层级EHR组织编码（必传）
     */
    @NotBlank(message = "控制层级EHR组织编码不能为空")
    @ApiModelProperty(value = "控制层级EHR组织编码", required = true)
    private String controlEhrCd;
}
