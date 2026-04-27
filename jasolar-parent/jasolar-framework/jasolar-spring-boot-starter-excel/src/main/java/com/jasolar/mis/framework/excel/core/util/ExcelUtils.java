package com.jasolar.mis.framework.excel.core.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.VerticalAlignment;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.converters.longconverter.LongStringConverter;
import com.alibaba.excel.read.builder.ExcelReaderBuilder;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.excel.write.builder.ExcelWriterBuilder;
import com.alibaba.excel.write.builder.ExcelWriterSheetBuilder;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.fill.FillConfig;
import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.metadata.style.WriteFont;
import com.alibaba.excel.write.style.HorizontalCellStyleStrategy;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.jasolar.mis.framework.excel.core.convert.DictConvert;
import com.jasolar.mis.framework.excel.core.event.ValidModelBuildEventListener;
import com.jasolar.mis.framework.excel.core.handler.I18nHeaderWriteHandler;
import com.jasolar.mis.framework.excel.core.handler.SelectSheetWriteHandler;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.StrPool;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

/**
 * Excel 工具类
 *
 * @author zhaohuang
 */
public interface ExcelUtils {

    /** 内存模式的最大行数 */
    int MAX_ROW_FOR_MEMORY = 10000; // 内存模式阈值

    /**
     * 将列表以 Excel 响应给前端
     *
     * @param response 响应
     * @param filename 文件名
     * @param sheetName Excel sheet 名
     * @param head Excel head 头
     * @param data 数据列表哦
     * @param <T> 泛型，保证 head 和 data 类型的一致性
     * @throws IOException 写入失败的情况
     */
    static <T> void write(HttpServletResponse response, String filename, String sheetName, Class<T> head, List<T> data) throws IOException {
        // 输出 Excel
        // 使用内存模式避免 SXSSF 的字体库依赖（在Docker容器中可能缺少字体库）
        // 移除 LongestMatchColumnWidthStyleStrategy，因为它需要字体库来计算列宽
        EasyExcel.write(response.getOutputStream(), head)
                .autoCloseStream(false) // 不要自动关闭，交给 Servlet 自己处理
                .inMemory(true) // 使用内存模式，避免字体库依赖
                .registerWriteHandler(new SelectSheetWriteHandler(head)) // 基于固定 sheet 实现下拉框
                .registerConverter(new LongStringConverter()) // 避免 Long 类型丢失精度
                .sheet(sheetName).doWrite(data);
        setResponseHeader(response, filename);
    }

    /**
     * 根据EXCEL的固定模板填充数据
     *
     * @param response
     * @param fileName
     * @param templatePath
     * @param data
     * @param <T>
     */
    static <T> void fillTemplate(HttpServletResponse response, String fileName, String templatePath, List<T> data) throws IOException {
        fillTemplate(response, fileName, templatePath, data, null);
    }

    /**
     * 根据EXCEL的固定模板填充数据
     *
     * @param response
     * @param fileName
     * @param templatePath
     * @param data
     * @param <T>
     */
    static <T> void fillTemplate(HttpServletResponse response, String fileName, String templatePath, List<T> data,
            Map<String, Object> specialParams) throws IOException {
        try (InputStream in = ExcelUtils.class.getResourceAsStream(templatePath)) {
            setResponseHeader(response, fileName);
            // 使用内存模式避免 SXSSF 的字体库依赖（在Docker容器中可能缺少字体库）
            ExcelWriter excelWriter = EasyExcel.write(response.getOutputStream())
                    .withTemplate(in)
                    .inMemory(true) // 使用内存模式，避免字体库依赖
                    .build();
            WriteSheet writeSheet = EasyExcel.writerSheet().build();
            FillConfig fillConfig = FillConfig.builder().forceNewRow(Boolean.TRUE).build();
            excelWriter.fill(data, fillConfig, writeSheet);
            if (MapUtil.isNotEmpty(specialParams)) {
                excelWriter.fill(specialParams, writeSheet);
            }
            excelWriter.finish();
        }
    }

    /**
     * 设置返回响应头
     *
     * @param response
     * @param fileName
     * @throws UnsupportedEncodingException
     */
    static void setResponseHeader(HttpServletResponse response, String fileName) throws UnsupportedEncodingException {
        response.setHeader("Connection", "close");
        response.setHeader("Content-Type", "application/octet-stream");
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=UTF-8");
        response.setHeader("Content-disposition", "attachment;filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));
    }

    /**
     * Web导出（适用于Spring Web）
     *
     * @param response HttpServletResponse
     * @param fileName 下载文件名（无需后缀）
     * @param dataList 数据列表
     * @param sheetName 工作表名称
     * @param headClass 表头类
     */
    static <T> void export(HttpServletResponse response, String fileName, List<T> dataList, String sheetName, Class<T> headClass)
            throws IOException {
        validateParams(fileName, sheetName, headClass);
        // // 设置响应头
        // String timestamp = DateUtil.format(DateUtil.date(), DatePattern.PURE_DATETIME_PATTERN);
        // fileName = fileName + "_" + timestamp + ExcelTypeEnum.XLSX.getValue();
        // setResponseHeader(response, fileName);
        // Boolean memory = true;
        // if (dataList.size() > MAX_ROW_FOR_MEMORY) {
        // memory = Boolean.FALSE;
        // }
        // EasyExcel.write(response.getOutputStream(), headClass).inMemory(memory)
        // .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy()).registerWriteHandler(createDefaultStyleStrategy())
        // .registerConverter(new DictConvert()).autoCloseStream(true).sheet(sheetName).doWrite(dataList);

        builder(response, fileName, headClass, dataList.size() <= MAX_ROW_FOR_MEMORY, sheetName).doWrite(dataList);
    }

    /**
     * 
     * 导出数据
     * 
     * @param <T> 数据类型
     * @param response
     * @param fileName 文件名
     * @param data 数据
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    static <T> void export(HttpServletResponse response, String fileName, List<T> data) throws IOException {
        export(response, fileName, data, (Class<T>) data.get(0).getClass());
    }

    /**
     * 导出数据
     * 
     * @param <T> 数据类型
     * @param response
     * @param fileName 文件名
     * @param data 数据
     * @param headClass 表头的class
     * @throws IOException
     */
    static <T> void export(HttpServletResponse response, String fileName, List<T> data, Class<T> headClass) throws IOException {
        builder(response, fileName, headClass).sheet(0, "Sheet1").doWrite(data);
    }

    /**
     * 根据指定的表头构建{@link ExcelWriterSheetBuilder}对象
     * 
     * @param response HttpServletResponse请求
     * @param filename 导出的文件名
     * @param headClass 数据类,表字段注解
     * @param inMemory 是否使用内存模式
     * @param sheetName 使用的sheet名称
     * @return ExcelWriterSheetBuilder
     * @throws IOException
     */
    static <T> ExcelWriterSheetBuilder builder(HttpServletResponse response, String filename, Class<T> headClass, boolean inMemory,
            String sheetName) throws IOException {
        ExcelWriterBuilder builder = builder(response, filename, headClass).inMemory(inMemory);
        return builder.sheet(sheetName);
    }

    /**
     * 根据指定的表头构建{@link ExcelWriterSheetBuilder}对象. 默认使用第一个sheet,且非内存模式
     * 
     * @param response HttpServletResponse请求
     * @param filename 导出的文件名, 不包括后缀名. 会自动添加时间戳
     * @param headClass 数据类,表字段注解
     * @return ExcelWriterSheetBuilder
     * @throws IOException
     */
    static <T> ExcelWriterBuilder builder(HttpServletResponse response, String filename, Class<T> headClass) throws IOException {
        filename = newFilename(filename);
        setResponseHeader(response, filename);
        ExcelWriterBuilder builder = EasyExcel.write(response.getOutputStream(), headClass)
                // 表头国际化
                .registerWriteHandler(new I18nHeaderWriteHandler())
                // 下拉选项
                .registerWriteHandler(new SelectSheetWriteHandler(headClass))
                // 自动列宽
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                // 自定义样式
                .registerWriteHandler(createDefaultStyleStrategy())
                // 字典转换
                .registerConverter(new DictConvert())
                //
                .autoCloseStream(true);
        return builder;
    }

    /** 文件名时间戳格式 */
    static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern(DatePattern.PURE_DATETIME_PATTERN);

    /**
     * 为文件名添加时间戳
     * 
     * @param filename 原始文件名
     * @return 包括时间戳和后缀名的新文件名
     */
    static String newFilename(String filename) {
        String timestamp = LocalDateTime.now().format(DATETIME_FORMATTER);
        filename = filename + StrPool.DASHED + timestamp + ExcelTypeEnum.XLSX.getValue();
        return filename;
    }

    private static void validateParams(String fileName, String sheetName, Class<?> headClass) {
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new IllegalArgumentException("文件名不能为空");
        }
        if (sheetName == null || sheetName.trim().isEmpty()) {
            throw new IllegalArgumentException("Sheet名称不能为空");
        }
        if (headClass == null) {
            throw new IllegalArgumentException("表头类不能为空");
        }
    }

    /**
     * 创建样式策略
     */
    private static HorizontalCellStyleStrategy createDefaultStyleStrategy() {
        WriteCellStyle headStyle = createHeadStyle();
        WriteCellStyle contentStyle = createContentStyle();
        return new HorizontalCellStyleStrategy(headStyle, contentStyle);
    }

    static WriteCellStyle createHeadStyle() {
        // 表头样式
        WriteCellStyle headStyle = new WriteCellStyle();
        // 对齐方式
        headStyle.setWrapped(false);
        headStyle.setHorizontalAlignment(HorizontalAlignment.CENTER);
        headStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        headStyle.setFillForegroundColor(IndexedColors.BLUE_GREY.getIndex());
        headStyle.setFillPatternType(FillPatternType.SOLID_FOREGROUND);
        // 边框设置 - 细线边框
        headStyle.setBorderLeft(BorderStyle.THIN);
        headStyle.setBorderRight(BorderStyle.THIN);
        headStyle.setBorderTop(BorderStyle.THIN);
        headStyle.setBorderBottom(BorderStyle.THIN);
        // 字体配置
        WriteFont headFont = new WriteFont();
        headFont.setFontName("微软雅黑");
        headFont.setFontHeightInPoints((short) 10);
        headFont.setBold(true);
        headFont.setColor(IndexedColors.WHITE.getIndex());
        headStyle.setWriteFont(headFont);
        return headStyle;
    }

    static WriteCellStyle createContentStyle() {
        // 内容样式
        WriteCellStyle contentStyle = new WriteCellStyle();
        // 对齐方式
        // contentStyle.setHorizontalAlignment(HorizontalAlignment.LEFT);
        contentStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        // 边框设置
        contentStyle.setBorderLeft(BorderStyle.THIN);
        contentStyle.setBorderRight(BorderStyle.THIN);
        contentStyle.setBorderTop(BorderStyle.THIN);
        contentStyle.setBorderBottom(BorderStyle.THIN);

        // 字体配置
        WriteFont contentFont = new WriteFont();
        contentFont.setFontName("微软雅黑");
        contentFont.setFontHeightInPoints((short) 10);
        // contentFont.setColor(IndexedColors.BLACK.getIndex());
        contentStyle.setWriteFont(contentFont);
        return contentStyle;
    }

    /**
     * 读取Excel且不做数据校验. 此方法必须自己外部传入ReadListener以便接收数据
     * 
     * @param <T>
     * @param in 读取的流
     * @param headClass 数据头class
     * @param readListener 自定义的读取监听,可以为null
     * @return
     */
    static <T> ExcelReaderBuilder read(InputStream in, Class<T> headClass, @Nullable ReadListener<T> readListener) {
        ExcelReaderBuilder builder = EasyExcel.read(in, headClass, ValidModelBuildEventListener.of(headClass, null)).autoTrim(true)
                .ignoreEmptyRow(true).useDefaultListener(false);
        if (readListener != null) {
            builder.registerReadListener(readListener);
        }
        return builder;
    }

    /**
     * 读取Excel并且进行数据校验, 注意这里的校验是使用{@link Valid}进行注解校验. 此方法必须自己外部传入ReadListener以便接收数据
     * 
     * @param <T>
     * @param in 读取的流
     * @param headClass 数据头class
     * @param readListener 自定义的读取监听,可以为null
     * @return
     */
    static <T> ExcelReaderBuilder readValid(InputStream in, Class<T> headClass, @Nullable ReadListener<T> readListener) {
        ExcelReaderBuilder builder = EasyExcel.read(in, headClass, ValidModelBuildEventListener.of(headClass)).autoTrim(true)
                .ignoreEmptyRow(true).useDefaultListener(false);
        if (readListener != null) {
            builder.registerReadListener(readListener);
        }
        return builder;
    }

    /**
     * 读取第一个Sheet的数据且不做数据校验
     * 
     * @param <T>
     * @param in 读取的流
     * @param headClass 数据头class
     * @param readListener 自定义的读取监听,可以为null
     */
    static <T> List<T> readSync(InputStream in, Class<T> headClass, @Nullable ReadListener<T> readListener) {
        return read(in, headClass, readListener).sheet(0).doReadSync();
    }

    /**
     * 读取第一个Sheet的数据且不做数据校验
     * 
     * @param <T>
     * @param in 读取的流
     * @param headClass 数据头class
     */
    static <T> List<T> readSync(InputStream in, Class<T> headClass) {
        return read(in, headClass, null).sheet(0).doReadSync();
    }

    /**
     * 读取第一个Sheet的数据并且进行数据校验, 注意这里的校验是使用{@link Valid}进行注解校验
     * 
     * @param <T>
     * @param in 读取的流
     * @param headClass 数据头class
     * @param readListener 自定义的读取监听,可以为null
     */
    static <T> List<T> readValidSync(InputStream in, Class<T> headClass, @Nullable ReadListener<T> readListener) {
        return readValid(in, headClass, readListener).sheet(0).doReadSync();
    }

    /**
     * 读取第一个Sheet的数据并且进行数据校验, 注意这里的校验是使用{@link Valid}进行注解校验
     * 
     * @param <T>
     * @param in 读取的流
     * @param headClass 数据头class
     */
    static <T> List<T> readValidSync(InputStream in, Class<T> headClass) {
        return readValid(in, headClass, null).sheet(0).doReadSync();
    }

}
