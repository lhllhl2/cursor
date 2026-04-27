package com.jasolar.mis.module.system.util;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * 通用Excel导出工具类
 * 支持分段导出，避免大数据量导出时的OOM问题
 *
 * @author jasolar
 */
@Slf4j
public class ExcelExportUtil {

    /**
     * 默认分页大小
     */
    private static final int DEFAULT_PAGE_SIZE = 1000;

    /**
     * 通用Excel导出方法
     *
     * @param response HTTP响应
     * @param fileName 文件名（不包含扩展名）
     * @param exportClass 导出数据的Class类型
     * @param queryFunction 分页查询函数
     * @param converter 数据转换函数
     * @param <T> 数据库实体类型
     * @param <R> 导出数据类型
     * @throws IOException IO异常
     */
    public static <T,R> void exportExcel(
            HttpServletResponse response,
            String fileName,
            Class<R> exportClass,
            Function<PageParam, List<T>> queryFunction,
            Function<T, R> converter) throws IOException {
        
        exportExcel(response, fileName, exportClass, queryFunction, converter, DEFAULT_PAGE_SIZE);
    }

    /**
     * 通用Excel导出方法（自定义分页大小）
     *
     * @param response HTTP响应
     * @param fileName 文件名（不包含扩展名）
     * @param exportClass 导出数据的Class类型
     * @param queryFunction 分页查询函数
     * @param converter 数据转换函数
     * @param pageSize 每页大小
     * @param <T> 数据库实体类型
     * @param <R> 导出数据类型
     * @throws IOException IO异常
     */
    public static <T, R> void exportExcel(
            HttpServletResponse response,
            String fileName,
            Class<R> exportClass,
            Function<PageParam, List<T>> queryFunction,
            Function<T, R> converter,
            int pageSize) throws IOException {

        // 设置响应头
        setupResponseHeaders(response, fileName);
        
        // 设置系统属性，禁用字体功能
        System.setProperty("java.awt.headless", "true");
        
        // 使用最简化的EasyExcel配置，避免字体依赖
        try (ExcelWriter excelWriter = EasyExcel.write(response.getOutputStream(), exportClass)
                .useDefaultStyle(false) // 禁用默认样式
                .build()) {
            
            WriteSheet writeSheet = EasyExcel.writerSheet("数据").build();
            
            // 分批查询和写入，避免内存溢出
            int pageNo = 1;
            int totalBatches = 0;
            long totalRecords = 0;
            boolean hasMore = true;
            
            while (hasMore) {
                // 分页查询数据
                PageParam pageParam = new PageParam(pageNo, pageSize);

                List<T> batchList = queryFunction.apply(pageParam);

                if (CollectionUtils.isEmpty(batchList)) {
                    hasMore = false;
                    break;
                }
                
                // 转换为导出数据
                List<R> batchExportList = batchList.stream()
                        .map(converter)
                        .toList();
                
                // 写入当前批次数据
                excelWriter.write(batchExportList, writeSheet);
                
                // 统计信息
                totalRecords += batchList.size();
                totalBatches++;
                
                // 判断是否还有更多数据
                if (batchList.size() < pageSize) {
                    hasMore = false;
                } else {
                    pageNo++;
                }
                
                log.info("已处理第{}批数据，每批{}条，累计{}条", totalBatches, pageSize, totalRecords);
            }
            
            log.info("Excel导出完成，总共处理{}批数据，{}条记录", totalBatches, totalRecords);
        }
    }

    /**
     * 备用导出方法 - 使用最简化配置
     * 如果主方法仍然有问题，可以使用这个方法
     */
    public static <T, R> void exportExcelSimple(
            HttpServletResponse response,
            String fileName,
            Class<R> exportClass,
            Function<PageParam, List<T>> queryFunction,
            Function<T, R> converter) throws IOException {

        // 设置响应头
        setupResponseHeaders(response, fileName);
        
        // 强制设置系统属性
        System.setProperty("java.awt.headless", "true");
        System.setProperty("java.awt.fonts", "/dev/null");
        
        // 使用最简单的配置
        try (ExcelWriter excelWriter = EasyExcel.write(response.getOutputStream(), exportClass).build()) {
            
            WriteSheet writeSheet = EasyExcel.writerSheet("数据").build();
            
            // 分批查询和写入
            int pageNo = 1;
            int pageSize = 1000;
            boolean hasMore = true;
            
            while (hasMore) {
                PageParam pageParam = new PageParam(pageNo, pageSize);
                List<T> batchList = queryFunction.apply(pageParam);

                if (CollectionUtils.isEmpty(batchList)) {
                    break;
                }
                
                List<R> batchExportList = batchList.stream()
                        .map(converter)
                        .toList();
                
                excelWriter.write(batchExportList, writeSheet);
                
                if (batchList.size() < pageSize) {
                    hasMore = false;
                } else {
                    pageNo++;
                }
            }
        }
    }

    /**
     * 完全无字体依赖的导出方法 - 使用Apache POI
     * 当EasyExcel出现字体问题时，使用这个方法
     */
    public static <T, R> void exportExcelNoFont(
            HttpServletResponse response,
            String fileName,
            List<String> headers,
            Function<PageParam, List<T>> queryFunction,
            Function<T, List<String>> converter) throws IOException {

        log.info("开始导出Excel文件: {}", fileName);
        
        // 设置响应头
        setupResponseHeaders(response, fileName);
        
        // 强制设置系统属性
        System.setProperty("java.awt.headless", "true");
        System.setProperty("java.awt.fonts", "/dev/null");
        
        // 获取输出流
        try (org.apache.poi.xssf.usermodel.XSSFWorkbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
             java.io.OutputStream outputStream = response.getOutputStream()) {
            
            org.apache.poi.xssf.usermodel.XSSFSheet sheet = workbook.createSheet("数据");
            
            // 创建标题行
            org.apache.poi.xssf.usermodel.XSSFRow headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.size(); i++) {
                org.apache.poi.xssf.usermodel.XSSFCell cell = headerRow.createCell(i);
                cell.setCellValue(headers.get(i));
            }
            
            // 分批查询和写入
            int pageNo = 1;
            int pageSize = 1000;
            int rowIndex = 1;
            boolean hasMore = true;
            
            while (hasMore) {
                PageParam pageParam = new PageParam(pageNo, pageSize);
                List<T> batchList = queryFunction.apply(pageParam);

                if (CollectionUtils.isEmpty(batchList)) {
                    break;
                }
                
                for (T item : batchList) {
                    List<String> rowData = converter.apply(item);
                    org.apache.poi.xssf.usermodel.XSSFRow row = sheet.createRow(rowIndex++);
                    
                    for (int i = 0; i < rowData.size(); i++) {
                        org.apache.poi.xssf.usermodel.XSSFCell cell = row.createCell(i);
                        cell.setCellValue(rowData.get(i));
                    }
                }
                
                if (batchList.size() < pageSize) {
                    hasMore = false;
                } else {
                    pageNo++;
                }
                
                log.debug("已处理第{}页数据，当前行数: {}", pageNo, rowIndex);
            }
            
            log.info("开始写入Excel文件到响应流，总行数: {}", rowIndex);
            
            // 写入响应流并确保刷新
            workbook.write(outputStream);
            outputStream.flush();
            
            log.info("Excel文件导出完成，文件名: {}, 总行数: {}", fileName, rowIndex);
        } catch (Exception e) {
            log.error("Excel文件导出失败: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 支持下拉选项的Excel导出方法 - 使用Apache POI
     * @param response HTTP响应
     * @param fileName 文件名
     * @param headers 表头列表
     * @param queryFunction 分页查询函数
     * @param converter 数据转换函数
     * @param dropdownConfigs 下拉选项配置，key为列索引，value为选项列表
     * @param <T> 数据库实体类型
     * @throws IOException IO异常
     */
    public static <T> void exportExcelWithDropdown(
            HttpServletResponse response,
            String fileName,
            List<String> headers,
            Function<PageParam, List<T>> queryFunction,
            Function<T, List<String>> converter,
            Map<Integer, List<String>> dropdownConfigs) throws IOException {

        log.info("开始导出带下拉选项的Excel文件: {}", fileName);
        
        // 设置响应头
        setupResponseHeaders(response, fileName);
        
        // 强制设置系统属性
        System.setProperty("java.awt.headless", "true");
        System.setProperty("java.awt.fonts", "/dev/null");
        
        // 获取输出流
        try (org.apache.poi.xssf.usermodel.XSSFWorkbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
             java.io.OutputStream outputStream = response.getOutputStream()) {
            
            org.apache.poi.xssf.usermodel.XSSFSheet sheet = workbook.createSheet("数据");
            
            // 创建标题行
            org.apache.poi.xssf.usermodel.XSSFRow headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.size(); i++) {
                org.apache.poi.xssf.usermodel.XSSFCell cell = headerRow.createCell(i);
                cell.setCellValue(headers.get(i));
            }
            
            // 分批查询和写入
            int pageNo = 1;
            int pageSize = 1000;
            int rowIndex = 1;
            boolean hasMore = true;
            
            while (hasMore) {
                PageParam pageParam = new PageParam(pageNo, pageSize);
                List<T> batchList = queryFunction.apply(pageParam);

                if (CollectionUtils.isEmpty(batchList)) {
                    break;
                }
                
                for (T item : batchList) {
                    List<String> rowData = converter.apply(item);
                    org.apache.poi.xssf.usermodel.XSSFRow row = sheet.createRow(rowIndex++);
                    
                    for (int i = 0; i < rowData.size(); i++) {
                        org.apache.poi.xssf.usermodel.XSSFCell cell = row.createCell(i);
                        cell.setCellValue(rowData.get(i));
                    }
                }
                
                if (batchList.size() < pageSize) {
                    hasMore = false;
                } else {
                    pageNo++;
                }
                
                log.debug("已处理第{}页数据，当前行数: {}", pageNo, rowIndex);
            }
            
            // 为指定列添加下拉选项
            if (dropdownConfigs != null && !dropdownConfigs.isEmpty()) {
                addDropdownValidationSimple(sheet, dropdownConfigs, rowIndex - 1);
            }
            
            log.info("开始写入Excel文件到响应流，总行数: {}", rowIndex);
            
            // 写入响应流并确保刷新
            workbook.write(outputStream);
            outputStream.flush();
            
            log.info("Excel文件导出完成，文件名: {}, 总行数: {}", fileName, rowIndex);
        } catch (Exception e) {
            log.error("Excel文件导出失败: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 简化的下拉选项添加方法
     * @param sheet Excel工作表
     * @param dropdownConfigs 下拉选项配置
     * @param lastRowIndex 最后一行索引
     */
    private static void addDropdownValidationSimple(org.apache.poi.xssf.usermodel.XSSFSheet sheet, 
                                                  Map<Integer, List<String>> dropdownConfigs, 
                                                  int lastRowIndex) {
        try {
            for (Map.Entry<Integer, List<String>> entry : dropdownConfigs.entrySet()) {
                int columnIndex = entry.getKey();
                List<String> options = entry.getValue();
                
                if (options == null || options.isEmpty()) {
                    continue;
                }
                
                log.info("开始为第{}列添加下拉选项，选项: {}", columnIndex + 1, options);
                
                // 创建数据验证约束
                org.apache.poi.ss.usermodel.DataValidationConstraint constraint = 
                    sheet.getDataValidationHelper().createExplicitListConstraint(options.toArray(new String[0]));
                
                // 设置验证区域（从第2行开始，至少到第1000行）
                int endRow = Math.max(lastRowIndex, 1000);
                org.apache.poi.ss.util.CellRangeAddressList regions = new org.apache.poi.ss.util.CellRangeAddressList(1, endRow, columnIndex, columnIndex);
                
                // 创建数据验证
                org.apache.poi.ss.usermodel.DataValidation dataValidation = 
                    sheet.getDataValidationHelper().createValidation(constraint, regions);
                
                // 设置验证属性
                dataValidation.setShowErrorBox(true);
                dataValidation.setErrorStyle(org.apache.poi.ss.usermodel.DataValidation.ErrorStyle.STOP);
                dataValidation.createErrorBox("输入错误", "请从下拉列表中选择有效的选项");
                
                dataValidation.setShowPromptBox(true);
                dataValidation.createPromptBox("选择选项", "请从下拉列表中选择一个选项");
                
                // 应用验证
                sheet.addValidationData(dataValidation);
                
                log.info("成功为第{}列添加下拉选项，选项数量: {}, 验证区域: 行1-{}", columnIndex + 1, options.size(), endRow);
            }
        } catch (Exception e) {
            log.error("添加下拉选项验证失败: {}", e.getMessage(), e);
            e.printStackTrace();
        }
    }

    /**
     * 为单列添加下拉选项（数据写入完成后调用，确保 xlsx 中下拉框正常显示）
     *
     * @param sheet       工作表（XSSFSheet）
     * @param columnIndex 列索引（0-based）
     * @param options     选项列表，如 ["不变", "新增", "修改"]
     * @param lastRowIndex 最后一行数据行索引（0-based，表头为 0）
     */
    public static void addDropdownForColumn(org.apache.poi.xssf.usermodel.XSSFSheet sheet,
                                           int columnIndex,
                                           List<String> options,
                                           int lastRowIndex) {
        if (options == null || options.isEmpty()) {
            return;
        }
        Map<Integer, List<String>> map = Map.of(columnIndex, options);
        addDropdownValidationSimple(sheet, map, lastRowIndex);
    }

    
    /**
     * 备用导出方法 - 使用字节数组方式
     * 如果流式导出有问题，可以使用这个方法
     */
    public static <T, R> void exportExcelAsBytes(
            HttpServletResponse response,
            String fileName,
            List<String> headers,
            Function<PageParam, List<T>> queryFunction,
            Function<T, List<String>> converter) throws IOException {

        log.info("开始使用字节数组方式导出Excel文件: {}", fileName);
        
        // 设置响应头
        setupResponseHeaders(response, fileName);
        
        // 强制设置系统属性
        System.setProperty("java.awt.headless", "true");
        System.setProperty("java.awt.fonts", "/dev/null");
        
        try (org.apache.poi.xssf.usermodel.XSSFWorkbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook()) {
            org.apache.poi.xssf.usermodel.XSSFSheet sheet = workbook.createSheet("数据");
            
            // 创建标题行
            org.apache.poi.xssf.usermodel.XSSFRow headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.size(); i++) {
                org.apache.poi.xssf.usermodel.XSSFCell cell = headerRow.createCell(i);
                cell.setCellValue(headers.get(i));
            }
            
            // 分批查询和写入
            int pageNo = 1;
            int pageSize = 1000;
            int rowIndex = 1;
            boolean hasMore = true;
            
            while (hasMore) {
                PageParam pageParam = new PageParam(pageNo, pageSize);
                List<T> batchList = queryFunction.apply(pageParam);

                if (CollectionUtils.isEmpty(batchList)) {
                    break;
                }
                
                for (T item : batchList) {
                    List<String> rowData = converter.apply(item);
                    org.apache.poi.xssf.usermodel.XSSFRow row = sheet.createRow(rowIndex++);
                    
                    for (int i = 0; i < rowData.size(); i++) {
                        org.apache.poi.xssf.usermodel.XSSFCell cell = row.createCell(i);
                        cell.setCellValue(rowData.get(i));
                    }
                }
                
                if (batchList.size() < pageSize) {
                    hasMore = false;
                } else {
                    pageNo++;
                }
            }
            
            log.info("开始将Excel文件转换为字节数组，总行数: {}", rowIndex);
            
            // 将workbook转换为字节数组
            try (java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream()) {
                workbook.write(baos);
                byte[] excelBytes = baos.toByteArray();
                
                log.info("Excel文件转换完成，文件大小: {} bytes", excelBytes.length);
                
                // 设置Content-Length头
                response.setContentLength(excelBytes.length);
                
                // 直接写入字节数组
                response.getOutputStream().write(excelBytes);
                response.getOutputStream().flush();
                
                log.info("Excel文件导出完成，文件名: {}, 总行数: {}, 文件大小: {} bytes", 
                        fileName, rowIndex, excelBytes.length);
            }
        } catch (Exception e) {
            log.error("Excel文件导出失败: {}", e.getMessage(), e);
            throw e;
        }
    }


    /**
     * 测试方法：创建简单的带下拉选项的Excel
     * @param response HTTP响应
     * @param fileName 文件名
     * @throws IOException IO异常
     */
    public static void createTestExcelWithDropdown(HttpServletResponse response, String fileName) throws IOException {
        log.info("开始创建测试Excel文件: {}", fileName);
        
        // 设置响应头
        setupResponseHeaders(response, fileName);
        
        // 强制设置系统属性
        System.setProperty("java.awt.headless", "true");
        System.setProperty("java.awt.fonts", "/dev/null");
        
        try (org.apache.poi.xssf.usermodel.XSSFWorkbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
             java.io.OutputStream outputStream = response.getOutputStream()) {
            
            org.apache.poi.xssf.usermodel.XSSFSheet sheet = workbook.createSheet("测试数据");
            
            // 创建标题行
            String[] headers = {"ID", "表单名称", "操作类型", "类型", "填报频率", "状态", "是否更改"};
            org.apache.poi.xssf.usermodel.XSSFRow headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                org.apache.poi.xssf.usermodel.XSSFCell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }
            
            // 创建一些测试数据
            String[][] testData = {
                {"1", "测试报表1", "基地财报", "填报", "月", "已上架", "N"},
                {"2", "测试报表2", "合并调整", "查看", "年", "未上架", "N"},
                {"3", "测试报表3", "集团分析组", "看板", "季度", "已上架", "N"}
            };
            
            for (int i = 0; i < testData.length; i++) {
                org.apache.poi.xssf.usermodel.XSSFRow row = sheet.createRow(i + 1);
                for (int j = 0; j < testData[i].length; j++) {
                    org.apache.poi.xssf.usermodel.XSSFCell cell = row.createCell(j);
                    cell.setCellValue(testData[i][j]);
                }
            }
            
            // 配置下拉选项
            Map<Integer, List<String>> dropdownConfigs = new java.util.HashMap<>();
            dropdownConfigs.put(2, List.of("基地财报", "合并调单休", "合并调整", "合并结果", "集团分析组"));
            dropdownConfigs.put(3, List.of("填报", "查看", "看板"));
            
            // 添加下拉选项
            addDropdownValidationSimple(sheet, dropdownConfigs, 1000);
            
            // 写入文件
            workbook.write(outputStream);
            outputStream.flush();
            
            log.info("测试Excel文件创建完成，文件名: {}", fileName);
        } catch (Exception e) {
            log.error("创建测试Excel文件失败: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 设置响应头
     */
    private static void setupResponseHeaders(HttpServletResponse response, String fileName) {
        // 设置内容类型
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        
        // 构造文件名
        String fullFileName = fileName + "_" + System.currentTimeMillis() + ".xlsx";
        String encodedFileName = URLEncoder.encode(fullFileName, StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");
        
        // 设置下载相关的响应头
        response.setHeader("Content-Disposition", "attachment; filename=\"" + encodedFileName + "\"");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");
        
        // 禁用内容压缩
        response.setHeader("Content-Encoding", "identity");
        
        log.info("设置Excel导出响应头，文件名: {}, 编码后文件名: {}", fullFileName, encodedFileName);
    }

    /**
     * 分页参数
     */
    public static class PageParam {
        private int pageNo;
        private int pageSize;
        
        public PageParam(int pageNo, int pageSize) {
            this.pageNo = pageNo;
            this.pageSize = pageSize;
        }
        
        public int getPageNo() {
            return pageNo;
        }
        
        public int getPageSize() {
            return pageSize;
        }
    }

    /**
     * 分页结果
     */
    public static class PageResult<T> {
        private List<T> list;
        private long total;
        
        public PageResult(List<T> list, long total) {
            this.list = list;
            this.total = total;
        }
        
        public List<T> getList() {
            return list;
        }
        
        public long getTotal() {
            return total;
        }
    }
}
