package com.jasolar.mis.module.system.service.budget.query;

import com.jasolar.mis.framework.common.pojo.PageResult;
import com.jasolar.mis.module.system.controller.budget.vo.BudgetAssetBalanceQueryParams;
import com.jasolar.mis.module.system.controller.budget.vo.BudgetBalanceQueryParams;
import com.jasolar.mis.module.system.controller.budget.vo.BudgetBalanceVo;
import com.jasolar.mis.module.system.controller.budget.vo.BudgetClaimStateVo;
import com.jasolar.mis.module.system.controller.budget.vo.BudgetProjectBalanceQueryParams;
import com.jasolar.mis.module.system.controller.budget.vo.PaymentStatusQueryParams;

import java.util.List;

/**
 * Description: 预算余额查询服务接口
 * Author : Auto Generated
 * Date : 2025-01-XX
 * Version : 1.0
 */
public interface BudgetBalanceQueryService {

    /**
     * 部门费用可用预算查询
     *
     * @param params 查询参数
     * @return 分页查询结果
     */
    PageResult<BudgetBalanceVo> queryDeptBudgetBalance(BudgetBalanceQueryParams params);

    /**
     * 部门资产可用预算查询
     *
     * @param params 查询参数
     * @return 分页查询结果
     */
    PageResult<BudgetBalanceVo> queryDeptAssetBudgetBalance(BudgetAssetBalanceQueryParams params);

    /**
     * 项目可用预算查询
     *
     * @param params 查询参数
     * @return 分页查询结果
     */
    PageResult<BudgetBalanceVo> queryProjectBudgetBalance(BudgetProjectBalanceQueryParams params);

    /**
     * 付款情况查询
     *
     * @param params 查询参数
     * @return 分页查询结果
     */
    PageResult<BudgetClaimStateVo> queryPaymentStatus(PaymentStatusQueryParams params);

    /**
     * 部门费用可用预算全量查询（用于导出，不分页）
     *
     * @param params 查询参数
     * @return 全量数据列表
     */
    List<BudgetBalanceVo> queryDeptBudgetBalanceAll(BudgetBalanceQueryParams params);

    /**
     * 部门资产可用预算全量查询（用于导出，不分页）
     *
     * @param params 查询参数
     * @return 全量数据列表
     */
    List<BudgetBalanceVo> queryDeptAssetBudgetBalanceAll(BudgetAssetBalanceQueryParams params);

    /**
     * 项目可用预算全量查询（用于导出，不分页）
     *
     * @param params 查询参数
     * @return 全量数据列表
     */
    List<BudgetBalanceVo> queryProjectBudgetBalanceAll(BudgetProjectBalanceQueryParams params);

    /**
     * 付款情况全量查询（用于导出，不分页）
     *
     * @param params 查询参数
     * @return 全量数据列表
     */
    List<BudgetClaimStateVo> queryPaymentStatusAll(PaymentStatusQueryParams params);

    /**
     * 预算季度明细查询（不分页）
     *
     * @param params 查询参数（包含 year 和 controlEhrCd）
     * @return 数据列表
     */
    List<com.jasolar.mis.module.system.controller.budget.vo.BudgetBalanceVo> queryBudgetQuarterlyDetail(
            com.jasolar.mis.module.system.controller.budget.vo.BudgetBalanceDetailQueryParams params
    );

    /**
     * 预算季度聚合查询（按原始组织，不分页）
     *
     * @param params 查询参数（包含 year、controlEhrCd 和 budgetType）
     * @return 数据列表
     */
    List<BudgetBalanceVo> queryBudgetQuarterlyAggregateByMorg(
            com.jasolar.mis.module.system.controller.budget.vo.BudgetQuarterlyAggregateByMorgQueryParams params
    );
}

