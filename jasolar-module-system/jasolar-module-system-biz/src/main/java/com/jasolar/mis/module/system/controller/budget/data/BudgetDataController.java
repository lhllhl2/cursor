package com.jasolar.mis.module.system.controller.budget.data;

import com.jasolar.mis.framework.common.pojo.CommonResult;
import com.jasolar.mis.module.system.service.budget.data.BudgetDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Description: 预算数据控制器
 * Author : Auto Generated
 * Date : 2025-01-XX
 * Version : 1.0
 */
@Tag(name = "预算管理 - 预算数据")
@RestController
@RequestMapping(value = "/budget/data")
@Slf4j
public class BudgetDataController {

    @Resource
    private BudgetDataService budgetDataService;

    /**
     * 模拟生成预算数据
     * 
     * @return 生成结果
     */
    @Operation(summary = "模拟生成预算数据")
    @PostMapping("/simulateBudgetData")
    public CommonResult<String> simulateBudgetData() {
        log.info("接收模拟生成预算数据请求");
        String result = budgetDataService.simulateBudgetData();
        return CommonResult.success(result);
    }

    /**
     * 模拟生成预算数据（不包含项目维度）
     * 
     * @return 生成结果
     */
    @Operation(summary = "模拟生成预算数据（不包含项目维度）")
    @PostMapping("/simulateBudgetDataWithoutProject")
    public CommonResult<String> simulateBudgetDataWithoutProject() {
        log.info("接收模拟生成预算数据请求（不包含项目维度）");
        String result = budgetDataService.simulateBudgetDataWithoutProject();
        return CommonResult.success(result);
    }

    /**
     * 模拟生成资金类型预算数据
     * 
     * @return 生成结果
     */
    @Operation(summary = "模拟生成资金类型预算数据")
    @PostMapping("/simulateCapitalTypeBudgetData")
    public CommonResult<String> simulateCapitalTypeBudgetData() {
        log.info("接收模拟生成资金类型预算数据请求");
        String result = budgetDataService.simulateCapitalTypeBudgetData();
        return CommonResult.success(result);
    }

    /**
     * 同步项目预算数据
     * 从 DATAINTEGRATION.VIEW_BUDGET_TO_CONTROL 视图同步到 JASOLAR_BUDGET.SYSTEM_PROJECT_BUDGET 表
     * 
     * @param year 年份
     * @return 同步结果
     */
    @Operation(summary = "同步项目预算数据")
    @PostMapping("/syncProjectBudget")
    public CommonResult<String> syncProjectBudget(@RequestParam String year) {
        log.info("接收同步项目预算数据请求，年份: {}", year);
        String result = budgetDataService.syncProjectBudgetNew(year);
        return CommonResult.success(result);
    }
}

