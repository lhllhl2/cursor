package com.jasolar.mis.module.system.controller.budget.quota;

import com.jasolar.mis.framework.common.pojo.CommonResult;
import com.jasolar.mis.module.system.service.budget.quota.BudgetQuotaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 预算额度 Controller
 */
@Tag(name = "预算管理 - 预算额度")
@RestController
@RequestMapping("/budget/quota")
@Slf4j
public class BudgetQuotaController {

    @Resource
    private BudgetQuotaService budgetQuotaService;

    /**
     * 同步原始预算数据
     */
    @Operation(summary = "同步原始项目预算到预算额度表")
    @PostMapping("/syncOriginal")
    public CommonResult<String> syncQuotaDataFromOriginal(@RequestParam String year) {
        log.info("收到同步原始预算额度数据请求，year={}", year);
        String result = budgetQuotaService.syncQuotaDataFromOriginal(year);
        return CommonResult.success(result);
    }

    /**
     * 补足 BUDGET_POOL_DEM_R 的 project_id
     * 按 project_code（master_project_code）和 morgCode 匹配 SYSTEM_PROJECT_BUDGET，只更新 project_id
     * 用于预算调整单等创建的 pool 未设置 project_id 的场景
     */
    @Operation(summary = "补足预算池的 project_id（按 project_code 匹配）")
    @PostMapping("/complementProjectId")
    public CommonResult<String> complementProjectId(@RequestParam String year) {
        log.info("收到补足 project_id 请求，year={}", year);
        String result = budgetQuotaService.complementProjectId(year);
        return CommonResult.success(result);
    }
}

