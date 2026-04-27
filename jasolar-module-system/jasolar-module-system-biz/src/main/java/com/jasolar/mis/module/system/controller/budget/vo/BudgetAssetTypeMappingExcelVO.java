package com.jasolar.mis.module.system.controller.budget.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.jasolar.mis.module.system.controller.ehr.vo.converter.LongIdStringConverter;
import lombok.Data;

/**
 * 预算资产类型映射 Excel 导出/导入 VO
 * 主键以字符串读写，避免 Excel 数字精度丢失导致导入时「主键不存在」。
 */
@Data
public class BudgetAssetTypeMappingExcelVO {

    @ExcelProperty(value = "主键", converter = LongIdStringConverter.class)
    private String id;

    @ExcelProperty("预算资产类型编码")
    private String budgetAssetTypeCode;

    @ExcelProperty("预算资产类型名称")
    private String budgetAssetTypeName;

    @ExcelProperty("资产大类编码")
    private String assetMajorCategoryCode;

    @ExcelProperty("资产大类名称")
    private String assetMajorCategoryName;

    @ExcelProperty("资产类型编码")
    private String erpAssetType;

    @ExcelProperty("资产类型名称")
    private String assetTypeName;

    @ExcelProperty("年份")
    private String year;

    @ExcelProperty("是否变更")
    private String changeStatus;
}
