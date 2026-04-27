package com.jasolar.mis.module.system.controller.budget.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 项目付款额 Excel 导出 VO
 * 表头顺序：组织编码, 组织名称, 项目编号, 项目名称, 去年使用预算数, 顶部汇总列（年度预算数、预算调整数、总预算数），
 * 各季度：冻结数、占用数、发生数、可用预算数、年度预算数、预算调整数、总预算数
 */
@Data
public class BudgetProjectPaymentExcelVO {

    @ExcelProperty("组织编码")
    private String morgCode;

    @ExcelProperty("组织名称")
    private String morgName;

    @ExcelProperty("项目编号")
    private String projectCode;

    @ExcelProperty("项目名称")
    private String projectName;

    @ExcelProperty("去年使用预算数")
    private BigDecimal lastYearUsedBudget;

    // 顶部汇总列（如果需要的话，可以为空）
    @ExcelProperty("年度预算数")
    private BigDecimal totalAmountYearTotal;

    @ExcelProperty("预算调整数")
    private BigDecimal totalAmountAdj;

    @ExcelProperty("总预算数")
    private BigDecimal totalAmountTotal;

    // Q1季度
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

    @ExcelProperty("Q1年度预算数")
    private BigDecimal q1AmountYearTotal;

    @ExcelProperty("Q1预算调整数")
    private BigDecimal q1AmountAdj;

    @ExcelProperty("Q1总预算数")
    private BigDecimal q1AmountTotal;

    // Q2季度
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

    @ExcelProperty("Q2年度预算数")
    private BigDecimal q2AmountYearTotal;

    @ExcelProperty("Q2预算调整数")
    private BigDecimal q2AmountAdj;

    @ExcelProperty("Q2总预算数")
    private BigDecimal q2AmountTotal;

    // Q3季度
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

    @ExcelProperty("Q3年度预算数")
    private BigDecimal q3AmountYearTotal;

    @ExcelProperty("Q3预算调整数")
    private BigDecimal q3AmountAdj;

    @ExcelProperty("Q3总预算数")
    private BigDecimal q3AmountTotal;

    // Q4季度
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

    @ExcelProperty("Q4年度预算数")
    private BigDecimal q4AmountYearTotal;

    @ExcelProperty("Q4预算调整数")
    private BigDecimal q4AmountAdj;

    @ExcelProperty("Q4总预算数")
    private BigDecimal q4AmountTotal;
}


