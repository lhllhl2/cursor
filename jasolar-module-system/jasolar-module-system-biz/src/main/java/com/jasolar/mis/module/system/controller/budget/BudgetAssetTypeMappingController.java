package com.jasolar.mis.module.system.controller.budget;

import com.jasolar.mis.framework.common.pojo.CommonResult;
import com.jasolar.mis.framework.common.pojo.PageResult;
import com.jasolar.mis.module.system.controller.budget.vo.BudgetAssetTypeMappingExcelVO;
import com.jasolar.mis.module.system.controller.budget.vo.BudgetAssetTypeMappingPageVo;
import com.jasolar.mis.module.system.controller.budget.vo.BudgetAssetTypeMappingQueryParams;
import com.jasolar.mis.module.system.controller.budget.vo.BudgetAssetTypeMappingUpdateReqVO;
import com.jasolar.mis.module.system.service.budget.BudgetAssetTypeMappingService;
import com.jasolar.mis.module.system.util.ExcelExportStyleUtil;
import com.jasolar.mis.module.system.util.ExcelExportUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 预算资产类型映射控制器
 */
@Tag(name = "预算管理 - 资产类型映射")
@RestController
@RequestMapping("/budget-asset-type-mapping")
@Slf4j
public class BudgetAssetTypeMappingController {

    @Resource
    private BudgetAssetTypeMappingService budgetAssetTypeMappingService;

    /**
     * 分页查询预算资产类型映射
     * year、changeStatus 多选精确匹配；assetTypeName、erpAssetType、assetMajorCategoryName、
     * assetMajorCategoryCode、budgetAssetTypeName、budgetAssetTypeCode 支持模糊搜索
     */
    @PostMapping("/page")
    @Operation(summary = "分页查询预算资产类型映射",
            description = "year、changeStatus 多选精确匹配；其余 6 个字段支持模糊搜索")
    public CommonResult<PageResult<BudgetAssetTypeMappingPageVo>> pageQuery(
            @RequestBody @Valid BudgetAssetTypeMappingQueryParams params) {
        log.info("分页查询预算资产类型映射，params={}", params);
        PageResult<BudgetAssetTypeMappingPageVo> result = budgetAssetTypeMappingService.pageQuery(params);
        return CommonResult.success(result);
    }

    /**
     * 导出预算资产类型映射（全量数据，动态生成 Excel）
     * 支持与分页相同的筛选条件，导出符合条件的所有记录
     *
     * @param params   查询参数（可选，不传则导出全部）
     * @param response HTTP 响应对象
     */
    @PostMapping("/export")
    @Operation(summary = "导出预算资产类型映射", description = "全量导出，支持与分页相同的筛选条件")
    public void export(@RequestBody @Valid BudgetAssetTypeMappingQueryParams params, HttpServletResponse response) throws IOException {
        log.info("开始导出预算资产类型映射，params={}", params);
        try {
            System.setProperty("java.awt.headless", "true");
            try {
                System.setProperty("sun.java2d.fontpath", "");
            } catch (Exception e) {
                // 忽略
            }

            List<BudgetAssetTypeMappingPageVo> voList = budgetAssetTypeMappingService.listAll(params);
            log.info("查询到全量数据：{} 条", voList.size());

            List<BudgetAssetTypeMappingExcelVO> excelVoList = voList.stream()
                    .map(this::convertToExcelVO)
                    .collect(Collectors.toList());

            String fileName = "预算资产类型映射导出.xlsx";
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8);
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-Disposition", "attachment; filename=" + encodedFileName);

            // 使用原生 POI：先写表头和数据，再添加下拉与隐藏列，保证 xlsx 中下拉框能正常显示（与 ExcelExportUtil.exportExcelWithDropdown 一致）
            try (XSSFWorkbook workbook = new XSSFWorkbook()) {
                XSSFSheet sheet = workbook.createSheet("预算资产类型映射");
                String[] headers = {"主键", "预算资产类型编码", "预算资产类型名称", "资产大类编码", "资产大类名称", "资产类型编码", "资产类型名称", "年份", "是否变更"};
                // 表头/内容样式：直接使用 ExcelExportStyleUtil 的 POI 样式，与 EasyExcel 表头效果一致
                XSSFCellStyle headerStyle = ExcelExportStyleUtil.createHeaderCellStyle(workbook);
                XSSFCellStyle contentStyle = ExcelExportStyleUtil.createContentCellStyle(workbook);
                XSSFRow headerRow = sheet.createRow(0);
                for (int i = 0; i < headers.length; i++) {
                    XSSFCell cell = headerRow.createCell(i);
                    cell.setCellValue(headers[i]);
                    cell.setCellStyle(headerStyle);
                }
                // 设置列宽，使表头不换行、统一单行显示
                for (int i = 0; i < headers.length; i++) {
                    sheet.setColumnWidth(i, 20 * 256);
                }
                int rowIndex = 1;
                for (BudgetAssetTypeMappingExcelVO vo : excelVoList) {
                    XSSFRow row = sheet.createRow(rowIndex++);
                    setCellValue(row.createCell(0), vo.getId());
                    setCellValue(row.createCell(1), vo.getBudgetAssetTypeCode());
                    setCellValue(row.createCell(2), vo.getBudgetAssetTypeName());
                    setCellValue(row.createCell(3), vo.getAssetMajorCategoryCode());
                    setCellValue(row.createCell(4), vo.getAssetMajorCategoryName());
                    setCellValue(row.createCell(5), vo.getErpAssetType());
                    setCellValue(row.createCell(6), vo.getAssetTypeName());
                    setCellValue(row.createCell(7), vo.getYear());
                    setCellValue(row.createCell(8), vo.getChangeStatus());
                    for (int c = 0; c < 9; c++) {
                        row.getCell(c).setCellStyle(contentStyle);
                    }
                }
                sheet.setColumnHidden(0, true);
                int lastRowIndex = excelVoList.size(); // 最后一行数据行索引（表头为 0，数据从 1 开始）
                ExcelExportUtil.addDropdownForColumn(sheet, 8, List.of("不变", "新增", "修改"), lastRowIndex);
                workbook.write(response.getOutputStream());
                response.getOutputStream().flush();
            }

            log.info("预算资产类型映射导出完成，共导出 {} 条数据", excelVoList.size());
        } catch (Exception e) {
            log.error("导出预算资产类型映射失败", e);
            throw new IOException("导出失败：" + e.getMessage(), e);
        }
    }

    /**
     * 根据主键获取单条，用于页面编辑回显（含同步的 budgetAssetTypeCode、budgetAssetTypeName、year 及手工维护字段）
     */
    @GetMapping("/get/{id}")
    @Operation(summary = "获取单条", description = "根据主键获取，用于编辑回显")
    public CommonResult<BudgetAssetTypeMappingPageVo> getById(@PathVariable("id") Long id) {
        BudgetAssetTypeMappingPageVo vo = budgetAssetTypeMappingService.getById(id);
        return CommonResult.success(vo);
    }

    /**
     * 保存手工维护字段（资产大类编码/名称、资产类型编码/名称、年份、是否变更），不修改 budgetAssetTypeCode、budgetAssetTypeName
     * 参考 EhrOrgManageRController 的 changeBudget 等，按主键更新单条
     */
    @PostMapping("/update")
    @Operation(summary = "保存手工维护字段", description = "按主键更新：资产大类编码/名称、资产类型编码/名称、年份、是否变更")
    public CommonResult<Void> updateManualFields(@RequestBody @Valid BudgetAssetTypeMappingUpdateReqVO req) {
        budgetAssetTypeMappingService.updateManualFields(req);
        return CommonResult.success(null);
    }

    /**
     * 数据同步：从 DATAINTEGRATION.VIEW_BUDGET_MEMBER_NAME_CODE 增量同步到 budget_asset_type_mapping。
     * 拉取 MEMBER_CD 以 CU205 开头的数据；表里已有（同 budget_asset_type_code + 当前年）的不修改，仅新增。
     */
    @PostMapping("/sync")
    @Operation(summary = "从视图增量同步", description = "拉取视图 MEMBER_CD 以 CU205 开头的数据，已有不修改仅新增，year 取当前年")
    public CommonResult<String> syncFromView() {
        log.info("开始从 VIEW_BUDGET_MEMBER_NAME_CODE 增量同步到 budget_asset_type_mapping");
        String result = budgetAssetTypeMappingService.syncFromView();
        return CommonResult.success(result);
    }

    /**
     * 批量导入预算资产类型映射
     * 根据「是否变更」列：不变-不处理该行，新增-插入新记录，修改-按主键更新该行
     * 请使用导出的 Excel 模板或保持表头一致（含主键列），修改时主键必填
     */
    @PostMapping("/import")
    @Operation(summary = "批量导入预算资产类型映射",
            description = "不变=跳过；新增=插入；修改=按主键更新。请使用导出模板，修改时主键必填")
    public CommonResult<String> importExcel(@RequestParam("file") MultipartFile file) {
        try {
            log.info("开始导入预算资产类型映射，文件名={}", file.getOriginalFilename());
            String result = budgetAssetTypeMappingService.importExcel(file);
            return CommonResult.success(result);
        } catch (Exception e) {
            log.error("导入预算资产类型映射失败", e);
            return CommonResult.error("500", "导入失败：" + (e.getMessage() != null ? e.getMessage() : e.toString()));
        }
    }

    private BudgetAssetTypeMappingExcelVO convertToExcelVO(BudgetAssetTypeMappingPageVo vo) {
        BudgetAssetTypeMappingExcelVO excelVO = new BudgetAssetTypeMappingExcelVO();
        excelVO.setId(vo.getId() != null ? String.valueOf(vo.getId()) : null);
        excelVO.setBudgetAssetTypeCode(vo.getBudgetAssetTypeCode());
        excelVO.setBudgetAssetTypeName(vo.getBudgetAssetTypeName());
        excelVO.setAssetMajorCategoryCode(vo.getAssetMajorCategoryCode());
        excelVO.setAssetMajorCategoryName(vo.getAssetMajorCategoryName());
        excelVO.setErpAssetType(vo.getErpAssetType());
        excelVO.setAssetTypeName(vo.getAssetTypeName());
        excelVO.setYear(vo.getYear());
        // 导出时「是否变更」列默认为空，由用户在下拉框中选择
        excelVO.setChangeStatus("");
        return excelVO;
    }

    /** 将 changeStatus 枚举值转为导出/下拉显示：不变、新增、修改 */
    private static String toChangeStatusLabel(String changeStatus) {
        if (changeStatus == null) {
            return "";
        }
        return switch (changeStatus.toUpperCase()) {
            case "UNCHANGED" -> "不变";
            case "NEW" -> "新增";
            case "MODIFY" -> "修改";
            default -> changeStatus;
        };
    }

    private static void setCellValue(XSSFCell cell, Object value) {
        if (value == null) {
            cell.setBlank();
            return;
        }
        // 主键 id 以字符串写入，避免 Excel 按数字存储导致大 Long 精度丢失，导入时主键不存在
        if (value instanceof Long l) {
            cell.setCellValue(String.valueOf(l));
            cell.setCellType(org.apache.poi.ss.usermodel.CellType.STRING);
            return;
        }
        cell.setCellValue(value.toString());
    }

}
