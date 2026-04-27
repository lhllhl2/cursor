package com.jasolar.mis.module.system.controller.budget.project;

import com.jasolar.mis.framework.common.pojo.CommonResult;
import com.jasolar.mis.module.system.service.budget.project.BudgetProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Description: 项目预算控制器
 * Author : Auto Generated
 * Date : 2025-01-XX
 * Version : 1.0
 */
@Tag(name = "预算管理 - 项目预算")
@RestController
@RequestMapping(value = "/budget/project")
@Slf4j
public class BudgetProjectController {

    @Resource
    private BudgetProjectService budgetProjectService;

    /**
     * 生成模拟项目数据
     * 
     * @return 生成结果
     */
    @Operation(summary = "生成模拟项目数据")
    @PostMapping("/projectData")
    public CommonResult<String> projectData() {
        log.info("接收生成模拟项目数据请求");
        String result = budgetProjectService.generateProjectData();
        return CommonResult.success(result);
    }
}

