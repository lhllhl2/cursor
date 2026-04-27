package com.jasolar.mis.module.system.util;

import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.metadata.style.WriteFont;
import com.alibaba.excel.write.style.HorizontalCellStyleStrategy;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Excel 导出统一样式：表头浅灰色背景 + 微软雅黑，内容微软雅黑。
 * 供所有预算/配置相关 Excel 下载接口复用。
 * 使用原生 POI 写 Excel 时请用 createHeaderCellStyle/createContentCellStyle，与 EasyExcel 策略视觉一致。
 */
public final class ExcelExportStyleUtil {

    private ExcelExportStyleUtil() {
    }

    /**
     * POI 表头样式：与 createDefaultStyleStrategy 的表头完全一致——浅灰底、微软雅黑 10pt 加粗黑色、水平垂直居中、四边细边框、不换行。
     */
    public static XSSFCellStyle createHeaderCellStyle(XSSFWorkbook workbook) {
        XSSFCellStyle style = workbook.createCellStyle();
        style.setWrapText(false);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        XSSFFont font = workbook.createFont();
        font.setFontName("微软雅黑");
        font.setFontHeightInPoints((short) 10);
        font.setBold(true);
        font.setColor(IndexedColors.BLACK.getIndex());
        style.setFont(font);
        return style;
    }

    /**
     * POI 内容样式：与 createDefaultStyleStrategy 的内容完全一致——微软雅黑 10pt、垂直居中、四边细边框。
     */
    public static XSSFCellStyle createContentCellStyle(XSSFWorkbook workbook) {
        XSSFCellStyle style = workbook.createCellStyle();
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        XSSFFont font = workbook.createFont();
        font.setFontName("微软雅黑");
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);
        return style;
    }

    /**
     * 创建默认导出样式策略：表头浅灰背景、微软雅黑加粗黑色字；内容微软雅黑、细边框。
     */
    public static HorizontalCellStyleStrategy createDefaultStyleStrategy() {
        WriteCellStyle headStyle = new WriteCellStyle();
        headStyle.setHorizontalAlignment(HorizontalAlignment.CENTER);
        headStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        headStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headStyle.setFillPatternType(FillPatternType.SOLID_FOREGROUND);
        headStyle.setBorderLeft(BorderStyle.THIN);
        headStyle.setBorderRight(BorderStyle.THIN);
        headStyle.setBorderTop(BorderStyle.THIN);
        headStyle.setBorderBottom(BorderStyle.THIN);
        WriteFont headFont = new WriteFont();
        headFont.setFontName("微软雅黑");
        headFont.setFontHeightInPoints((short) 10);
        headFont.setBold(true);
        headFont.setColor(IndexedColors.BLACK.getIndex());
        headStyle.setWriteFont(headFont);

        WriteCellStyle contentStyle = new WriteCellStyle();
        contentStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        contentStyle.setBorderLeft(BorderStyle.THIN);
        contentStyle.setBorderRight(BorderStyle.THIN);
        contentStyle.setBorderTop(BorderStyle.THIN);
        contentStyle.setBorderBottom(BorderStyle.THIN);
        WriteFont contentFont = new WriteFont();
        contentFont.setFontName("微软雅黑");
        contentFont.setFontHeightInPoints((short) 10);
        contentStyle.setWriteFont(contentFont);

        return new HorizontalCellStyleStrategy(headStyle, contentStyle);
    }
}
