package com.jasolar.mis.module.system.controller.budget.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;


/**
 * Description:
 * Author : Zhou Hai
 * Date : 18/12/2025 14:25
 * Version : 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@ApiModel("预算组织查询结果")
public class BudgetQueryRelationsVo {

    private ESBRespInfoVo esbInfo;

    @ApiModelProperty(value = "组织相关信息", required = false)
    private BudgetOrgResultVo orgResult;

}


