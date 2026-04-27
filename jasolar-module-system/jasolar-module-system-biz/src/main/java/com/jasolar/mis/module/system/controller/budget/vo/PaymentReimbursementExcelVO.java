package com.jasolar.mis.module.system.controller.budget.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 付款报销单报表 Excel 导出 VO
 * 对应模板：付款报销单报表.xlsx
 * 字段顺序：报销/付款单号、合同单号、需求单号、单据状态、实际发生年度、实际发生月度、EHR编码、EHR名称、
 * 费用科目编码、费用科目名称、资产类型、项目编码、项目名称、集团内/集团外、报销金额/付款金额
 */
@Data
public class PaymentReimbursementExcelVO {

    @ExcelProperty("报销/付款单号")
    private String bizCode;

    @ExcelProperty("合同单号")
    private String contractNos;

    @ExcelProperty("需求单号")
    private String demandOrderNos;

    @ExcelProperty("单据状态")
    private String status;

    @ExcelProperty("实际发生年度")
    private String year;

    @ExcelProperty("实际发生月度")
    private String month;

    @ExcelProperty("EHR编码")
    private String morgCode;

    @ExcelProperty("EHR名称")
    private String morgName;

    @ExcelProperty("控制层级EHR组织代码")
    private String controlEhrCd;

    @ExcelProperty("控制层级EHR组织名称")
    private String controlEhrNm;

    @ExcelProperty("预算组织编码")
    private String budgetOrgCd;

    @ExcelProperty("预算组织名称")
    private String budgetOrgNm;

    @ExcelProperty("费用科目编码")
    private String budgetSubjectCode;

    @ExcelProperty("费用科目名称")
    private String budgetSubjectName;

    @ExcelProperty("资产类型")
    private String erpAssetType;

    @ExcelProperty("资产类型名称")
    private String erpAssetTypeName;

    @ExcelProperty("项目编码")
    private String masterProjectCode;

    @ExcelProperty("项目名称")
    private String masterProjectName;

    @ExcelProperty("集团内/集团外")
    private String isInternal;

    @ExcelProperty("操作人")
    private String operator;

    @ExcelProperty("操作时间")
    private String operateTime;

    @ExcelProperty("来源系统")
    private String dataSource;

    @ExcelProperty("流程名称")
    private String processName;

    @ExcelProperty("报销金额/付款金额")
    private BigDecimal amount;
}

