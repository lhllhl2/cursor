package com.jasolar.mis.module.system.mapper.budget;

import com.jasolar.mis.framework.mybatis.core.mapper.BaseMapperX;
import com.jasolar.mis.module.system.domain.budget.BudgetClaimMonthlyAggregate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 预算付款（CLAIM）月度聚合视图 Mapper
 * 对应视图：V_BUDGET_CLAIM_MONTHLY_AGGREGATE
 *
 * @author Auto
 */
@Mapper
public interface BudgetClaimMonthlyAggregateMapper extends BaseMapperX<BudgetClaimMonthlyAggregate> {

    /**
     * 查询预算付款月度聚合数据列表
     *
     * @param year 年度（可选）
     * @param morgCode 管理组织编码（可选）
     * @param budgetSubjectCode 预算科目编码（可选）
     * @param masterProjectCode 主数据项目编码（可选）
     * @param erpAssetType ERP资产类型编码（可选）
     * @param isInternal 是否内部项目（可选）
     * @return 数据列表
     */
    List<BudgetClaimMonthlyAggregate> selectList(
            @Param("year") String year,
            @Param("morgCode") String morgCode,
            @Param("budgetSubjectCode") String budgetSubjectCode,
            @Param("masterProjectCode") String masterProjectCode,
            @Param("erpAssetType") String erpAssetType,
            @Param("isInternal") String isInternal
    );

    /**
     * 分页查询预算付款月度聚合数据（支持名称字段单边模糊搜索）
     *
     * @param startRow 起始行（从1开始）
     * @param endRow 结束行
     * @param params 查询参数
     * @return 数据列表
     */
    List<BudgetClaimMonthlyAggregate> selectPageByParams(
            @Param("startRow") Integer startRow,
            @Param("endRow") Integer endRow,
            @Param("params") com.jasolar.mis.module.system.controller.budget.vo.PaymentStatusQueryParams params
    );

    /**
     * 查询符合条件的记录总数
     *
     * @param params 查询参数
     * @return 记录总数
     */
    Long selectCountByParams(
            @Param("params") com.jasolar.mis.module.system.controller.budget.vo.PaymentStatusQueryParams params
    );

    /**
     * 全量查询预算付款月度聚合数据（支持名称字段单边模糊搜索，不分页）
     *
     * @param params 查询参数
     * @return 数据列表
     */
    List<BudgetClaimMonthlyAggregate> selectAllByParams(
            @Param("params") com.jasolar.mis.module.system.controller.budget.vo.PaymentStatusQueryParams params
    );
}

