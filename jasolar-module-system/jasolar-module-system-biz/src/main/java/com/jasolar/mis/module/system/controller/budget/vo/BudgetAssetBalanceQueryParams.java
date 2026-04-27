package com.jasolar.mis.module.system.controller.budget.vo;

import com.jasolar.mis.framework.common.pojo.PageParam;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * Description: 预算资产余额查询参数
 * Author : Auto Generated
 * Date : 2025-01-XX
 * Version : 1.0
 */
@Data
@ToString
@ApiModel("预算资产余额查询参数")
public class BudgetAssetBalanceQueryParams extends PageParam {

    /**
     * EHR组织编码
     */
    @ApiModelProperty(value = "EHR组织编码", required = false)
    private String ehrCode;

    /**
     * EHR组织名称
     */
    @ApiModelProperty(value = "EHR组织名称", required = false)
    private String ehrName;

    /**
     * 年度
     */
    @ApiModelProperty(value = "年度", required = false)
    private String year;

    /**
     * ERP资产类型编码
     */
    @ApiModelProperty(value = "ERP资产类型编码", required = false)
    private String erpAssetType;

    /**
     * ERP资产类型名称（用于模糊搜索）
     */
    @ApiModelProperty(value = "ERP资产类型名称（用于模糊搜索）", required = false)
    private String erpAssetTypeName;

    /**
     * 预算类型
     */
    @ApiModelProperty(value = "预算类型", required = true)
    @NotBlank(message = "预算类型不能为空")
    private String budgetType;

    /**
     * EHR组织编码列表（用于权限过滤，IN 查询）
     */
    @ApiModelProperty(value = "EHR组织编码列表（用于权限过滤）", required = false)
    private List<String> ehrCdList;
}

