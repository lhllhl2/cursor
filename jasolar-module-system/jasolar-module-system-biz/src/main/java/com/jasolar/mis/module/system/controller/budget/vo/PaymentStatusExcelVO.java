package com.jasolar.mis.module.system.controller.budget.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 付款情况 Excel 导出 VO
 * 对应模板列顺序（付款情况导出报表.xlsx）：
 * A: 项目编号, B: 项目名称, C: EHR组织编码, D: EHR组织名称
 * E: 费用科目编码, F: 费用科目名称, G: 资产类型
 * H-S: 付款额（1月-12月）
 */
@Data
public class PaymentStatusExcelVO {

    @ExcelProperty("项目编号")
    private String projectCode;

    @ExcelProperty("项目名称")
    private String projectName;

    @ExcelProperty("EHR组织编码")
    private String ehrCode;

    @ExcelProperty("EHR组织名称")
    private String ehrName;

    @ExcelProperty("费用科目编码")
    private String erpAcctCd;

    @ExcelProperty("费用科目名称")
    private String erpAcctNm;

    @ExcelProperty("资产类型")
    private String erpAssetType;

    @ExcelProperty("1月")
    private BigDecimal amount01;

    @ExcelProperty("2月")
    private BigDecimal amount02;

    @ExcelProperty("3月")
    private BigDecimal amount03;

    @ExcelProperty("4月")
    private BigDecimal amount04;

    @ExcelProperty("5月")
    private BigDecimal amount05;

    @ExcelProperty("6月")
    private BigDecimal amount06;

    @ExcelProperty("7月")
    private BigDecimal amount07;

    @ExcelProperty("8月")
    private BigDecimal amount08;

    @ExcelProperty("9月")
    private BigDecimal amount09;

    @ExcelProperty("10月")
    private BigDecimal amount10;

    @ExcelProperty("11月")
    private BigDecimal amount11;

    @ExcelProperty("12月")
    private BigDecimal amount12;
}

