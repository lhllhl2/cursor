package com.jasolar.mis.module.system.mapper.budget;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jasolar.mis.framework.mybatis.core.mapper.BaseMapperX;
import com.jasolar.mis.module.system.controller.budget.vo.BudgetLedgerQueryParams;
import com.jasolar.mis.module.system.domain.budget.BudgetLedger;
import com.jasolar.mis.module.system.domain.budget.BudgetLedgerCompositeKey;
import com.jasolar.mis.module.system.domain.budget.BudgetLedgerWithNames;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * 预算流水 Mapper
 */
@Mapper
public interface BudgetLedgerMapper extends BaseMapperX<BudgetLedger> {

    List<BudgetLedger> selectByCompositeKeys(@Param("conditions") List<BudgetLedgerCompositeKey> conditions);

    /**
     * 根据扩展明细查询预算流水（BIZ_TYPE=APPLY）
     *
     * @param details 扩展明细列表
     * @return 预算流水列表
     */
    List<BudgetLedger> selectByExtDetails(@Param("details") List<com.jasolar.mis.module.system.controller.budget.vo.ApplyExtDetailVo> details);

    /**
     * 批量更新预算流水
     *
     * @param list 预算流水列表
     * @return 更新条数
     */
    int updateBatchById(@Param("list") List<BudgetLedger> list);

    /**
     * 根据续期扩展明细查询预算流水（BIZ_TYPE=APPLY）
     *
     * @param details 续期扩展明细列表
     * @return 预算流水列表
     */
    List<BudgetLedger> selectByRenewExtDetails(@Param("details") List<com.jasolar.mis.module.system.controller.budget.vo.RenewExtDetailVo> details);

    /**
     * 根据合同扩展明细查询预算流水（BIZ_TYPE=CONTRACT）
     *
     * @param details 合同扩展明细列表
     * @return 预算流水列表
     */
    List<BudgetLedger> selectByContractExtDetails(@Param("details") List<com.jasolar.mis.module.system.controller.budget.vo.ContractExtDetailVo> details);

    /**
     * 根据付款/报销扩展明细查询预算流水（BIZ_TYPE=CLAIM）
     *
     * @param details 付款/报销扩展明细列表
     * @return 预算流水列表
     */
    List<BudgetLedger> selectByClaimExtDetails(@Param("details") List<com.jasolar.mis.module.system.controller.budget.vo.ClaimExtDetailVo> details);

    /**
     * 根据调整扩展明细查询预算流水（BIZ_TYPE=ADJUST）
     *
     * @param details 调整扩展明细列表
     * @return 预算流水列表
     */
    List<BudgetLedger> selectByAdjustExtDetails(@Param("details") List<com.jasolar.mis.module.system.controller.budget.vo.AdjustExtDetailVo> details);

    /**
     * 分页查询预算流水（关联 BUDGET_LEDGER_HEAD 表，支持 status 条件）
     *
     * @param page 分页对象
     * @param params 查询参数
     * @param targetIds 关联查询的目标ID集合（可为null或空）
     * @param allowedEhrCdsBatches 用户有权限的EHR组织编码集合列表（每个子列表最多1000个，可为null或空，如果为空则不添加morg_code过滤条件）
     * @param allowedProjectCodesBatches 用户有权限的项目编码集合列表（每个子列表最多1000个，可为null或空，如果为空则不添加master_project_code过滤条件）
     * @return 分页结果
     */
    IPage<BudgetLedgerWithNames> selectPageWithHead(IPage<BudgetLedgerWithNames> page, 
                                          @Param("params") BudgetLedgerQueryParams params,
                                          @Param("targetIds") Set<Long> targetIds,
                                          @Param("allowedEhrCdsBatches") List<List<String>> allowedEhrCdsBatches,
                                          @Param("allowedProjectCodesBatches") List<List<String>> allowedProjectCodesBatches);
}

