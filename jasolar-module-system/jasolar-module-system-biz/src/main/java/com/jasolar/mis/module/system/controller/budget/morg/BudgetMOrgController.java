package com.jasolar.mis.module.system.controller.budget.morg;

import com.jasolar.mis.framework.common.pojo.CommonResult;
import com.jasolar.mis.module.system.service.budget.morg.BudgetMOrgService;
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
 * 管理组织导入控制器
 */
@Tag(name = "预算管理 - 管理组织")
@RestController
@RequestMapping("/budget/morg")
@Slf4j
public class BudgetMOrgController {

    @Resource
    private BudgetMOrgService budgetMOrgService;

    /**
     * 导入管理组织数据
     *
     * @param file Excel 文件
     * @return 处理结果
     */
    @PostMapping("/importData")
    @Operation(summary = "导入管理组织数据")
    public CommonResult<String> importData(@RequestParam("file") MultipartFile file) {
        log.info("开始导入管理组织数据，文件名：{}", file.getOriginalFilename());
        String result = budgetMOrgService.importData(file);
        return CommonResult.success(result);
    }
}


