package com.jasolar.mis.module.system.controller.budget.vo;

import com.jasolar.mis.framework.common.pojo.PageParam;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * Description: 预算流水查询参数
 * Author : Auto Generated
 * Date : 2025-01-XX
 * Version : 1.0
 */
@Data
@ToString
@ApiModel("预算流水查询参数")
public class BudgetLedgerQueryParams extends PageParam {

    /**
     * 业务类型
     */
    @ApiModelProperty(value = "业务类型", required = true)
    @NotBlank(message = "业务类型不能为空")
    private String bizType;

    /**
     * 需求单号（预算申请/需求单），可为空按其它条件查询
     * 支持模糊搜索（%xxx%），例如：输入 "EA265" 可以匹配 "EA26532223"、"EA26532224"、"PREA265" 等
     */
    @ApiModelProperty(value = "需求单号（预算申请/需求单），支持模糊搜索（%xxx%）", required = false, example = "BUD-REQ-20250101-0001")
    private String demandOrderNo;

    /**
     * 合同号，可为空按其它条件查询
     * 支持模糊搜索（%xxx%），例如：输入 "CTR-2025" 可以匹配 "CTR-20250101-0001"、"CTR-20250102-0001"、"PRE-CTR-2025" 等
     */
    @ApiModelProperty(value = "合同号，支持模糊搜索（%xxx%）", required = false, example = "CTR-20250101-0001")
    private String contractNo;

    /**
     * 主单据号（根据bizType判断：APPLY时为需求单号，CONTRACT时为合同号，CLAIM时为付款/报销单号），可为空按其它条件查询
     */
    @ApiModelProperty(value = "主单据号（根据bizType判断：APPLY时为需求单号，CONTRACT时为合同号，CLAIM时为付款/报销单号）", required = false, example = "BUD-REQ-20250101-0001")
    private String bizCode;

    /**
     * 状态列表（支持多个状态查询）
     */
    @ApiModelProperty(value = "状态列表（支持多个状态查询）", required = false)
    private List<String> status;

    /**
     * 年份列表（支持多个年份查询）
     * 参考值：2025
     */
    @ApiModelProperty(value = "年份列表（支持多个年份查询）", required = false)
    private List<String> year;

    /**
     * 季度
     * 参考值：q1, q2, q3, q4
     */
    @ApiModelProperty(value = "季度", example = "q1", required = false)
    private String quarter;

    /**
     * 月份列表（支持多个月份查询）
     * 参考值：1-12
     */
    @ApiModelProperty(value = "月份列表（支持多个月份查询）", required = false)
    private List<String> month;

    /**
     * 管理组织编码
     */
    @ApiModelProperty(value = "管理组织编码", required = false)
    private String morgCode;

    /**
     * 管理组织名称（支持模糊搜索）
     */
    @ApiModelProperty(value = "管理组织名称（支持模糊搜索）", required = false)
    private String morgName;

    /**
     * 控制层级EHR组织代码（支持模糊搜索，%关键字%）
     */
    @ApiModelProperty(value = "控制层级EHR组织代码（支持模糊搜索）", required = false)
    private String controlEhrCd;

    /**
     * 控制层级EHR组织名称（支持模糊搜索，%关键字%）
     */
    @ApiModelProperty(value = "控制层级EHR组织名称（支持模糊搜索）", required = false)
    private String controlEhrNm;

    /**
     * 预算组织编码（支持模糊搜索，%关键字%）
     */
    @ApiModelProperty(value = "预算组织编码（支持模糊搜索）", required = false)
    private String budgetOrgCd;

    /**
     * 预算组织名称（支持模糊搜索，%关键字%）
     */
    @ApiModelProperty(value = "预算组织名称（支持模糊搜索）", required = false)
    private String budgetOrgNm;

    /**
     * 预算科目编码
     */
    @ApiModelProperty(value = "预算科目编码", required = false)
    private String budgetSubjectCode;

    /**
     * 预算科目名称（支持模糊搜索）
     */
    @ApiModelProperty(value = "预算科目名称（支持模糊搜索）", required = false)
    private String budgetSubjectName;

    /**
     * ERP资产类型
     */
    @ApiModelProperty(value = "ERP资产类型", required = false)
    private String erpAssetType;

    /**
     * ERP资产类型名称（支持模糊搜索）
     */
    @ApiModelProperty(value = "ERP资产类型名称（支持模糊搜索）", required = false)
    private String erpAssetTypeName;

    /**
     * 主数据项目编码
     */
    @ApiModelProperty(value = "主数据项目编码", required = false)
    private String masterProjectCode;

    /**
     * 主数据项目名称（支持模糊搜索）
     */
    @ApiModelProperty(value = "主数据项目名称（支持模糊搜索）", required = false)
    private String masterProjectName;

    /**
     * 操作人（支持模糊搜索，匹配操作人姓名或工号）
     */
    @ApiModelProperty(value = "操作人（支持模糊搜索，匹配操作人姓名或工号）", required = false)
    private String operator;

    /**
     * 是否在集团内列表（支持多个值查询）
     * 参考值：0-否，1-是
     */
    @ApiModelProperty(value = "是否在集团内列表（支持多个值查询），0-否，1-是", required = false)
    private List<String> isInternal;

    /**
     * 影响类型列表（支持多个值查询）
     * 参考值：0-预算调整-采购额，1-投资额调整，2-预算调整-付款额
     */
    @ApiModelProperty(value = "影响类型列表（支持多个值查询），0-预算调整-采购额，1-投资额调整，2-预算调整-付款额", required = false)
    private List<String> effectType;
}

