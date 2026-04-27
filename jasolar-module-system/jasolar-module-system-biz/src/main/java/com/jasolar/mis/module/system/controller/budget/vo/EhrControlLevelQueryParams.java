package com.jasolar.mis.module.system.controller.budget.vo;

import com.jasolar.mis.framework.common.pojo.PageParam;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

/**
 * Description: EHR控制层级查询参数
 * Author : Auto Generated
 * Date : 2025-01-XX
 * Version : 1.0
 */
@Data
@ToString
@ApiModel("EHR控制层级查询参数")
public class EhrControlLevelQueryParams extends PageParam {

    /**
     * EHR组织代码（支持模糊搜索）
     */
    @ApiModelProperty(value = "EHR组织代码（支持模糊搜索）", required = false)
    private String ehrCd;

    /**
     * 控制层级EHR组织代码（支持模糊搜索）
     */
    @ApiModelProperty(value = "控制层级EHR组织代码（支持模糊搜索）", required = false)
    private String controlEhrCd;

    /**
     * 预算组织编码（支持模糊搜索）
     */
    @ApiModelProperty(value = "预算组织编码（支持模糊搜索）", required = false)
    private String budgetOrgCd;

    /**
     * 预算层级EHR组织编码（支持模糊搜索）
     */
    @ApiModelProperty(value = "预算层级EHR组织编码（支持模糊搜索）", required = false)
    private String budgetEhrCd;

    /**
     * EHR组织名称（支持模糊搜索）
     */
    @ApiModelProperty(value = "EHR组织名称（支持模糊搜索）", required = false)
    private String ehrNm;

    /**
     * 控制层级EHR组织名称（支持模糊搜索）
     */
    @ApiModelProperty(value = "控制层级EHR组织名称（支持模糊搜索）", required = false)
    private String controlEhrNm;

    /**
     * 预算组织名称（支持模糊搜索）
     */
    @ApiModelProperty(value = "预算组织名称（支持模糊搜索）", required = false)
    private String budgetOrgNm;

    /**
     * 预算层级EHR组织名称（支持模糊搜索）
     */
    @ApiModelProperty(value = "预算层级EHR组织名称（支持模糊搜索）", required = false)
    private String budgetEhrNm;

    /**
     * ERP部门编码（支持模糊搜索）
     */
    @ApiModelProperty(value = "ERP部门编码（支持模糊搜索）", required = false)
    private String erpDepart;
}

