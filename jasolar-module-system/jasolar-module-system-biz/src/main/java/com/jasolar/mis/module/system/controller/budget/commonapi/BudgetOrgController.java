package com.jasolar.mis.module.system.controller.budget.commonapi;

import com.jasolar.mis.module.system.controller.budget.vo.*;
import com.jasolar.mis.module.system.service.budget.query.BudgetQueryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Description: 预算组织控制器
 * Author : Auto Generated
 * Date : 2025-01-XX
 * Version : 1.0
 */
@RestController
@RequestMapping(value = "/budget/org")
@Slf4j
@Api(tags = "预算组织")
public class BudgetOrgController {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    @Resource
    private BudgetQueryService budgetQueryService;


    @ApiOperation("查询组织关系")
    @PostMapping("/queryOrgRelations")
    public BudgetQueryRelationsVo queryOrgRelations(@RequestBody @Valid BudgetQueryOrgRelationsParams relationsParams){
        log.info("开始处理预算组织查询，params={}", relationsParams);
        return budgetQueryService.queryOrgRelations(relationsParams);
    }

}

