package com.jasolar.mis.module.system.mapper.budget;

import com.jasolar.mis.framework.mybatis.core.mapper.BaseMapperX;
import com.jasolar.mis.module.system.domain.budget.BudgetLedgerSelfR;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * 预算流水自引用关系 Mapper
 */
@Mapper
public interface BudgetLedgerSelfRMapper extends BaseMapperX<BudgetLedgerSelfR> {

    List<BudgetLedgerSelfR> selectByIdsAndBizType(@Param("ids") Set<Long> ids, @Param("bizType") String bizType);

    /**
     * 根据关联ID和业务类型批量查询
     * @param relatedIds 关联ID集合
     * @param bizType 业务类型
     * @return 预算流水自引用列表
     */
    List<BudgetLedgerSelfR> selectByRelatedIdsAndBizType(@Param("relatedIds") Set<Long> relatedIds, @Param("bizType") String bizType);

    int deleteByIdsAndBizType(@Param("ids") Set<Long> ids, @Param("bizType") String bizType);

    int insertBatch(@Param("list") List<BudgetLedgerSelfR> list);
}

