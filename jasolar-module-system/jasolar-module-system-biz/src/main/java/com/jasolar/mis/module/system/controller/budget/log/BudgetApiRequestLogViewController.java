package com.jasolar.mis.module.system.controller.budget.log;

import com.jasolar.mis.framework.common.pojo.CommonResult;
import com.jasolar.mis.framework.common.pojo.PageResult;
import com.jasolar.mis.module.system.controller.budget.vo.BudgetApiRequestLogQueryParams;
import com.jasolar.mis.module.system.controller.budget.vo.BudgetApiRequestLogViewVo;
import com.jasolar.mis.module.system.service.budget.log.BudgetApiRequestLogViewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 预算接口请求报文记录视图控制器
 * 
 * @author Auto Generated
 */
@Tag(name = "预算管理 - 接口请求报文记录查询")
@RestController
@RequestMapping("/api-request-log")
@Slf4j
public class BudgetApiRequestLogViewController {

    @Resource
    private BudgetApiRequestLogViewService budgetApiRequestLogViewService;

    /**
     * 分页查询预算接口请求报文记录
     * 支持DOC_NO、USER_IP、INTERFACE_NAME、RESPONSE_RESULT四个字段的模糊搜索
     * STATUS字段支持精确搜索
     * 
     * @param params 查询参数
     * @return 分页结果
     */
    @PostMapping("/page")
    @Operation(summary = "分页查询预算接口请求报文记录", description = "支持DOC_NO、USER_IP、INTERFACE_NAME、RESPONSE_RESULT四个字段的模糊搜索，STATUS字段支持精确搜索")
    public CommonResult<PageResult<BudgetApiRequestLogViewVo>> pageQuery(
            @RequestBody @Valid BudgetApiRequestLogQueryParams params) {
        log.info("分页查询预算接口请求报文记录，params={}", params);
        
        PageResult<BudgetApiRequestLogViewVo> result = budgetApiRequestLogViewService.pageQuery(params);
        
        return CommonResult.success(result);
    }
}

