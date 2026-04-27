package com.jasolar.mis.module.system.controller.admin.project.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.jasolar.mis.framework.excel.core.annotations.ExcelColumnSelect;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 31/12/2025 10:00
 * Version : 1.0
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectUserGroupExcelVo {

    @ExcelProperty("项目编码")
    private String prjCd;

    @ExcelProperty("项目名称")
    private String prjNm;

    @ExcelProperty("用户组名称")
    private String userGroupName;

    @ExcelProperty("是否变更")
    @ExcelColumnSelect(beanName = "projectAuthFunction")
    private String change;



}
