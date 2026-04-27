package com.jasolar.mis.module.system.util;

import com.alibaba.excel.write.handler.SheetWriteHandler;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.holder.WriteWorkbookHolder;
import org.apache.poi.hssf.usermodel.HSSFDataValidation;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddressList;

/**
 * 为「是否变更」列添加下拉框：不变、新增、修改。
 * 采用与框架 SelectSheetWriteHandler 相同方式：隐藏 Sheet 存选项 + 命名引用 + 公式列表约束，
 * 避免 createExplicitListConstraint 在 xlsx 中下拉不显示的问题。
 * 列顺序：主键(0)、预算资产类型编码(1)、名称(2)、资产大类编码(3)、名称(4)、资产类型编码(5)、名称(6)、年份(7)、是否变更(8)。
 */
public class ChangeStatusDropdownWriteHandler implements SheetWriteHandler {

    private static final String DICT_SHEET_NAME = "dict_change_status";
    private static final String LIST_NAME = "changeStatusList";

    /** 「是否变更」列在 BudgetAssetTypeMappingExcelVO 中的索引（0-based），第 9 列 */
    private static final int CHANGE_STATUS_COLUMN_INDEX = 8;

    /** 数据行起始（0 为表头） */
    private static final int FIRST_ROW = 1;

    /** 下拉生效的最大行数（Excel 最大行 1048575，不超过即可） */
    private static final int LAST_ROW = 10000;

    private static final String[] CHANGE_STATUS_OPTIONS = new String[]{"不变", "新增", "修改"};

    @Override
    public void afterSheetCreate(WriteWorkbookHolder writeWorkbookHolder, WriteSheetHolder writeSheetHolder) {
        Workbook workbook = writeWorkbookHolder.getWorkbook();
        Sheet dataSheet = writeSheetHolder.getSheet();
        DataValidationHelper helper = dataSheet.getDataValidationHelper();

        // 1. 创建隐藏 Sheet，写入选项（与 SelectSheetWriteHandler 一致，xlsx 中下拉更可靠）
        Sheet dictSheet = workbook.createSheet(DICT_SHEET_NAME);
        for (int i = 0; i < CHANGE_STATUS_OPTIONS.length; i++) {
            Row row = dictSheet.createRow(i);
            row.createCell(0).setCellValue(CHANGE_STATUS_OPTIONS[i]);
        }
        String refFormula = DICT_SHEET_NAME + "!$A$1:$A$" + CHANGE_STATUS_OPTIONS.length;
        Name name = workbook.createName();
        name.setNameName(LIST_NAME);
        name.setRefersToFormula(refFormula);

        // 2. 用公式引用约束做数据验证
        DataValidationConstraint constraint = helper.createFormulaListConstraint(LIST_NAME);
        CellRangeAddressList rangeList = new CellRangeAddressList(FIRST_ROW, LAST_ROW, CHANGE_STATUS_COLUMN_INDEX, CHANGE_STATUS_COLUMN_INDEX);
        DataValidation validation = helper.createValidation(constraint, rangeList);
        if (validation instanceof HSSFDataValidation) {
            validation.setSuppressDropDownArrow(false);
        } else {
            validation.setSuppressDropDownArrow(false);
            validation.setShowErrorBox(true);
        }
        validation.setErrorStyle(DataValidation.ErrorStyle.STOP);
        validation.createErrorBox("提示", "请从下拉框选择：不变、新增、修改");
        dataSheet.addValidationData(validation);

        // 3. 隐藏字典 Sheet
        workbook.setSheetHidden(workbook.getSheetIndex(dictSheet), true);
    }
}
