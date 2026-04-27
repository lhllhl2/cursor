package com.jasolar.mis.module.system.service.budget.query;

import com.jasolar.mis.framework.common.pojo.PageResult;
import com.jasolar.mis.module.system.controller.budget.vo.BudgetLedgerQueryParams;
import com.jasolar.mis.module.system.controller.budget.vo.BudgetLedgerVo;

/**
 * Description: 预算流水查询服务接口
 * Author : Auto Generated
 * Date : 2025-01-XX
 * Version : 1.0
 */
public interface BudgetLedgerQueryService {

    /**
     * 查询预算流水数据
     *
     * @param params 查询参数
     * @return 分页查询结果
     */
    PageResult<BudgetLedgerVo> queryData(BudgetLedgerQueryParams params);

    /**
     * 查询预算流水全量数据（不分页，用于导出）
     *
     * @param params 查询参数
     * @return 全量数据列表
     */
    java.util.List<BudgetLedgerVo> queryAllData(BudgetLedgerQueryParams params);
}

