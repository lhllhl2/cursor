package com.jasolar.mis.module.system.mapper.budget;

import com.jasolar.mis.framework.mybatis.core.mapper.BaseMapperX;
import com.jasolar.mis.module.system.domain.budget.BudgetLedgerHistory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 预算流水历史 Mapper
 */
@Mapper
public interface BudgetLedgerHistoryMapper extends BaseMapperX<BudgetLedgerHistory> {
}

