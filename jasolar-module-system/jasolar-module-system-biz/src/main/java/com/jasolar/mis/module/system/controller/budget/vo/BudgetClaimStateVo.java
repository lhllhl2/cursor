package com.jasolar.mis.module.system.controller.budget.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

/**
 * Description: 付款情况查询返回对象
 * Author : Auto Generated
 * Date : 2025-01-XX
 * Version : 1.0
 */
@Data
@ToString
@ApiModel("付款情况查询返回对象")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BudgetClaimStateVo {

    /**
     * 预算年度
     */
    @ApiModelProperty(value = "预算年度")
    private String year;

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
     * ERP科目编码
     */
    @ApiModelProperty(value = "ERP科目编码")
    private String erpAcctCd;

    /**
     * ERP科目名称
     */
    @ApiModelProperty(value = "ERP科目名称")
    private String erpAcctNm;

    /**
     * ERP资产类型编码
     */
    @ApiModelProperty(value = "ERP资产类型编码")
    private String erpAssetType;

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
     * 是否内部项目
     */
    @ApiModelProperty(value = "是否内部项目")
    private String isInternal;

    /**
     * 是否内部项目描述
     */
    @ApiModelProperty(value = "是否内部项目描述")
    private String isInternalDes;

    /**
     * 付款额
     */
    @ApiModelProperty(value = "付款额")
    private PaymentAmountVo paymentAmount;
}

