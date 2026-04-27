package com.jasolar.mis.module.system.controller.budget.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * EHR控制层级 Excel 导出 VO
 */
@Data
public class EhrControlLevelExcelVO {

    @ExcelProperty("EHR组织代码")
    private String ehrCd;

    @ExcelProperty("EHR组织名称")
    private String ehrNm;

    @ExcelProperty("控制层级EHR组织代码")
    private String controlEhrCd;

    @ExcelProperty("控制层级EHR组织名称")
    private String controlEhrNm;

    @ExcelProperty("预算组织编码")
    private String budgetOrgCd;

    @ExcelProperty("预算组织名称")
    private String budgetOrgNm;

    @ExcelProperty("预算层级EHR组织编码")
    private String budgetEhrCd;

    @ExcelProperty("预算层级EHR组织名称")
    private String budgetEhrNm;

    @ExcelProperty("ERP部门编码")
    private String erpDepart;
}
