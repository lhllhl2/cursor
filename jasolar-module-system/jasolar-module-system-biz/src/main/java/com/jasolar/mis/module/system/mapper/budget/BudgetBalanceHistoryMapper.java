package com.jasolar.mis.module.system.mapper.budget;

import com.jasolar.mis.framework.mybatis.core.mapper.BaseMapperX;
import com.jasolar.mis.module.system.domain.budget.BudgetBalanceHistory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 预算余额历史 Mapper
 */
@Mapper
public interface BudgetBalanceHistoryMapper extends BaseMapperX<BudgetBalanceHistory> {
}

