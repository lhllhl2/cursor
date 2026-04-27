package com.jasolar.mis.module.system.controller.budget.period;

import com.jasolar.mis.framework.common.pojo.CommonResult;
import com.jasolar.mis.module.system.service.budget.period.BudgetPeriodService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Description: 预算期间控制器
 * Author : Auto Generated
 * Date : 2025-01-XX
 * Version : 1.0
 */
@Tag(name = "预算管理 - 预算期间")
@RestController
@RequestMapping(value = "/budget/period")
@Slf4j
public class BudgetPeriodController {

    @Resource
    private BudgetPeriodService budgetPeriodService;

    /**
     * 生成模拟预算期间数据
     * 
     * @return 生成结果
     */
    @Operation(summary = "生成模拟预算期间数据")
    @PostMapping("/periodData")
    public CommonResult<String> periodData() {
        log.info("接收生成模拟预算期间数据请求");
        String result = budgetPeriodService.generatePeriodData();
        return CommonResult.success(result);
    }
}

