package com.jasolar.mis.module.system.mapper.budget;

import com.jasolar.mis.framework.mybatis.core.mapper.BaseMapperX;
import com.jasolar.mis.module.system.domain.budget.BudgetBalance;
import org.apache.ibatis.annotations.Mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 预算余额 Mapper
 */
@Mapper
public interface BudgetBalanceMapper extends BaseMapperX<BudgetBalance> {

    /**
     * 批量更新预算余额
     *
     * @param list 预算余额列表
     * @return 更新条数
     */
    int updateBatchById(@Param("list") List<BudgetBalance> list);
}

