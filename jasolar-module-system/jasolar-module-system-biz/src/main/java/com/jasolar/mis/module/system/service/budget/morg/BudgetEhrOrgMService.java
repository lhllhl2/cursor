package com.jasolar.mis.module.system.service.budget.morg;

import java.util.List;

/**
 * EHR组织管理扩展关系 Service
 */
public interface BudgetEhrOrgMService {

    /**
     * 同步EHR管理组织关系数据
     * 从EHR_ORG_MANAGE_R表同步数据到EHR_ORG_MANAGE_EXT_R表
     *
     * @param ehrCodes EHR组织编码列表
     * @return 处理结果描述
     */
    String syncEhrManageRData(List<String> ehrCodes);

    /**
     * 同步EHR管理组织一对一关系数据
     * 从EHR_ORG_MANAGE_R表同步数据到EHR_ORG_MANAGE_ONE_R表
     * 自动查询所有 controlLevel=1 且 Deleted=0 的数据
     *
     * @return 处理结果描述
     */
    String synEhrManageOneRData();
}

