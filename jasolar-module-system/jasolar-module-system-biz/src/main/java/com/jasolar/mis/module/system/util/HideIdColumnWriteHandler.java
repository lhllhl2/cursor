package com.jasolar.mis.module.system.util;

import com.alibaba.excel.write.handler.SheetWriteHandler;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.holder.WriteWorkbookHolder;
import org.apache.poi.ss.usermodel.Sheet;

/**
 * 隐藏 Excel 第 1 列（主键 id），不展示给用户，导入时仍可读取。
 * 用于预算资产类型映射导出。
 */
public class HideIdColumnWriteHandler implements SheetWriteHandler {

    private static final int ID_COLUMN_INDEX = 0;

    @Override
    public void afterSheetCreate(WriteWorkbookHolder writeWorkbookHolder, WriteSheetHolder writeSheetHolder) {
        Sheet sheet = writeSheetHolder.getSheet();
        sheet.setColumnHidden(ID_COLUMN_INDEX, true);
    }
}
