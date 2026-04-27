package com.jasolar.mis.module.system.controller.budget.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 提交不上去单据清单 Excel 行 VO（sheet「提交不上去单据清单」）
 * 第 1 行忽略，第 2 行为表头，数据从第 3 行开始。
 */
@Data
public class SubmitFailListExcelRowVo {

    @ExcelProperty(index = 0)
    private String id;

    @ExcelProperty(index = 1)
    private String bizCode;

    @ExcelProperty(index = 2)
    private String bizType;

    @ExcelProperty(index = 3)
    private String runStatus;

    @ExcelProperty(index = 4)
    private String errorMsg;

    /** 提取得组织/项目编码 */
    @ExcelProperty(index = 5)
    private String extractedOrgOrProjectCode;

    /** 处理方法 */
    @ExcelProperty(index = 6)
    private String processMethod;

    /** 对应项目编码/组织编码 */
    @ExcelProperty(index = 7)
    private String targetProjectOrOrgCode;
}
