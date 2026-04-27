package com.jasolar.mis.module.system.mapper.budget;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jasolar.mis.framework.mybatis.core.mapper.BaseMapperX;
import com.jasolar.mis.module.system.domain.budget.BudgetQuarterlyDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 预算季度明细视图 Mapper
 * 对应视图：V_BUDGET_QUARTERLY_DETAIL
 *
 * @author Auto
 */
@Mapper
public interface BudgetQuarterlyDetailMapper extends BaseMapperX<BudgetQuarterlyDetail> {

    /**
     * 分页查询预算季度明细数据
     *
     * @param page 分页对象
     * @param year 年度（可选）
     * @param morgCode 组织编码（可选）
     * @param cust1Cd CUST1编码（可选）
     * @param accountSubjectCode 科目编码（可选）
     * @return 分页结果
     */
    IPage<BudgetQuarterlyDetail> selectPage(
            Page<BudgetQuarterlyDetail> page,
            @Param("year") String year,
            @Param("morgCode") String morgCode,
            @Param("cust1Cd") String cust1Cd,
            @Param("accountSubjectCode") String accountSubjectCode
    );

    /**
     * 查询预算季度明细数据列表
     *
     * @param year 年度（可选）
     * @param morgCode 组织编码（可选）
     * @param cust1Cd CUST1编码（可选）
     * @param accountSubjectCode 科目编码（可选）
     * @return 数据列表
     */
    List<BudgetQuarterlyDetail> selectList(
            @Param("year") String year,
            @Param("morgCode") String morgCode,
            @Param("cust1Cd") String cust1Cd,
            @Param("accountSubjectCode") String accountSubjectCode
    );

    /**
     * 分页查询预算季度明细数据（支持 BudgetBalanceQueryParams 查询条件）
     *
     * @param startRow 起始行（从1开始）
     * @param endRow 结束行
     * @param params 查询参数
     * @return 数据列表
     */
    List<BudgetQuarterlyDetail> selectPageByParams(
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
    List<BudgetQuarterlyDetail> selectListByParams(
            @Param("params") com.jasolar.mis.module.system.controller.budget.vo.BudgetBalanceQueryParams params
    );

    /**
     * 根据年度和组织编码列表查询预算季度明细数据
     *
     * @param year 年度
     * @param morgCodeList 组织编码列表
     * @return 数据列表
     */
    List<BudgetQuarterlyDetail> selectListByYearAndMorgCodes(
            @Param("year") String year,
            @Param("morgCodeList") List<String> morgCodeList
    );
}
