package com.jasolar.mis.module.system.util;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.jasolar.mis.framework.common.pojo.PageParam;
import com.jasolar.mis.framework.common.pojo.PageResult;
import com.jasolar.mis.framework.excel.core.handler.SelectSheetWriteHandler;
import com.jasolar.mis.module.system.controller.admin.project.vo.ProjectUserGroupExcelVo;
import jakarta.servlet.http.HttpServletResponse;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Function;

/**
 * 分页导出工具类
 * 提供通用的分页查询和导出功能
 */
public class PageExportUtil {

    /**
     * 默认分页大小
     */
    private static final int DEFAULT_PAGE_SIZE = 1000;

    /**
     * 通用分页处理方法
     *
     * @param searchVo 查询参数
     * @param pageFunction 分页查询函数
     * @param processor 数据处理器
     * @param <T> 查询参数类型
     * @param <R> 数据实体类型
     */
    public static <T extends PageParam, R> void processPageExport(
            T searchVo,
            Function<T, PageResult<R>> pageFunction,
            PageProcessor<R> processor) throws Exception {
        
        processPageExport(searchVo, pageFunction, processor, DEFAULT_PAGE_SIZE);
    }

    /**
     * 通用分页处理方法（自定义分页大小）
     *
     * @param searchVo 查询参数
     * @param pageFunction 分页查询函数
     * @param processor 数据处理器
     * @param pageSize 每页大小
     * @param <T> 查询参数类型
     * @param <R> 数据实体类型
     */
    public static <T extends PageParam, R> void processPageExport(
            T searchVo,
            Function<T, PageResult<R>> pageFunction,
            PageProcessor<R> processor,
            int pageSize) throws Exception {
        
        // 设置分页参数
        searchVo.setPageSize(pageSize);
        int pageIndex = 1;

        while (true) {
            // 设置当前页码
            searchVo.setPageNo(pageIndex);
            
            // 执行分页查询
            PageResult<R> pageResult = pageFunction.apply(searchVo);
            
            // 处理当前页数据
            processor.process(pageResult.getList());
            
            // 如果当前页数据少于页面大小，说明已经是最后一页
            if (pageResult.getList().size() < pageSize) {
                break;
            }
            
            // 处理下一页
            pageIndex++;
        }
    }

    /**
     * 通用Excel导出方法
     *
     * @param searchVo 查询参数
     * @param pageFunction 分页查询函数
     * @param response HTTP响应对象
     * @param fileName 文件名
     * @param exportClass 导出数据的Class类型
     * @param converter 数据转换函数
     * @param <T> 查询参数类型
     * @param <R> 数据库实体类型
     * @param <E> 导出数据类型
     */
    public static <T extends PageParam, R, E> void exportExcel(
            T searchVo,
            Function<T, PageResult<R>> pageFunction,
            HttpServletResponse response,
            String fileName,
            Class<E> exportClass,
            Function<R, E> converter) throws Exception {
        
        // 禁用字体管理，避免在缺少字体库的容器环境中报错
        // 这不会影响 Excel 导出功能，只是不使用字体来计算列宽
        System.setProperty("java.awt.headless", "true");
        try {
            // 尝试禁用字体管理器的初始化
            System.setProperty("sun.java2d.fontpath", "");
        } catch (Exception e) {
            // 忽略设置失败
        }
        
        // 设置响应头
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8);
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-Disposition", "attachment; filename=" + encodedFileName);
        
        // 创建Excel写入器，使用内存模式避免 SXSSF 的自动列宽计算（需要字体库）
        // 表头浅灰+微软雅黑统一样式；对于大数据量，可以后续优化为流式写入，但需要先解决字体库问题
        ExcelWriter excelWriter = EasyExcel.write(response.getOutputStream(), exportClass)
                .inMemory(true) // 使用内存模式，避免 SXSSF 的字体依赖
                .registerWriteHandler(ExcelExportStyleUtil.createDefaultStyleStrategy())
                .registerWriteHandler(new SelectSheetWriteHandler(exportClass)) // 注册下拉框处理器
                .build();
        WriteSheet writeSheet = EasyExcel.writerSheet("数据").build();
        
        try {
            // 使用通用分页处理方法
            processPageExport(searchVo, pageFunction, dataList -> {
                if (!dataList.isEmpty()) {
                    // 转换为Excel VO
                    List<E> excelData = dataList.stream().map(converter).toList();
                    
                    // 写入当前批次数据
                    excelWriter.write(excelData, writeSheet);
                }
            });
        } finally {
            // 关闭写入器
            if (excelWriter != null) {
                excelWriter.finish();
            }
        }
    }


    public static void writeEmptyExcel(HttpServletResponse response) throws Exception {
        // 设置响应头
        String encodedFileName = java.net.URLEncoder.encode("项目.xlsx", StandardCharsets.UTF_8);
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-Disposition", "attachment; filename=" + encodedFileName);

        // 使用EasyExcel写入一个空的数据集，但包含表头（统一样式：表头浅灰+微软雅黑）
        EasyExcel.write(response.getOutputStream(), ProjectUserGroupExcelVo.class)
                .inMemory(true)
                .registerWriteHandler(ExcelExportStyleUtil.createDefaultStyleStrategy())
                .sheet("数据")
                .doWrite(java.util.Collections.emptyList());
    }

    /**
     * 页面处理器接口
     *
     * @param <T> 数据类型
     */
    @FunctionalInterface
    public interface PageProcessor<T> {
        /**
         * 处理页面数据
         *
         * @param dataList 数据列表
         * @throws Exception 处理异常
         */
        void process(List<T> dataList) throws Exception;
    }
}