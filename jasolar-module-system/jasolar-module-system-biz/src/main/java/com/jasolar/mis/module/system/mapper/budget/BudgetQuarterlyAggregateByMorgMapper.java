package com.jasolar.mis.module.system.mapper.budget;

import com.jasolar.mis.framework.mybatis.core.mapper.BaseMapperX;
import com.jasolar.mis.module.system.domain.budget.BudgetQuarterlyAggregateByMorg;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 预算季度聚合视图 Mapper（按采购额/付款额分类，按原始组织）
 * 对应视图：V_BUDGET_QUARTERLY_AGGREGATE_BY_MORG
 *
 * @author Auto
 */
@Mapper
public interface BudgetQuarterlyAggregateByMorgMapper extends BaseMapperX<BudgetQuarterlyAggregateByMorg> {

    /**
     * 分页查询预算季度聚合数据（按采购额/付款额分类，按原始组织）
     *
     * @param startRow 起始行（从1开始）
     * @param endRow 结束行
     * @param params 查询参数
     * @return 数据列表
     */
    List<BudgetQuarterlyAggregateByMorg> selectPageByParams(
            @Param("startRow") Integer startRow,
            @Param("endRow") Integer endRow,
            @Param("params") com.jasolar.mis.module.system.controller.budget.vo.BudgetAssetBalanceQueryParams params
    );

    /**
     * 查询符合条件的记录总数
     *
     * @param params 查询参数
     * @return 记录总数
     */
    Long selectCountByParams(
            @Param("params") com.jasolar.mis.module.system.controller.budget.vo.BudgetAssetBalanceQueryParams params
    );

    /**
     * 查询预算季度聚合数据列表（按采购额/付款额分类，按原始组织）
     *
     * @param year 年度（可选）
     * @param morgCode 组织编码（可选）
     * @param erpAssetType ERP资产类型编码（可选）
     * @return 数据列表
     */
    List<BudgetQuarterlyAggregateByMorg> selectList(
            @Param("year") String year,
            @Param("morgCode") String morgCode,
            @Param("erpAssetType") String erpAssetType
    );

    /**
     * 查询全量数据列表（支持 BudgetAssetBalanceQueryParams 查询条件，不分页）
     *
     * @param params 查询参数
     * @return 数据列表
     */
    List<BudgetQuarterlyAggregateByMorg> selectListByParams(
            @Param("params") com.jasolar.mis.module.system.controller.budget.vo.BudgetAssetBalanceQueryParams params
    );

    /**
     * 根据年度和组织编码列表查询预算季度聚合数据
     *
     * @param year 年度
     * @param morgCodeList 组织编码列表
     * @return 数据列表
     */
    List<BudgetQuarterlyAggregateByMorg> selectListByYearAndMorgCodes(
            @Param("year") String year,
            @Param("morgCodeList") List<String> morgCodeList
    );
}
