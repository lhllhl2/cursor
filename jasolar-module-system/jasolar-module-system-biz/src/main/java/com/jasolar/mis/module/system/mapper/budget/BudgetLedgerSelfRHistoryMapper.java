package com.jasolar.mis.module.system.mapper.budget;

import com.jasolar.mis.framework.mybatis.core.mapper.BaseMapperX;
import com.jasolar.mis.module.system.domain.budget.BudgetLedgerSelfRHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 预算流水自引用关系历史 Mapper
 */
@Mapper
public interface BudgetLedgerSelfRHistoryMapper extends BaseMapperX<BudgetLedgerSelfRHistory> {

    int insertBatch(@Param("list") List<BudgetLedgerSelfRHistory> list);
}

