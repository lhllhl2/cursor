package com.jasolar.mis.module.system.controller.ehr.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.jasolar.mis.framework.excel.core.annotations.ExcelColumnSelect;
import lombok.Data;
/**
 * EhrOrgManageR Excel 导出 VO
 */
@Data
public class EhrOrgManageRExcelVO {

    @ExcelProperty("ID")
    private String id;

    @ExcelProperty("EHR组织编码")
    private String ehrCd;

    @ExcelProperty("EHR组织名称")
    private String ehrNm;

    @ExcelProperty("EHR组织父级编码")
    private String ehrParCd;

    @ExcelProperty("EHR组织父级名称")
    private String ehrParNm;

    @ExcelProperty("管理组织编码")
    private String orgCd;

    @ExcelProperty("管理组织名称")
    private String orgNm;

    @ExcelProperty("控制层级")
    @ExcelColumnSelect(beanName = "zeroOneSelectFunction")
    private String controlLevel;

    @ExcelProperty("编制层级")
    @ExcelColumnSelect(beanName = "zeroOneSelectFunction")
    private String bzLevel;

    @ExcelProperty("ERP部门")
    private String erpDepart;

    @ExcelProperty("年份")
    private String year;

    @ExcelProperty("是否变更")
    @ExcelColumnSelect(beanName = "changeSelectFunction")
    private String change;
}