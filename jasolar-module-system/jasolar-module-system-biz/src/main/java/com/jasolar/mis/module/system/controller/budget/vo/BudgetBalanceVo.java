package com.jasolar.mis.module.system.controller.budget.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

/**
 * Description: 预算余额返回对象
 * Author : Auto Generated
 * Date : 2025-01-XX
 * Version : 1.0
 */
@Data
@ToString
@ApiModel("预算余额返回对象")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BudgetBalanceVo {

    /**
     * EHR组织编码
     */
    @ApiModelProperty(value = "EHR组织编码")
    private String ehrCode;

    /**
     * EHR组织名称
     */
    @ApiModelProperty(value = "EHR组织名称")
    private String ehrName;

    /**
     * 预算科目编码
     */
    @ApiModelProperty(value = "预算科目编码")
    private String subjectCode;

    /**
     * 预算科目名称
     */
    @ApiModelProperty(value = "预算科目名称")
    private String subjectName;

    /**
     * 控制层级CUST1编码
     */
    @ApiModelProperty(value = "控制层级CUST1编码")
    private String controlCust1Cd;

    /**
     * 控制层级CUST1名称
     */
    @ApiModelProperty(value = "控制层级CUST1名称")
    private String controlCust1Name;

    /**
     * 以前年度已使用金额（去年同一维度的冻结数+占用数+发生数，无则展示0）
     */
    @ApiModelProperty(value = "以前年度已使用金额")
    private java.math.BigDecimal lastYearUsedBudget;

    /**
     * ERP资产类型编码
     */
    @ApiModelProperty(value = "ERP资产类型编码")
    private String erpAssetType;

    /**
     * ERP资产类型名称
     */
    @ApiModelProperty(value = "ERP资产类型名称")
    private String erpAssetTypeName;

    /**
     * 项目编码
     */
    @ApiModelProperty(value = "项目编码")
    private String projectCode;

    /**
     * 项目名称
     */
    @ApiModelProperty(value = "项目名称")
    private String projectName;

    /**
     * 集团内/集团外
     */
    @ApiModelProperty(value = "集团内/集团外")
    private String isInternal;

    /**
     * 第一季度预算金额
     */
    @ApiModelProperty(value = "第一季度预算金额")
    private BudgetAmountVo q1;

    /**
     * 第二季度预算金额
     */
    @ApiModelProperty(value = "第二季度预算金额")
    private BudgetAmountVo q2;

    /**
     * 第三季度预算金额
     */
    @ApiModelProperty(value = "第三季度预算金额")
    private BudgetAmountVo q3;

    /**
     * 第四季度预算金额
     */
    @ApiModelProperty(value = "第四季度预算金额")
    private BudgetAmountVo q4;

    /**
     * 总计预算金额
     */
    @ApiModelProperty(value = "总计预算金额")
    private BudgetAmountVo total;
}

