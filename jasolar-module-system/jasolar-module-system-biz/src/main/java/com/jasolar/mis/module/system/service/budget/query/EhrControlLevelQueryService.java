package com.jasolar.mis.module.system.service.budget.query;

import com.jasolar.mis.framework.common.pojo.PageResult;
import com.jasolar.mis.module.system.controller.budget.vo.EhrControlLevelQueryParams;
import com.jasolar.mis.module.system.controller.budget.vo.EhrControlLevelQueryVo;

import java.util.List;

/**
 * Description: EHR控制层级查询服务接口
 * Author : Auto Generated
 * Date : 2025-01-XX
 * Version : 1.0
 */
public interface EhrControlLevelQueryService {

    /**
     * EHR控制层级分页查询
     * 支持对 EHR_CD、CONTROL_EHR_CD、BUDGET_ORG_CD、BUDGET_EHR_CD 四个字段进行模糊搜索
     *
     * @param params 查询参数
     * @return 分页查询结果
     */
    PageResult<EhrControlLevelQueryVo> queryEhrControlLevel(EhrControlLevelQueryParams params);

    /**
     * EHR控制层级全量查询（用于导出，支持原有搜索条件，不分页）
     *
     * @param params 查询参数
     * @return 全量列表
     */
    List<EhrControlLevelQueryVo> queryEhrControlLevelAll(EhrControlLevelQueryParams params);
}

