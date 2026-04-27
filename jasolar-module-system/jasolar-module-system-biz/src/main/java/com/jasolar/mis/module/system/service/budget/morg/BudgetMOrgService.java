package com.jasolar.mis.module.system.service.budget.morg;

import org.springframework.web.multipart.MultipartFile;

/**
 * 管理组织导入 Service
 */
public interface BudgetMOrgService {

    /**
     * 导入管理组织相关数据
     *
     * @param file Excel 文件
     * @return 处理结果描述
     */
    String importData(MultipartFile file);
}


