package com.jasolar.mis.module.system.controller.ehr.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.jasolar.mis.framework.excel.core.annotations.ExcelColumnSelect;
import com.jasolar.mis.module.system.controller.ehr.vo.converter.LongIdStringConverter;
import lombok.Data;

/**
 * ProjectInfo Excel 导出 VO
 */
@Data
public class SubjectInfoExcelVO {

    @ExcelProperty(value = "ID", converter = LongIdStringConverter.class)
    private String id;

    @ExcelProperty("费用类型编码")
    private String cust1Cd;

    @ExcelProperty("费用类型名称")
    private String cust1Nm;

    @ExcelProperty("编码")
    private String acctCd;

    @ExcelProperty("科目名称")
    private String acctNm;

    @ExcelProperty("父编码")
    private String acctParCd;

    @ExcelProperty("父科目名称")
    private String acctParNm;

    @ExcelProperty(value = "是否叶节点")
    @ExcelColumnSelect(beanName = "leafSelectFunction")
    private Boolean leaf;

    @ExcelProperty("年份")
    private String year;

    @ExcelProperty("控制层级")
    @ExcelColumnSelect(beanName = "zeroOneSelectFunction")
    private String controlLevel;

    @ExcelProperty("控制层级科目编码")
    private String controlAcctCd;

    @ExcelProperty("控制层级科目名称")
    private String controlAcctNm;

    @ExcelProperty("ERP科目编码")
    private String erpAcctCd;

    @ExcelProperty("ERP科目名称")
    private String erpAcctNm;

    @ExcelProperty("是否变更")
    @ExcelColumnSelect(beanName = "subjectChangeSelectFunction")
    private String change;


}