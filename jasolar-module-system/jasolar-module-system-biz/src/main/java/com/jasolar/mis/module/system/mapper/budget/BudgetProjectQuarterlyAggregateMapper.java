package com.jasolar.mis.module.system.mapper.budget;

import com.jasolar.mis.framework.mybatis.core.mapper.BaseMapperX;
import com.jasolar.mis.module.system.domain.budget.BudgetProjectQuarterlyAggregate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 预算项目季度聚合视图 Mapper（按投资额/付款额分类）
 * 对应视图：V_BUDGET_PROJECT_QUARTERLY_AGGREGATE
 *
 * @author Auto
 */
@Mapper
public interface BudgetProjectQuarterlyAggregateMapper extends BaseMapperX<BudgetProjectQuarterlyAggregate> {

    /**
     * 分页查询预算项目季度聚合数据（按投资额/付款额分类）
     *
     * @param startRow 起始行（从1开始）
     * @param endRow 结束行
     * @param params 查询参数
     * @return 数据列表
     */
    List<BudgetProjectQuarterlyAggregate> selectPageByParams(
            @Param("startRow") Integer startRow,
            @Param("endRow") Integer endRow,
            @Param("params") com.jasolar.mis.module.system.controller.budget.vo.BudgetProjectBalanceQueryParams params
    );

    /**
     * 查询符合条件的记录总数
     *
     * @param params 查询参数
     * @return 记录总数
     */
    Long selectCountByParams(
            @Param("params") com.jasolar.mis.module.system.controller.budget.vo.BudgetProjectBalanceQueryParams params
    );

    /**
     * 查询预算项目季度聚合数据列表（按投资额/付款额分类）
     *
     * @param year 年度（可选）
     * @param morgCode 组织编码（可选）
     * @param prjCd 项目编码（可选）
     * @return 数据列表
     */
    List<BudgetProjectQuarterlyAggregate> selectList(
            @Param("year") String year,
            @Param("morgCode") String morgCode,
            @Param("prjCd") String prjCd
    );

    /**
     * 查询全量数据列表（支持 BudgetProjectBalanceQueryParams 查询条件，不分页）
     *
     * @param params 查询参数
     * @return 数据列表
     */
    List<BudgetProjectQuarterlyAggregate> selectListByParams(
            @Param("params") com.jasolar.mis.module.system.controller.budget.vo.BudgetProjectBalanceQueryParams params
    );
}

