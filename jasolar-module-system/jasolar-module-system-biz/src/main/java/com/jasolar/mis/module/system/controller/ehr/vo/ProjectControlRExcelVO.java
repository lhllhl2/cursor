package com.jasolar.mis.module.system.controller.ehr.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.jasolar.mis.framework.excel.core.annotations.ExcelColumnSelect;
import lombok.Data;

/**
 * ProjectControlR Excel 导出 VO
 */
@Data
public class ProjectControlRExcelVO {

    @ExcelProperty("ID")
    private String id;

    @ExcelProperty("项目编码")
    private String prjCd;

    @ExcelProperty("项目名称")
    private String prjNm;

    @ExcelProperty("父项目编码")
    private String parCd;

    @ExcelProperty("父项目名称")
    private String parNm;

    @ExcelProperty("是否为叶节点")
    @ExcelColumnSelect(beanName = "leafSelectFunction")
    private Boolean leaf;

    @ExcelProperty("控制层级")
    @ExcelColumnSelect(beanName = "zeroOneSelectFunction")
    private String controlLevel;

    @ExcelProperty("年份")
    private String year;

    @ExcelProperty("是否变更")
    @ExcelColumnSelect(beanName = "projectChangeSelectFunction")
    private String change;

}