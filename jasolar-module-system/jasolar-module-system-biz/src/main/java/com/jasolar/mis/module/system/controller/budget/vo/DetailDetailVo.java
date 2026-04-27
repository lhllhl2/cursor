package com.jasolar.mis.module.system.controller.budget.vo;

import com.jasolar.mis.module.system.controller.budget.validation.MutuallyExclusiveNotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Description: 明细信息
 * Author : Auto Generated
 * Date : 2025-01-XX
 * Version : 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@MutuallyExclusiveNotNull(
    field1 = "budgetSubjectCode",
    field2 = "erpAssetType",
    field1Name = "预算科目编码",
    field2Name = "ERP资产类型"
)
public class DetailDetailVo {

    /**
     * 明细行号
     */
    @NotBlank(message = "明细行号不能为空")
    private String detailLineNo;

    /**
     * 年度
     */
    @NotBlank(message = "年度不能为空")
    private String year;

    /**
     * 月份
     */
    @NotBlank(message = "月份不能为空")
    private String month;

    /**
     * 实际年度
     */
    private String actualYear;

    /**
     * 实际月份
     */
    private String actualMonth;

    /**
     * 公司编码
     */
    private String company;

    /**
     * 部门编码
     */
    private String department;

    /**
     * 管理组织编码
     */
    @NotBlank(message = "管理组织编码不能为空")
    private String managementOrg;

    /**
     * 管理组织名称
     */
    private String managementOrgName;

    /**
     * 预算科目编码
     */
    private String budgetSubjectCode;

    /**
     * 预算科目名称
     */
    private String budgetSubjectName;

    /**
     * 主数据项目编码
     */
    private String masterProjectCode;

    /**
     * 主数据项目名称
     */
    private String masterProjectName;

    /**
     * ERP 资产类型
     */
    private String erpAssetType;

    /**
     * 是否内部项目
     */
    @NotBlank(message = "是否在集团内不能为空")
    private String isInternal;

    /**
     * 金额
     */
    @NotNull(message = "金额不能为空")
    private BigDecimal amount;

    /**
     * 币种
     */
    @NotBlank(message = "币种不能为空")
    private String currency;

    /**
     * 额度总额（含调整）
     */
    private BigDecimal amountQuota;

    /**
     * 冻结金额
     */
    private BigDecimal amountFrozen;

    /**
     * 实际金额
     */
    private BigDecimal amountActual;

    /**
     * 可用金额
     */
    private BigDecimal amountAvailable;

    /**
     * 可用预算占比
     */
    private BigDecimal availableBudgetRatio;

    /**
     * 申请明细列表
     */
    private List<SubDetailVo> applyDetails;

    /**
     * 合同明细列表
     */
    private List<SubDetailVo> contractDetails;

    /**
     * 合同等扩展标识：如合同 effectType=1 表示框架协议（与预算调整单 effectType 语义独立）
     */
    private String effectType;
    
    /**
     * 元数据（扩展信息，JSON 字符串格式）
     * 存储格式：{"text1": "123", "text2": "234", ...}
     */
    private String metadata;
    
    /**
     * 校验结果（内部字段，用于传递校验结果）
     */
    private String validationResult;
    
    /**
     * 校验消息（内部字段，用于传递校验消息）
     */
    private String validationMessage;
}

