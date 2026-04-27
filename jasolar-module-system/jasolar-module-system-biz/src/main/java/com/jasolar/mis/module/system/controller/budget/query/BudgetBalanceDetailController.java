package com.jasolar.mis.module.system.controller.budget.query;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.jasolar.mis.framework.common.pojo.CommonResult;
import com.jasolar.mis.module.system.controller.budget.vo.BudgetAmountVo;
import com.jasolar.mis.module.system.controller.budget.vo.BudgetBalanceDetailQueryParams;
import com.jasolar.mis.module.system.controller.budget.vo.BudgetBalanceExcelVO;
import com.jasolar.mis.module.system.controller.budget.vo.BudgetBalanceVo;
import com.jasolar.mis.module.system.controller.budget.vo.BudgetQuarterlyAggregateByMorgQueryParams;
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
import java.util.List;
import java.util.stream.Collectors;

/**
 * Description: 预算季度明细查询控制器（需要登录）
 * Author : Auto Generated
 * Date : 2025-01-XX
 * Version : 1.0
 */
@RestController
@RequestMapping(value = "/query")
@Slf4j
@Api(tags = "预算季度明细查询（需要登录）")
public class BudgetBalanceDetailController {

    @Resource
    private BudgetBalanceQueryService budgetBalanceQueryService;

    /**
     * 预算季度明细查询（不分页）
     * 
     * @param params 查询参数（包含 year 和 controlEhrCd）
     * @return 查询结果
     */
    @PostMapping("/queryBudgetQuarterlyDetail")
    @ApiOperation("预算季度明细查询（不分页）")
    public CommonResult<List<BudgetBalanceVo>> queryBudgetQuarterlyDetail(
            @RequestBody @Valid BudgetBalanceDetailQueryParams params) {
        log.info("开始处理预算季度明细查询，params={}", params);
        List<BudgetBalanceVo> result = budgetBalanceQueryService.queryBudgetQuarterlyDetail(params);
        return CommonResult.success(result);
    }

    /**
     * 导出预算季度明细（动态生成Excel）
     *
     * @param params   查询参数（包含 year 和 controlEhrCd）
     * @param response HTTP响应对象
     * @throws IOException 导出异常
     */
    @PostMapping("/exportBudgetQuarterlyDetail")
    @ApiOperation("导出预算季度明细")
    public void exportBudgetQuarterlyDetail(@RequestBody @Valid BudgetBalanceDetailQueryParams params,
                                           HttpServletResponse response) throws IOException {
        log.info("开始导出预算季度明细，params={}", params);
        try {
            System.setProperty("java.awt.headless", "true");
            try {
                System.setProperty("sun.java2d.fontpath", "");
            } catch (Exception e) {
                // 忽略设置失败
            }

            List<BudgetBalanceVo> voList = budgetBalanceQueryService.queryBudgetQuarterlyDetail(params);
            log.info("查询到全量数据：{} 条", voList.size());

            List<BudgetBalanceExcelVO> excelVoList = voList.stream()
                    .map(this::convertToExcelVO)
                    .collect(Collectors.toList());

            String fileName = "预算季度明细.xlsx";
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8);
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-Disposition", "attachment; filename=" + encodedFileName);

            ExcelWriter excelWriter = EasyExcel.write(response.getOutputStream(), BudgetBalanceExcelVO.class)
                    .inMemory(true)
                    .autoCloseStream(false)
                    .registerWriteHandler(ExcelExportStyleUtil.createDefaultStyleStrategy())
                    .build();

            WriteSheet writeSheet = EasyExcel.writerSheet("预算季度明细").build();
            excelWriter.write(excelVoList, writeSheet);
            excelWriter.finish();

            log.info("预算季度明细导出完成，共导出 {} 条数据", excelVoList.size());
        } catch (Exception e) {
            log.error("导出预算季度明细失败", e);
            throw new IOException("导出失败：" + e.getMessage(), e);
        }
    }

    /**
     * 将 BudgetBalanceVo 转换为 BudgetBalanceExcelVO
     */
    private BudgetBalanceExcelVO convertToExcelVO(BudgetBalanceVo vo) {
        BudgetBalanceExcelVO excelVO = new BudgetBalanceExcelVO();
        excelVO.setEhrCode(vo.getEhrCode());
        excelVO.setEhrName(vo.getEhrName());
        excelVO.setControlCust1Cd(vo.getControlCust1Cd());
        excelVO.setControlCust1Name(vo.getControlCust1Name());
        excelVO.setSubjectCode(vo.getSubjectCode());
        excelVO.setSubjectName(vo.getSubjectName());
        excelVO.setLastYearUsedBudget(vo.getLastYearUsedBudget());

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
     * 预算季度聚合查询（按原始组织，不分页）
     * 
     * @param params 查询参数（包含 year、controlEhrCd 和 budgetType）
     * @return 查询结果
     */
    @PostMapping("/queryBudgetQuarterlyAggregateByMorg")
    @ApiOperation("预算季度聚合查询（按原始组织，不分页）")
    public CommonResult<List<BudgetBalanceVo>> queryBudgetQuarterlyAggregateByMorg(
            @RequestBody @Valid BudgetQuarterlyAggregateByMorgQueryParams params) {
        log.info("开始处理预算季度聚合查询（按原始组织），params={}", params);
        List<BudgetBalanceVo> result = budgetBalanceQueryService.queryBudgetQuarterlyAggregateByMorg(params);
        return CommonResult.success(result);
    }

    /**
     * 导出预算季度聚合（按原始组织）（动态生成Excel）
     *
     * @param params   查询参数（包含 year、controlEhrCd 和 budgetType）
     * @param response HTTP响应对象
     * @throws IOException 导出异常
     */
    @PostMapping("/exportBudgetQuarterlyAggregateByMorg")
    @ApiOperation("导出预算季度聚合（按原始组织）")
    public void exportBudgetQuarterlyAggregateByMorg(
            @RequestBody @Valid BudgetQuarterlyAggregateByMorgQueryParams params,
            HttpServletResponse response) throws IOException {
        log.info("开始导出预算季度聚合（按原始组织），params={}", params);
        try {
            System.setProperty("java.awt.headless", "true");
            try {
                System.setProperty("sun.java2d.fontpath", "");
            } catch (Exception e) {
                // 忽略设置失败
            }

            List<BudgetBalanceVo> voList = budgetBalanceQueryService.queryBudgetQuarterlyAggregateByMorg(params);
            log.info("查询到全量数据：{} 条", voList.size());

            List<BudgetBalanceExcelVO> excelVoList = voList.stream()
                    .map(this::convertToExcelVO)
                    .collect(Collectors.toList());

            String fileName = "预算季度聚合（按原始组织）.xlsx";
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8);
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-Disposition", "attachment; filename=" + encodedFileName);

            ExcelWriter excelWriter = EasyExcel.write(response.getOutputStream(), BudgetBalanceExcelVO.class)
                    .inMemory(true)
                    .autoCloseStream(false)
                    .registerWriteHandler(ExcelExportStyleUtil.createDefaultStyleStrategy())
                    .build();

            WriteSheet writeSheet = EasyExcel.writerSheet("预算季度聚合（按原始组织）").build();
            excelWriter.write(excelVoList, writeSheet);
            excelWriter.finish();

            log.info("预算季度聚合（按原始组织）导出完成，共导出 {} 条数据", excelVoList.size());
        } catch (Exception e) {
            log.error("导出预算季度聚合（按原始组织）失败", e);
            throw new IOException("导出失败：" + e.getMessage(), e);
        }
    }
}
