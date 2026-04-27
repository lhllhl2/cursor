package com.jasolar.mis.module.system.mapper.budget;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jasolar.mis.framework.mybatis.core.mapper.BaseMapperX;
import com.jasolar.mis.module.system.domain.budget.BudgetQuarterlyAggregate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 预算季度聚合视图 Mapper
 * 对应视图：V_BUDGET_QUARTERLY_AGGREGATE_OPTIMIZED
 *
 * @author Auto
 */
@Mapper
public interface BudgetQuarterlyAggregateMapper extends BaseMapperX<BudgetQuarterlyAggregate> {

    /**
     * 分页查询预算季度聚合数据
     *
     * @param page 分页对象
     * @param year 年度（可选）
     * @param ehrErpAcctCd ERP科目编码（可选）
     * @param controlEhrCode 控制层级EHR编码（可选）
     * @param controlCust1Cd 控制层级CUST1编码（可选）
     * @param controlAccountSubjectCode 控制层级科目编码（可选）
     * @return 分页结果
     */
    IPage<BudgetQuarterlyAggregate> selectPage(
            Page<BudgetQuarterlyAggregate> page,
            @Param("year") String year,
            @Param("ehrErpAcctCd") String ehrErpAcctCd,
            @Param("controlEhrCode") String controlEhrCode,
            @Param("controlCust1Cd") String controlCust1Cd,
            @Param("controlAccountSubjectCode") String controlAccountSubjectCode
    );

    /**
     * 查询预算季度聚合数据列表
     *
     * @param year 年度（可选）
     * @param ehrErpAcctCd ERP科目编码（可选）
     * @param controlEhrCode 控制层级EHR编码（可选）
     * @param controlCust1Cd 控制层级CUST1编码（可选）
     * @param controlAccountSubjectCode 控制层级科目编码（可选）
     * @return 数据列表
     */
    List<BudgetQuarterlyAggregate> selectList(
            @Param("year") String year,
            @Param("ehrErpAcctCd") String ehrErpAcctCd,
            @Param("controlEhrCode") String controlEhrCode,
            @Param("controlCust1Cd") String controlCust1Cd,
            @Param("controlAccountSubjectCode") String controlAccountSubjectCode
    );

    /**
     * 分页查询预算季度聚合数据（支持 BudgetBalanceQueryParams 查询条件）
     *
     * @param startRow 起始行（从1开始）
     * @param endRow 结束行
     * @param params 查询参数
     * @return 数据列表
     */
    List<BudgetQuarterlyAggregate> selectPageByParams(
            @Param("startRow") Integer startRow,
            @Param("endRow") Integer endRow,
            @Param("params") com.jasolar.mis.module.system.controller.budget.vo.BudgetBalanceQueryParams params
    );

    /**
     * 查询符合条件的记录总数（支持 BudgetBalanceQueryParams 查询条件）
     *
     * @param params 查询参数
     * @return 记录总数
     */
    Long selectCountByParams(
            @Param("params") com.jasolar.mis.module.system.controller.budget.vo.BudgetBalanceQueryParams params
    );

    /**
     * 查询全量数据列表（支持 BudgetBalanceQueryParams 查询条件，不分页）
     *
     * @param params 查询参数
     * @return 数据列表
     */
    List<BudgetQuarterlyAggregate> selectListByParams(
            @Param("params") com.jasolar.mis.module.system.controller.budget.vo.BudgetBalanceQueryParams params
    );
}

