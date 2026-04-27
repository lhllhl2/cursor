package com.jasolar.mis.module.system.service.budget.query;

import com.jasolar.mis.module.system.controller.budget.vo.BudgetQueryOrgRelationsParams;
import com.jasolar.mis.module.system.controller.budget.vo.BudgetQueryParams;
import com.jasolar.mis.module.system.controller.budget.vo.BudgetQueryRelationsVo;
import com.jasolar.mis.module.system.controller.budget.vo.BudgetQueryRespVo;
import jakarta.validation.Valid;

/**
 * Description: 预算查询服务接口
 * Author : Auto Generated
 * Date : 2025-01-XX
 * Version : 1.0
 */
public interface BudgetQueryService {
    
    /**
     * 预算查询
     * 
     * @param budgetQueryParams 预算查询参数
     * @return 预算查询响应
     */
    BudgetQueryRespVo query(BudgetQueryParams budgetQueryParams);


    /**
     * 查询组织关系
     *
     * @param relationsParams 组织关系参数
     * @return 组织关系响应
     */
    BudgetQueryRelationsVo queryOrgRelations(@Valid BudgetQueryOrgRelationsParams relationsParams);
}

