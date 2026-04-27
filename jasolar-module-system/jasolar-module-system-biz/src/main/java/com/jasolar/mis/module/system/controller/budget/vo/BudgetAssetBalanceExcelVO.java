package com.jasolar.mis.module.system.controller.budget.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 部门资产可用预算 Excel 导出 VO
 * 对应模板列顺序（根据Excel表头）：
 * A: EHR组织编号, B: EHR组织名称, C: 资产类型编码, D: 资产类型名称, E: 去年使用预算数
 * F-L: Q1年度预算数、Q1预算调整数、Q1总预算数、Q1冻结数、Q1占用数、Q1发生数、Q1可用预算数
 * M-S: Q2年度预算数、Q2预算调整数、Q2总预算数、Q2冻结数、Q2占用数、Q2发生数、Q2可用预算数
 * T-Z: Q3年度预算数、Q3预算调整数、Q3总预算数、Q3冻结数、Q3占用数、Q3发生数、Q3可用预算数
 * AA-AG: Q4年度预算数、Q4预算调整数、Q4总预算数、Q4冻结数、Q4占用数、Q4发生数、Q4可用预算数
 */
@Data
public class BudgetAssetBalanceExcelVO {

    @ExcelProperty("EHR组织编号")
    private String ehrCode;

    @ExcelProperty("EHR组织名称")
    private String ehrName;

    @ExcelProperty("资产类型编码")
    private String erpAssetType;

    @ExcelProperty("资产类型名称")
    private String erpAssetTypeName;

    // 去年使用预算数
    @ExcelProperty("去年使用预算数")
    private BigDecimal lastYearUsedBudget;

    // Q1季度
    @ExcelProperty("Q1年度预算数")
    private BigDecimal q1AmountYearTotal;

    @ExcelProperty("Q1预算调整数")
    private BigDecimal q1AmountAdj;

    @ExcelProperty("Q1总预算数")
    private BigDecimal q1TotalBudget;

    @ExcelProperty("Q1冻结数")
    private BigDecimal q1AmountFrozen;

    @ExcelProperty("Q1占用数")
    private BigDecimal q1AmountOccupied;

    @ExcelProperty("Q1发生数")
    private BigDecimal q1AmountActual;

    @ExcelProperty("Q1实际数")
    private BigDecimal q1AmountActualApproved;

    @ExcelProperty("Q1可用预算数")
    private BigDecimal q1AmountAvailable;

    // Q2季度
    @ExcelProperty("Q2年度预算数")
    private BigDecimal q2AmountYearTotal;

    @ExcelProperty("Q2预算调整数")
    private BigDecimal q2AmountAdj;

    @ExcelProperty("Q2总预算数")
    private BigDecimal q2TotalBudget;

    @ExcelProperty("Q2冻结数")
    private BigDecimal q2AmountFrozen;

    @ExcelProperty("Q2占用数")
    private BigDecimal q2AmountOccupied;

    @ExcelProperty("Q2发生数")
    private BigDecimal q2AmountActual;

    @ExcelProperty("Q2实际数")
    private BigDecimal q2AmountActualApproved;

    @ExcelProperty("Q2可用预算数")
    private BigDecimal q2AmountAvailable;

    // Q3季度
    @ExcelProperty("Q3年度预算数")
    private BigDecimal q3AmountYearTotal;

    @ExcelProperty("Q3预算调整数")
    private BigDecimal q3AmountAdj;

    @ExcelProperty("Q3总预算数")
    private BigDecimal q3TotalBudget;

    @ExcelProperty("Q3冻结数")
    private BigDecimal q3AmountFrozen;

    @ExcelProperty("Q3占用数")
    private BigDecimal q3AmountOccupied;

    @ExcelProperty("Q3发生数")
    private BigDecimal q3AmountActual;

    @ExcelProperty("Q3实际数")
    private BigDecimal q3AmountActualApproved;

    @ExcelProperty("Q3可用预算数")
    private BigDecimal q3AmountAvailable;

    // Q4季度
    @ExcelProperty("Q4年度预算数")
    private BigDecimal q4AmountYearTotal;

    @ExcelProperty("Q4预算调整数")
    private BigDecimal q4AmountAdj;

    @ExcelProperty("Q4总预算数")
    private BigDecimal q4TotalBudget;

    @ExcelProperty("Q4冻结数")
    private BigDecimal q4AmountFrozen;

    @ExcelProperty("Q4占用数")
    private BigDecimal q4AmountOccupied;

    @ExcelProperty("Q4发生数")
    private BigDecimal q4AmountActual;

    @ExcelProperty("Q4实际数")
    private BigDecimal q4AmountActualApproved;

    @ExcelProperty("Q4可用预算数")
    private BigDecimal q4AmountAvailable;
}

