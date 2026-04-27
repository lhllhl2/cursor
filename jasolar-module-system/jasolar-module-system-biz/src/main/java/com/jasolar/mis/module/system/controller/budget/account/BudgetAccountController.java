package com.jasolar.mis.module.system.controller.budget.account;

import com.jasolar.mis.framework.common.pojo.CommonResult;
import com.jasolar.mis.module.system.service.budget.account.BudgetAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Description: 预算科目控制器
 * Author : Auto Generated
 * Date : 2025-01-XX
 * Version : 1.0
 */
@Tag(name = "预算管理 - 预算科目")
@RestController
@RequestMapping(value = "/budget/account")
@Slf4j
public class BudgetAccountController {

    @Resource
    private BudgetAccountService budgetAccountService;

    /**
     * 导入预算科目数据
     * 
     * @param file Excel文件
     * @return 导入结果
     */
    @Operation(summary = "导入预算科目数据")
    @PostMapping("/importData")
    public CommonResult<String> importData(@RequestParam("file") MultipartFile file) {
        log.info("接收预算科目数据导入请求，文件名: {}", file.getOriginalFilename());
        String result = budgetAccountService.importData(file);
        return CommonResult.success(result);
    }
}

