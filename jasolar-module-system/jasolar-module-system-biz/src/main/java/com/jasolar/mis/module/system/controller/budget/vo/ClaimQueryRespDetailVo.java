package com.jasolar.mis.module.system.controller.budget.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Description: 报销/付款查询响应明细
 * Author : Auto Generated
 * Date : 2025-01-XX
 * Version : 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@ApiModel("报销/付款查询响应明细")
public class ClaimQueryRespDetailVo {

    @ApiModelProperty(value = "报销/付款年份", example = "2025")
    private String claimYear;

    @ApiModelProperty(value = "报销/付款月份", example = "01")
    private String claimMonth;

    @ApiModelProperty(value = "实际发生年份", example = "2025")
    private String actualYear;

    @ApiModelProperty(value = "实际发生月份", example = "01")
    private String actualMonth;

    @ApiModelProperty(value = "公司编码", example = "COMP-001")
    private String company;

    @ApiModelProperty(value = "部门编码", example = "DEPT-001")
    private String department;

    @ApiModelProperty(value = "管理组织编码", example = "MGT-ORG-001")
    private String managementOrg;

    @ApiModelProperty(value = "管理组织名称", example = "管理组织名称")
    private String managementOrgName;

    @ApiModelProperty(value = "预算科目编码", example = "SUBJ-001")
    private String budgetSubjectCode;

    @ApiModelProperty(value = "预算科目名称", example = "费用-办公用品")
    private String budgetSubjectName;

    @ApiModelProperty(value = "主数据项目编码", example = "PROJ-001")
    private String masterProjectCode;

    @ApiModelProperty(value = "主数据项目名称", example = "项目A")
    private String masterProjectName;

    @ApiModelProperty(value = "ERP 资产类型", example = "ASSET-TYPE-001")
    private String erpAssetType;

    @ApiModelProperty(value = "集团内/集团外(1:集团内,0:集团外)", example = "1")
    private String isInternal;

    @ApiModelProperty(value = "币种", example = "CNY")
    private String currency;

    @ApiModelProperty(value = "元数据（扩展信息），存储格式：{\"text1\": \"123\", \"text2\": \"234\", ...}", required = false)
    private Map<String, String> metadata;

    @ApiModelProperty(value = "可用金额", example = "10000.00")
    private BigDecimal amountAvailable;

    @ApiModelProperty(value = "校验结果", example = "0", required = false)
    private String validationResult;

    @ApiModelProperty(value = "校验消息", example = "处理成功", required = false)
    private String validationMessage;
}


