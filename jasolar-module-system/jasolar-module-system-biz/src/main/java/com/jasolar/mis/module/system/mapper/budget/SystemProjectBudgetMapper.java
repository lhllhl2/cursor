package com.jasolar.mis.module.system.mapper.budget;

import com.jasolar.mis.module.system.domain.budget.SystemProjectBudget;
import com.jasolar.mis.framework.mybatis.core.mapper.BaseMapperX;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 项目预算 Mapper
 *
 * @author Auto
 */
@Mapper
public interface SystemProjectBudgetMapper extends BaseMapperX<SystemProjectBudget> {
    
    /**
     * 删除指定年份的数据
     * 
     * @param year 年份
     * @return 删除的记录数
     */
    int deleteByYear(@Param("year") String year);

    /**
     * 场景1：查询数据
     * ACCOUNT_CD LIKE 'A01030301%' AND PROJECT_CD = 'P00' AND CUSTOM2_CD = 'CU200'
     * 
     * @param year 年份
     * @return 数据列表
     */
    List<SystemProjectBudget> selectNewScenario1(@Param("year") String year);

    /**
     * 场景2：查询数据
     * ACCOUNT_CD = 'A01030112' AND PROJECT_CD = 'P00' AND CUSTOM2_CD LIKE 'CU205%'
     * 
     * @param year 年份
     * @return 数据列表
     */
    List<SystemProjectBudget> selectNewScenario2(@Param("year") String year);

    /**
     * 场景3：查询数据
     * ACCOUNT_CD = 'A010301150102' AND PROJECT_CD = 'P00' AND CUSTOM2_CD LIKE 'CU205%'
     * 
     * @param year 年份
     * @return 数据列表
     */
    List<SystemProjectBudget> selectNewScenario3(@Param("year") String year);

    /**
     * 场景4：查询数据
     * ACCOUNT_CD = 'A010301150102' AND PROJECT_CD != 'P00' AND CUSTOM2_CD = 'CU200'
     * 
     * @param year 年份
     * @return 数据列表
     */
    List<SystemProjectBudget> selectNewScenario4(@Param("year") String year);

    /**
     * 场景5：查询数据
     * ACCOUNT_CD = 'A01030115010102'
     * 从 VIEW_BUDGET_TO_CONTROL_TZE 视图查询
     *
     * @param year 年份
     * @return 数据列表
     */
    List<SystemProjectBudget> selectNewScenario5(@Param("year") String year);
}

