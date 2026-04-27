package com.jasolar.mis.module.system.mapper.budget;

import com.jasolar.mis.module.system.domain.budget.BudgetQuota;
import com.jasolar.mis.module.system.domain.budget.BudgetQuotaCompositeKey;
import com.jasolar.mis.framework.mybatis.core.mapper.BaseMapperX;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 预算额度 Mapper
 */
@Mapper
public interface BudgetQuotaMapper extends BaseMapperX<BudgetQuota> {

    /**
     * 根据复合条件批量查询预算额度
     *
     * @param conditions 条件列表
     * @return 预算额度列表
     */
    List<BudgetQuota> selectByCompositeKeys(@Param("conditions") List<BudgetQuotaCompositeKey> conditions);

    /**
     * 批量更新预算额度
     *
     * @param list 预算额度列表
     * @return 更新条数
     */
    int updateBatchById(@Param("list") List<BudgetQuota> list);
}

