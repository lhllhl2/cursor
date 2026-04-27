package com.jasolar.mis.module.system.controller.budget.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 项目投资额 Excel 导出 VO
 * 表头顺序：组织编码, 组织名称, 项目编号, 项目名称, 年度预算数, 预算调整数, 总预算数, 以前年度已使用金额,
 * 当年使用情况(冻结数、占用数、发生数), 可用预算数
 */
@Data
public class BudgetProjectInvestmentExcelVO {

    @ExcelProperty("组织编码")
    private String morgCode;

    @ExcelProperty("组织名称")
    private String morgName;

    @ExcelProperty("项目编号")
    private String projectCode;

    @ExcelProperty("项目名称")
    private String projectName;

    @ExcelProperty("年度预算数")
    private BigDecimal amountYearTotal;

    @ExcelProperty("预算调整数")
    private BigDecimal amountAdj;

    @ExcelProperty("总预算数")
    private BigDecimal amountTotal;

    @ExcelProperty("以前年度已使用金额")
    private BigDecimal lastYearUsedBudget;

    @ExcelProperty({"当年使用情况", "冻结数"})
    private BigDecimal amountFrozen;

    @ExcelProperty({"当年使用情况", "占用数"})
    private BigDecimal amountOccupied;

    @ExcelProperty({"当年使用情况", "发生数"})
    private BigDecimal amountActual;

    @ExcelProperty({"当年使用情况", "实际数"})
    private BigDecimal amountActualApproved;

    @ExcelProperty("可用预算数")
    private BigDecimal amountAvailable;
}


