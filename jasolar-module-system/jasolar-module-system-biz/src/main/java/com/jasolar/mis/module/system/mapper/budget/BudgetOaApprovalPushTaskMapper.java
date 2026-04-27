package com.jasolar.mis.module.system.mapper.budget;

import com.jasolar.mis.framework.mybatis.core.mapper.BaseMapperX;
import com.jasolar.mis.module.system.domain.budget.BudgetOaApprovalPushTask;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;

/**
 * OA审批流推送任务 Mapper（BUDGET_OA_APPROVAL_PUSH_TASK）
 */
@Mapper
public interface BudgetOaApprovalPushTaskMapper extends BaseMapperX<BudgetOaApprovalPushTask> {

    @Delete("DELETE FROM BUDGET_OA_APPROVAL_PUSH_TASK WHERE ID = #{id}")
    int deleteByIdPhysical(@Param("id") Long id);

    @Delete({
            "<script>",
            "DELETE FROM BUDGET_OA_APPROVAL_PUSH_TASK",
            "WHERE ID IN",
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>",
            "#{id}",
            "</foreach>",
            "</script>"
    })
    int deleteByIdsPhysical(@Param("ids") Collection<Long> ids);
}
