package com.jasolar.mis.module.system.controller.budget.query;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.jasolar.mis.framework.common.pojo.CommonResult;
import com.jasolar.mis.framework.common.pojo.PageResult;
import com.jasolar.mis.module.system.controller.budget.vo.BudgetAssetBalanceQueryParams;
import com.jasolar.mis.module.system.controller.budget.vo.BudgetAssetBalanceExcelVO;
import com.jasolar.mis.module.system.controller.budget.vo.BudgetBalanceQueryParams;
import com.jasolar.mis.module.system.controller.budget.vo.BudgetAmountVo;
import com.jasolar.mis.module.system.controller.budget.vo.BudgetBalanceExcelVO;
import com.jasolar.mis.module.system.controller.budget.vo.BudgetBalanceVo;
import com.jasolar.mis.module.system.controller.budget.vo.BudgetClaimStateVo;
import com.jasolar.mis.module.system.controller.budget.vo.BudgetProjectBalanceQueryParams;
import com.jasolar.mis.module.system.controller.budget.vo.BudgetProjectInvestmentExcelVO;
import com.jasolar.mis.module.system.controller.budget.vo.BudgetProjectPaymentExcelVO;
import com.jasolar.mis.module.system.controller.budget.vo.PaymentStatusExcelVO;
import com.jasolar.mis.module.system.controller.budget.vo.PaymentStatusQueryParams;
import com.jasolar.mis.module.system.service.budget.query.BudgetBalanceQueryService;
import com.jasolar.mis.module.system.util.ExcelExportStyleUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Description: 预算余额查询控制器（需要登录）
 * Author : Auto Generated
 * Date : 2025-01-XX
 * Version : 1.0
 */
@RestController
@RequestMapping(value = "/query")
@Slf4j
@Api(tags = "预算余额查询（需要登录）")
public class BudgetBalanceController {

    @Resource
    private BudgetBalanceQueryService budgetBalanceQueryService;

    /**
     * 部门费用可用预算查询
     * 
     * @param params 查询参数
     * @return 查询结果
     */
    @PostMapping("/queryDeptBudgetBalance")
    @ApiOperation("部门费用可用预算查询")
    public CommonResult<PageResult<BudgetBalanceVo>> queryDeptBudgetBalance(@RequestBody @Valid BudgetBalanceQueryParams params) {
        log.info("开始处理部门费用可用预算查询，params={}", params);
        PageResult<BudgetBalanceVo> result = budgetBalanceQueryService.queryDeptBudgetBalance(params);
        return CommonResult.success(result);
    }

    /**
     * 导出部门费用可用预算（动态生成Excel）
     * 
     * @param params 查询参数（支持原有搜索条件）
     * @param response HTTP响应对象
     * @throws IOException 导出异常
     */
    @PostMapping("/exportDeptBudgetBalance")
    @ApiOperation("导出部门费用可用预算")
    public void exportDeptBudgetBalance(@RequestBody @Valid BudgetBalanceQueryParams params, HttpServletResponse response) throws IOException {
        log.info("开始导出部门费用可用预算，params={}", params);
        try {
            // 设置headless模式，避免Docker容器中缺少字体库导致的错误
            System.setProperty("java.awt.headless", "true");
            try {
                System.setProperty("sun.java2d.fontpath", "");
            } catch (Exception e) {
                // 忽略设置失败
            }
            
            // 查询全量数据（支持原有搜索条件，但不分页）
            List<BudgetBalanceVo> voList = budgetBalanceQueryService.queryDeptBudgetBalanceAll(params);
            log.info("查询到全量数据：{} 条", voList.size());
            
            // 转换为ExcelVO
            List<BudgetBalanceExcelVO> excelVoList = voList.stream()
                    .map(this::convertToExcelVO)
                    .collect(Collectors.toList());
            
            // 设置响应头
            String fileName = "部门费用可用预算.xlsx";
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8);
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-Disposition", "attachment; filename=" + encodedFileName);
            
            // 直接使用 EasyExcel API，确保 inMemory(true) 生效，避免字体库依赖；表头浅灰+微软雅黑统一样式
            ExcelWriter excelWriter = EasyExcel.write(response.getOutputStream(), BudgetBalanceExcelVO.class)
                    .inMemory(true) // 使用内存模式，避免 SXSSF 的字体依赖
                    .autoCloseStream(false) // 不要自动关闭，交给 Servlet 自己处理
                    .registerWriteHandler(ExcelExportStyleUtil.createDefaultStyleStrategy())
                    .build();
            
            WriteSheet writeSheet = EasyExcel.writerSheet("部门费用可用预算").build();
            excelWriter.write(excelVoList, writeSheet);
            excelWriter.finish();
            
            log.info("部门费用可用预算导出完成，共导出 {} 条数据", excelVoList.size());
        } catch (Exception e) {
            log.error("导出部门费用可用预算失败", e);
            throw new IOException("导出失败：" + e.getMessage(), e);
        }
    }

    /**
     * 将BudgetBalanceVo转换为BudgetBalanceExcelVO
     */
    private BudgetBalanceExcelVO convertToExcelVO(BudgetBalanceVo vo) {
        BudgetBalanceExcelVO excelVO = new BudgetBalanceExcelVO();
        
        // 基本信息
        excelVO.setEhrCode(vo.getEhrCode());
        excelVO.setEhrName(vo.getEhrName());
        excelVO.setControlCust1Cd(vo.getControlCust1Cd());
        excelVO.setControlCust1Name(vo.getControlCust1Name());
        excelVO.setSubjectCode(vo.getSubjectCode());
        excelVO.setSubjectName(vo.getSubjectName());
        
        // 去年使用预算数
        excelVO.setLastYearUsedBudget(vo.getLastYearUsedBudget());
        
        // Q1季度
        BudgetAmountVo q1 = vo.getQ1();
        if (q1 != null) {
            excelVO.setQ1AmountYearTotal(q1.getAmountYearTotal());
            excelVO.setQ1AmountAdj(q1.getAmountAdj());
            excelVO.setQ1TotalBudget(q1.getAmountTotal());
            excelVO.setQ1AmountFrozen(q1.getAmountFrozen());
            excelVO.setQ1AmountOccupied(q1.getAmountOccupied());
            excelVO.setQ1AmountActual(q1.getAmountActual());
            excelVO.setQ1AmountActualApproved(q1.getAmountActualApproved());
            excelVO.setQ1AmountAvailable(q1.getAmountAvailable());
        }
        
        // Q2季度
        BudgetAmountVo q2 = vo.getQ2();
        if (q2 != null) {
            excelVO.setQ2AmountYearTotal(q2.getAmountYearTotal());
            excelVO.setQ2AmountAdj(q2.getAmountAdj());
            excelVO.setQ2TotalBudget(q2.getAmountTotal());
            excelVO.setQ2AmountFrozen(q2.getAmountFrozen());
            excelVO.setQ2AmountOccupied(q2.getAmountOccupied());
            excelVO.setQ2AmountActual(q2.getAmountActual());
            excelVO.setQ2AmountActualApproved(q2.getAmountActualApproved());
            excelVO.setQ2AmountAvailable(q2.getAmountAvailable());
        }
        
        // Q3季度
        BudgetAmountVo q3 = vo.getQ3();
        if (q3 != null) {
            excelVO.setQ3AmountYearTotal(q3.getAmountYearTotal());
            excelVO.setQ3AmountAdj(q3.getAmountAdj());
            excelVO.setQ3TotalBudget(q3.getAmountTotal());
            excelVO.setQ3AmountFrozen(q3.getAmountFrozen());
            excelVO.setQ3AmountOccupied(q3.getAmountOccupied());
            excelVO.setQ3AmountActual(q3.getAmountActual());
            excelVO.setQ3AmountActualApproved(q3.getAmountActualApproved());
            excelVO.setQ3AmountAvailable(q3.getAmountAvailable());
        }
        
        // Q4季度
        BudgetAmountVo q4 = vo.getQ4();
        if (q4 != null) {
            excelVO.setQ4AmountYearTotal(q4.getAmountYearTotal());
            excelVO.setQ4AmountAdj(q4.getAmountAdj());
            excelVO.setQ4TotalBudget(q4.getAmountTotal());
            excelVO.setQ4AmountFrozen(q4.getAmountFrozen());
            excelVO.setQ4AmountOccupied(q4.getAmountOccupied());
            excelVO.setQ4AmountActual(q4.getAmountActual());
            excelVO.setQ4AmountActualApproved(q4.getAmountActualApproved());
            excelVO.setQ4AmountAvailable(q4.getAmountAvailable());
        }
        
        return excelVO;
    }

    /**
     * 部门资产可用预算查询
     * 
     * @param params 查询参数
     * @return 查询结果
     */
    @PostMapping("/queryDeptAssetBudgetBalance")
    @ApiOperation("部门资产可用预算查询")
    public CommonResult<PageResult<BudgetBalanceVo>> queryDeptAssetBudgetBalance(@RequestBody @Valid BudgetAssetBalanceQueryParams params) {
        log.info("开始处理部门资产可用预算查询，params={}", params);
        PageResult<BudgetBalanceVo> result = budgetBalanceQueryService.queryDeptAssetBudgetBalance(params);
        return CommonResult.success(result);
    }

    /**
     * 导出部门资产可用预算（动态生成Excel）
     * 
     * @param params 查询参数（支持原有搜索条件，budgetType为PURCHASE时导出采购额，为PAYMENT时导出付款额）
     * @param response HTTP响应对象
     * @throws IOException 导出异常
     */
    @PostMapping("/exportDeptAssetBudgetBalance")
    @ApiOperation("导出部门资产可用预算")
    public void exportDeptAssetBudgetBalance(@RequestBody @Valid BudgetAssetBalanceQueryParams params, HttpServletResponse response) throws IOException {
        log.info("开始导出部门资产可用预算，params={}", params);
        try {
            // 设置headless模式，避免Docker容器中缺少字体库导致的错误
            System.setProperty("java.awt.headless", "true");
            try {
                System.setProperty("sun.java2d.fontpath", "");
            } catch (Exception e) {
                // 忽略设置失败
            }
            
            // 查询全量数据（支持原有搜索条件，但不分页）
            List<BudgetBalanceVo> voList = budgetBalanceQueryService.queryDeptAssetBudgetBalanceAll(params);
            log.info("查询到全量数据：{} 条", voList.size());
            
            // 转换为ExcelVO
            List<BudgetAssetBalanceExcelVO> excelVoList = voList.stream()
                    .map(vo -> convertToAssetBalanceExcelVO(vo))
                    .collect(Collectors.toList());
            
            // 根据budgetType确定文件名
            String budgetTypeName = "PURCHASE".equalsIgnoreCase(params.getBudgetType()) ? "采购额" : "付款额";
            String fileName = "部门资产可用预算(" + budgetTypeName + ").xlsx";
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8);
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-Disposition", "attachment; filename=" + encodedFileName);
            
            // 直接使用 EasyExcel API，确保 inMemory(true) 生效，避免字体库依赖；表头浅灰+微软雅黑统一样式
            ExcelWriter excelWriter = EasyExcel.write(response.getOutputStream(), BudgetAssetBalanceExcelVO.class)
                    .inMemory(true) // 使用内存模式，避免 SXSSF 的字体依赖
                    .autoCloseStream(false) // 不要自动关闭，交给 Servlet 自己处理
                    .registerWriteHandler(ExcelExportStyleUtil.createDefaultStyleStrategy())
                    .build();
            
            WriteSheet writeSheet = EasyExcel.writerSheet("部门资产可用预算").build();
            excelWriter.write(excelVoList, writeSheet);
            excelWriter.finish();
            
            log.info("部门资产可用预算导出完成，共导出 {} 条数据，类型：{}", excelVoList.size(), budgetTypeName);
        } catch (Exception e) {
            log.error("导出部门资产可用预算失败", e);
            throw new IOException("导出失败：" + e.getMessage(), e);
        }
    }

    /**
     * 将BudgetBalanceVo转换为BudgetAssetBalanceExcelVO
     */
    private BudgetAssetBalanceExcelVO convertToAssetBalanceExcelVO(BudgetBalanceVo vo) {
        BudgetAssetBalanceExcelVO excelVO = new BudgetAssetBalanceExcelVO();
        
        // 基本信息
        excelVO.setEhrCode(vo.getEhrCode());
        excelVO.setEhrName(vo.getEhrName());
        excelVO.setErpAssetType(vo.getErpAssetType());
        excelVO.setErpAssetTypeName(vo.getErpAssetTypeName());
        
        // 去年使用预算数
        excelVO.setLastYearUsedBudget(vo.getLastYearUsedBudget());
        
        // Q1季度
        BudgetAmountVo q1 = vo.getQ1();
        if (q1 != null) {
            excelVO.setQ1AmountYearTotal(q1.getAmountYearTotal());
            excelVO.setQ1AmountAdj(q1.getAmountAdj());
            excelVO.setQ1TotalBudget(q1.getAmountTotal());
            excelVO.setQ1AmountFrozen(q1.getAmountFrozen());
            excelVO.setQ1AmountOccupied(q1.getAmountOccupied());
            excelVO.setQ1AmountActual(q1.getAmountActual());
            excelVO.setQ1AmountActualApproved(q1.getAmountActualApproved());
            excelVO.setQ1AmountAvailable(q1.getAmountAvailable());
        }
        
        // Q2季度
        BudgetAmountVo q2 = vo.getQ2();
        if (q2 != null) {
            excelVO.setQ2AmountYearTotal(q2.getAmountYearTotal());
            excelVO.setQ2AmountAdj(q2.getAmountAdj());
            excelVO.setQ2TotalBudget(q2.getAmountTotal());
            excelVO.setQ2AmountFrozen(q2.getAmountFrozen());
            excelVO.setQ2AmountOccupied(q2.getAmountOccupied());
            excelVO.setQ2AmountActual(q2.getAmountActual());
            excelVO.setQ2AmountActualApproved(q2.getAmountActualApproved());
            excelVO.setQ2AmountAvailable(q2.getAmountAvailable());
        }
        
        // Q3季度
        BudgetAmountVo q3 = vo.getQ3();
        if (q3 != null) {
            excelVO.setQ3AmountYearTotal(q3.getAmountYearTotal());
            excelVO.setQ3AmountAdj(q3.getAmountAdj());
            excelVO.setQ3TotalBudget(q3.getAmountTotal());
            excelVO.setQ3AmountFrozen(q3.getAmountFrozen());
            excelVO.setQ3AmountOccupied(q3.getAmountOccupied());
            excelVO.setQ3AmountActual(q3.getAmountActual());
            excelVO.setQ3AmountActualApproved(q3.getAmountActualApproved());
            excelVO.setQ3AmountAvailable(q3.getAmountAvailable());
        }
        
        // Q4季度
        BudgetAmountVo q4 = vo.getQ4();
        if (q4 != null) {
            excelVO.setQ4AmountYearTotal(q4.getAmountYearTotal());
            excelVO.setQ4AmountAdj(q4.getAmountAdj());
            excelVO.setQ4TotalBudget(q4.getAmountTotal());
            excelVO.setQ4AmountFrozen(q4.getAmountFrozen());
            excelVO.setQ4AmountOccupied(q4.getAmountOccupied());
            excelVO.setQ4AmountActual(q4.getAmountActual());
            excelVO.setQ4AmountActualApproved(q4.getAmountActualApproved());
            excelVO.setQ4AmountAvailable(q4.getAmountAvailable());
        }
        
        return excelVO;
    }

    /**
     * 项目可用预算查询
     * 
     * @param params 查询参数
     * @return 查询结果
     */
    @PostMapping("/queryProjectBudgetBalance")
    @ApiOperation("项目可用预算查询")
    public CommonResult<PageResult<BudgetBalanceVo>> queryProjectBudgetBalance(@RequestBody @Valid BudgetProjectBalanceQueryParams params) {
        log.info("开始处理项目可用预算查询，params={}", params);
        PageResult<BudgetBalanceVo> result = budgetBalanceQueryService.queryProjectBudgetBalance(params);
        return CommonResult.success(result);
    }

    /**
     * 导出项目可用预算（动态生成Excel）
     * 
     * @param params 查询参数（支持原有搜索条件，budgetType为TOTALINVESTMENT时导出投资额，为PAYMENT时导出付款额）
     * @param response HTTP响应对象
     * @throws IOException 导出异常
     */
    @PostMapping("/exportProjectBudgetBalance")
    @ApiOperation("导出项目可用预算")
    public void exportProjectBudgetBalance(@RequestBody @Valid BudgetProjectBalanceQueryParams params, HttpServletResponse response) throws IOException {
        log.info("开始导出项目可用预算，params={}", params);
        try {
            // 设置headless模式，避免Docker容器中缺少字体库导致的错误
            System.setProperty("java.awt.headless", "true");
            try {
                System.setProperty("sun.java2d.fontpath", "");
            } catch (Exception e) {
                // 忽略设置失败
            }
            
            // 查询全量数据（支持原有搜索条件，但不分页）
            List<BudgetBalanceVo> voList = budgetBalanceQueryService.queryProjectBudgetBalanceAll(params);
            log.info("查询到全量数据：{} 条", voList.size());
            
            // 根据budgetType选择ExcelVO类和文件名
            String budgetType = params.getBudgetType();
            String fileName;
            String sheetName;
            String budgetTypeName;
            
            if ("TOTALINVESTMENT".equalsIgnoreCase(budgetType)) {
                // 投资额：使用投资额ExcelVO
                fileName = "项目投资额导出报表.xlsx";
                sheetName = "项目投资额";
                budgetTypeName = "投资额";
                List<BudgetProjectInvestmentExcelVO> excelVoList = voList.stream()
                        .map(this::convertToInvestmentExcelVO)
                        .collect(Collectors.toList());
                
                // 设置响应头
                String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8);
                response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                response.setCharacterEncoding("utf-8");
                response.setHeader("Content-Disposition", "attachment; filename=" + encodedFileName);
                
                // 直接使用 EasyExcel API，确保 inMemory(true) 生效，避免字体库依赖；表头浅灰+微软雅黑统一样式
                ExcelWriter excelWriter = EasyExcel.write(response.getOutputStream(), BudgetProjectInvestmentExcelVO.class)
                        .inMemory(true) // 使用内存模式，避免 SXSSF 的字体依赖
                        .autoCloseStream(false) // 不要自动关闭，交给 Servlet 自己处理
                        .registerWriteHandler(ExcelExportStyleUtil.createDefaultStyleStrategy())
                        .build();
                
                WriteSheet writeSheet = EasyExcel.writerSheet(sheetName).build();
                excelWriter.write(excelVoList, writeSheet);
                excelWriter.finish();
                
                log.info("项目可用预算导出完成，共导出 {} 条数据，类型：{}", excelVoList.size(), budgetTypeName);
            } else if ("PAYMENT".equalsIgnoreCase(budgetType)) {
                // 付款额：使用付款额ExcelVO
                fileName = "项目付款额导出报表.xlsx";
                sheetName = "项目付款额";
                budgetTypeName = "付款额";
                List<BudgetProjectPaymentExcelVO> excelVoList = voList.stream()
                        .map(this::convertToPaymentExcelVO)
                        .collect(Collectors.toList());
                
                // 设置响应头
                String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8);
                response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                response.setCharacterEncoding("utf-8");
                response.setHeader("Content-Disposition", "attachment; filename=" + encodedFileName);
                
                // 直接使用 EasyExcel API，确保 inMemory(true) 生效，避免字体库依赖；表头浅灰+微软雅黑统一样式
                ExcelWriter excelWriter = EasyExcel.write(response.getOutputStream(), BudgetProjectPaymentExcelVO.class)
                        .inMemory(true) // 使用内存模式，避免 SXSSF 的字体依赖
                        .autoCloseStream(false) // 不要自动关闭，交给 Servlet 自己处理
                        .registerWriteHandler(ExcelExportStyleUtil.createDefaultStyleStrategy())
                        .build();
                
                WriteSheet writeSheet = EasyExcel.writerSheet(sheetName).build();
                excelWriter.write(excelVoList, writeSheet);
                excelWriter.finish();
                
                log.info("项目可用预算导出完成，共导出 {} 条数据，类型：{}", excelVoList.size(), budgetTypeName);
            } else {
                throw new IllegalArgumentException("不支持的预算类型：" + budgetType + "，仅支持 TOTALINVESTMENT 或 PAYMENT");
            }
        } catch (Exception e) {
            log.error("导出项目可用预算失败", e);
            throw new IOException("导出失败：" + e.getMessage(), e);
        }
    }

    /**
     * 将BudgetBalanceVo转换为BudgetProjectInvestmentExcelVO（投资额）
     */
    private BudgetProjectInvestmentExcelVO convertToInvestmentExcelVO(BudgetBalanceVo vo) {
        BudgetProjectInvestmentExcelVO excelVO = new BudgetProjectInvestmentExcelVO();
        
        // 基本信息
        excelVO.setMorgCode(vo.getEhrCode());
        excelVO.setMorgName(vo.getEhrName());
        excelVO.setProjectCode(vo.getProjectCode());
        excelVO.setProjectName(vo.getProjectName());
        
        // 以前年度已使用金额
        excelVO.setLastYearUsedBudget(vo.getLastYearUsedBudget());
        
        // 使用total对象（投资额是Q1-Q4聚合）
        BudgetAmountVo total = vo.getTotal();
        if (total != null) {
            excelVO.setAmountYearTotal(total.getAmountYearTotal());
            excelVO.setAmountAdj(total.getAmountAdj());
            excelVO.setAmountTotal(total.getAmountTotal());
            excelVO.setAmountFrozen(total.getAmountFrozen());
            excelVO.setAmountOccupied(total.getAmountOccupied());
            excelVO.setAmountActual(total.getAmountActual());
            excelVO.setAmountActualApproved(total.getAmountActualApproved());
            excelVO.setAmountAvailable(total.getAmountAvailable());
        }
        
        return excelVO;
    }

    /**
     * 将BudgetBalanceVo转换为BudgetProjectPaymentExcelVO（付款额）
     */
    private BudgetProjectPaymentExcelVO convertToPaymentExcelVO(BudgetBalanceVo vo) {
        BudgetProjectPaymentExcelVO excelVO = new BudgetProjectPaymentExcelVO();
        
        // 基本信息
        excelVO.setMorgCode(vo.getEhrCode());
        excelVO.setMorgName(vo.getEhrName());
        excelVO.setProjectCode(vo.getProjectCode());
        excelVO.setProjectName(vo.getProjectName());
        
        // 去年使用预算数（项目付款额保持原表头）
        excelVO.setLastYearUsedBudget(vo.getLastYearUsedBudget());
        
        // 顶部汇总列（如果需要，可以从total计算，或者留空）
        BudgetAmountVo total = vo.getTotal();
        if (total != null) {
            excelVO.setTotalAmountYearTotal(total.getAmountYearTotal());
            excelVO.setTotalAmountAdj(total.getAmountAdj());
            excelVO.setTotalAmountTotal(total.getAmountTotal());
        }
        
        // Q1季度
        BudgetAmountVo q1 = vo.getQ1();
        if (q1 != null) {
            excelVO.setQ1AmountFrozen(q1.getAmountFrozen());
            excelVO.setQ1AmountOccupied(q1.getAmountOccupied());
            excelVO.setQ1AmountActual(q1.getAmountActual());
            excelVO.setQ1AmountActualApproved(q1.getAmountActualApproved());
            excelVO.setQ1AmountAvailable(q1.getAmountAvailable());
            excelVO.setQ1AmountYearTotal(q1.getAmountYearTotal());
            excelVO.setQ1AmountAdj(q1.getAmountAdj());
            excelVO.setQ1AmountTotal(q1.getAmountTotal());
        }
        
        // Q2季度
        BudgetAmountVo q2 = vo.getQ2();
        if (q2 != null) {
            excelVO.setQ2AmountFrozen(q2.getAmountFrozen());
            excelVO.setQ2AmountOccupied(q2.getAmountOccupied());
            excelVO.setQ2AmountActual(q2.getAmountActual());
            excelVO.setQ2AmountActualApproved(q2.getAmountActualApproved());
            excelVO.setQ2AmountAvailable(q2.getAmountAvailable());
            excelVO.setQ2AmountYearTotal(q2.getAmountYearTotal());
            excelVO.setQ2AmountAdj(q2.getAmountAdj());
            excelVO.setQ2AmountTotal(q2.getAmountTotal());
        }
        
        // Q3季度
        BudgetAmountVo q3 = vo.getQ3();
        if (q3 != null) {
            excelVO.setQ3AmountFrozen(q3.getAmountFrozen());
            excelVO.setQ3AmountOccupied(q3.getAmountOccupied());
            excelVO.setQ3AmountActual(q3.getAmountActual());
            excelVO.setQ3AmountActualApproved(q3.getAmountActualApproved());
            excelVO.setQ3AmountAvailable(q3.getAmountAvailable());
            excelVO.setQ3AmountYearTotal(q3.getAmountYearTotal());
            excelVO.setQ3AmountAdj(q3.getAmountAdj());
            excelVO.setQ3AmountTotal(q3.getAmountTotal());
        }
        
        // Q4季度
        BudgetAmountVo q4 = vo.getQ4();
        if (q4 != null) {
            excelVO.setQ4AmountFrozen(q4.getAmountFrozen());
            excelVO.setQ4AmountOccupied(q4.getAmountOccupied());
            excelVO.setQ4AmountActual(q4.getAmountActual());
            excelVO.setQ4AmountActualApproved(q4.getAmountActualApproved());
            excelVO.setQ4AmountAvailable(q4.getAmountAvailable());
            excelVO.setQ4AmountYearTotal(q4.getAmountYearTotal());
            excelVO.setQ4AmountAdj(q4.getAmountAdj());
            excelVO.setQ4AmountTotal(q4.getAmountTotal());
        }
        
        return excelVO;
    }

    /**
     * 付款情况查询
     * 
     * @param params 查询参数
     * @return 查询结果
     */
    @PostMapping("/queryPaymentStatus")
    @ApiOperation("付款情况查询")
    public CommonResult<PageResult<BudgetClaimStateVo>> queryPaymentStatus(@RequestBody @Valid PaymentStatusQueryParams params) {
        log.info("开始处理付款情况查询，params={}", params);
        PageResult<BudgetClaimStateVo> result = budgetBalanceQueryService.queryPaymentStatus(params);
        return CommonResult.success(result);
    }

    /**
     * 导出付款情况（动态生成Excel）
     * 
     * @param params 查询参数（支持原有搜索条件）
     * @param response HTTP响应对象
     * @throws IOException 导出异常
     */
    @PostMapping("/exportPaymentStatus")
    @ApiOperation("导出付款情况")
    public void exportPaymentStatus(@RequestBody @Valid PaymentStatusQueryParams params, HttpServletResponse response) throws IOException {
        log.info("开始导出付款情况，params={}", params);
        try {
            // 设置headless模式，避免Docker容器中缺少字体库导致的错误
            System.setProperty("java.awt.headless", "true");
            try {
                System.setProperty("sun.java2d.fontpath", "");
            } catch (Exception e) {
                // 忽略设置失败
            }
            
            // 查询全量数据（支持原有搜索条件，但不分页）
            List<BudgetClaimStateVo> voList = budgetBalanceQueryService.queryPaymentStatusAll(params);
            log.info("查询到全量数据：{} 条", voList.size());
            
            // 转换为ExcelVO
            List<PaymentStatusExcelVO> excelVoList = voList.stream()
                    .map(this::convertToPaymentStatusExcelVO)
                    .collect(Collectors.toList());
            
            // 设置响应头
            String fileName = "付款情况导出报表.xlsx";
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8);
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-Disposition", "attachment; filename=" + encodedFileName);
            
            // 直接使用 EasyExcel API，确保 inMemory(true) 生效，避免字体库依赖；表头浅灰+微软雅黑统一样式
            ExcelWriter excelWriter = EasyExcel.write(response.getOutputStream(), PaymentStatusExcelVO.class)
                    .inMemory(true) // 使用内存模式，避免 SXSSF 的字体依赖
                    .autoCloseStream(false) // 不要自动关闭，交给 Servlet 自己处理
                    .registerWriteHandler(ExcelExportStyleUtil.createDefaultStyleStrategy())
                    .build();
            
            WriteSheet writeSheet = EasyExcel.writerSheet("付款情况").build();
            excelWriter.write(excelVoList, writeSheet);
            excelWriter.finish();
            
            log.info("付款情况导出完成，共导出 {} 条数据", excelVoList.size());
        } catch (Exception e) {
            log.error("导出付款情况失败", e);
            throw new IOException("导出失败：" + e.getMessage(), e);
        }
    }

    /**
     * 将BudgetClaimStateVo转换为PaymentStatusExcelVO
     */
    private PaymentStatusExcelVO convertToPaymentStatusExcelVO(BudgetClaimStateVo vo) {
        PaymentStatusExcelVO excelVO = new PaymentStatusExcelVO();
        
        // 基本信息
        excelVO.setProjectCode(vo.getProjectCode());
        excelVO.setProjectName(vo.getProjectName());
        excelVO.setEhrCode(vo.getEhrCode());
        excelVO.setEhrName(vo.getEhrName());
        excelVO.setErpAcctCd(vo.getErpAcctCd());
        excelVO.setErpAcctNm(vo.getErpAcctNm());
        excelVO.setErpAssetType(vo.getErpAssetType());
        
        // 付款额（12个月）
        if (vo.getPaymentAmount() != null) {
            excelVO.setAmount01(vo.getPaymentAmount().getAmount01());
            excelVO.setAmount02(vo.getPaymentAmount().getAmount02());
            excelVO.setAmount03(vo.getPaymentAmount().getAmount03());
            excelVO.setAmount04(vo.getPaymentAmount().getAmount04());
            excelVO.setAmount05(vo.getPaymentAmount().getAmount05());
            excelVO.setAmount06(vo.getPaymentAmount().getAmount06());
            excelVO.setAmount07(vo.getPaymentAmount().getAmount07());
            excelVO.setAmount08(vo.getPaymentAmount().getAmount08());
            excelVO.setAmount09(vo.getPaymentAmount().getAmount09());
            excelVO.setAmount10(vo.getPaymentAmount().getAmount10());
            excelVO.setAmount11(vo.getPaymentAmount().getAmount11());
            excelVO.setAmount12(vo.getPaymentAmount().getAmount12());
        }
        
        return excelVO;
    }
}

