package com.jasolar.mis.framework.excel.core.handler;

import java.util.List;

import org.apache.poi.ss.usermodel.Row;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.metadata.Head;
import com.alibaba.excel.write.handler.CellWriteHandler;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.holder.WriteTableHolder;
import com.jasolar.mis.framework.i18n.I18nUtils;

/**
 * 用于表头国际化. 注意因为存在国际化, 所以在给对象添加{@link ExcelProperty}注解时,必须指定index参数用于按照顺序导入
 * 
 * @author galuo
 * @date 2025-04-01 14:25
 *
 */
public class I18nHeaderWriteHandler implements CellWriteHandler {

    @Override
    public void beforeCellCreate(WriteSheetHolder writeSheetHolder, WriteTableHolder writeTableHolder, Row row, Head head,
            Integer columnIndex, Integer relativeRowIndex, Boolean isHead) {
        if (!isHead || head == null || relativeRowIndex != 0) {
            return;
        }

        List<String> heads = head.getHeadNameList();
        if (heads == null || heads.isEmpty()) {
            return;
        }
        head.setHeadNameList(toI18nHeadNames(heads));
    }

    /**
     * 转换为国际化的表头字段名
     * 
     * @param heads 配置的表头字段名
     * @return
     */
    public static List<String> toI18nHeadNames(List<String> heads) {
        return heads.stream().map(I18nUtils::getMessage).toList();
    }

}
