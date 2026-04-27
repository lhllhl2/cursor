package com.jasolar.mis.module.system.service.budget.account;

import org.springframework.web.multipart.MultipartFile;

/**
 * 预算科目 Service 接口
 *
 * @author Auto Generated
 * @date 2025-01-XX
 */
public interface BudgetAccountService {

    /**
     * 导入预算科目数据
     *
     * @param file Excel文件
     * @return 导入结果信息
     */
    String importData(MultipartFile file);
}

