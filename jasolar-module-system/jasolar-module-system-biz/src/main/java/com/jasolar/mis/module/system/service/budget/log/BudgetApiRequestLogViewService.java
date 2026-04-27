package com.jasolar.mis.module.system.service.budget.log;

import com.jasolar.mis.framework.common.pojo.PageResult;
import com.jasolar.mis.module.system.controller.budget.vo.BudgetApiRequestLogQueryParams;
import com.jasolar.mis.module.system.controller.budget.vo.BudgetApiRequestLogViewVo;

/**
 * 预算接口请求报文记录视图服务接口
 * 
 * @author Auto Generated
 */
public interface BudgetApiRequestLogViewService {

    /**
     * 分页查询预算接口请求报文记录
     * 支持DOC_NO、USER_IP、INTERFACE_NAME、RESPONSE_RESULT四个字段的模糊搜索
     * STATUS字段支持精确搜索
     * 
     * @param params 查询参数
     * @return 分页结果
     */
    PageResult<BudgetApiRequestLogViewVo> pageQuery(BudgetApiRequestLogQueryParams params);
}

