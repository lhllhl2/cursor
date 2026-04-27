package com.jasolar.mis.module.system.controller.budget.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.util.List;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 18/12/2025 15:00
 * Version : 1.0
 */
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@ApiModel("预算组织查询结果")
public class BudgetOrgResultVo {

    @ApiModelProperty(value = "组织明细列表", required = false)
    private List<EhrDetailResultVo> orgDetails;
}
