package com.jasolar.mis.module.system.controller.budget.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Description: EHR控制层级查询结果
 * Author : Auto Generated
 * Date : 2025-01-XX
 * Version : 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("EHR控制层级查询结果")
public class EhrControlLevelQueryVo {

    /**
     * 原始EHR组织代码
     */
    @ApiModelProperty(value = "原始EHR组织代码")
    private String ehrCd;

    /**
     * 向上追溯找到的CONTROL_LEVEL=1的EHR组织代码
     */
    @ApiModelProperty(value = "控制层级EHR组织代码")
    private String controlEhrCd;

    /**
     * 预算组织编码（从EHR_CD向上追溯找到的第一个非NULL的ORG_CD）
     */
    @ApiModelProperty(value = "预算组织编码")
    private String budgetOrgCd;

    /**
     * 预算层级的EHR组织编码（找到BUDGET_ORG_CD时对应的EHR_CD）
     */
    @ApiModelProperty(value = "预算层级EHR组织编码")
    private String budgetEhrCd;

    /**
     * EHR组织名称（对应EHR_CD）
     */
    @ApiModelProperty(value = "EHR组织名称")
    private String ehrNm;

    /**
     * 控制层级EHR组织名称（对应CONTROL_EHR_CD）
     */
    @ApiModelProperty(value = "控制层级EHR组织名称")
    private String controlEhrNm;

    /**
     * 预算组织名称（对应BUDGET_ORG_CD）
     */
    @ApiModelProperty(value = "预算组织名称")
    private String budgetOrgNm;

    /**
     * 预算层级EHR组织名称（对应BUDGET_EHR_CD）
     */
    @ApiModelProperty(value = "预算层级EHR组织名称")
    private String budgetEhrNm;

    /**
     * ERP部门编码（口径：起点 EHR_CD 对应的 ERP_DEPART）
     */
    @ApiModelProperty(value = "ERP部门编码")
    private String erpDepart;
}

