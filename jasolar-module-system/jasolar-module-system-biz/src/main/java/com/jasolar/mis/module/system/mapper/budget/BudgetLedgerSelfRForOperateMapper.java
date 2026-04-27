package com.jasolar.mis.module.system.mapper.budget;

import com.jasolar.mis.framework.mybatis.core.mapper.BaseMapperX;
import com.jasolar.mis.module.system.domain.budget.BudgetLedgerSelfRForOperate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * 预算流水操作表自引用关系 Mapper（BUDGET_LEDGER_SELF_R_FOR_OPERATE）
 */
@Mapper
public interface BudgetLedgerSelfRForOperateMapper extends BaseMapperX<BudgetLedgerSelfRForOperate> {

    /**
     * 根据 BUDGET_LEDGER_FOR_OPERATE 的 id 集合及 bizType 查询，返回 relatedId 列表
     */
    List<BudgetLedgerSelfRForOperate> selectByIdsAndBizType(@Param("ids") Set<Long> ids, @Param("bizType") String bizType);
}
