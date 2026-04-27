package com.jasolar.mis.module.system.controller.budget.vo;

import com.jasolar.mis.framework.common.pojo.PageParam;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * Description: 预算余额查询参数
 * Author : Auto Generated
 * Date : 2025-01-XX
 * Version : 1.0
 */
@Data
@ToString
@ApiModel("预算余额查询参数")
public class BudgetBalanceQueryParams extends PageParam {

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
     * 自定义编码（对应 CUST1_CD）
     */
    @ApiModelProperty(value = "自定义编码（对应 CUST1_CD）", required = false)
    private String customCode;

    /**
     * 自定义名称（对应 CUST1_NM）
     */
    @ApiModelProperty(value = "自定义名称（对应 CUST1_NM）", required = false)
    private String customName;

    /**
     * 科目编码（对应 ACCT_CD）
     */
    @ApiModelProperty(value = "科目编码（对应 ACCT_CD）", required = false)
    private String accountSubjectCode;

    /**
     * 科目名称（对应 ACCT_NM）
     */
    @ApiModelProperty(value = "科目名称（对应 ACCT_NM）", required = false)
    private String accountSubjectName;

    /**
     * 控制层级CUST1编码（对应 CONTROL_CUST1_CD，支持模糊搜索）
     */
    @ApiModelProperty(value = "控制层级CUST1编码（对应 CONTROL_CUST1_CD，支持模糊搜索）", required = false)
    private String controlCust1Cd;

    /**
     * 控制层级CUST1名称（对应 CONTROL_CUST1_NAME，支持模糊搜索）
     */
    @ApiModelProperty(value = "控制层级CUST1名称（对应 CONTROL_CUST1_NAME，支持模糊搜索）", required = false)
    private String controlCust1Name;

    /**
     * EHR组织编码列表（用于权限过滤，IN 查询）
     */
    @ApiModelProperty(value = "EHR组织编码列表（用于权限过滤）", required = false)
    private List<String> ehrCdList;

    /**
     * 项目编码列表（用于权限过滤，IN 查询）
     */
    @ApiModelProperty(value = "项目编码列表（用于权限过滤）", required = false)
    private List<String> prjCdList;
}

