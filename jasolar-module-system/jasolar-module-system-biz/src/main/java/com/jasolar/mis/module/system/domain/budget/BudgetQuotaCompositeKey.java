package com.jasolar.mis.module.system.domain.budget;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 预算额度复合主键条件
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BudgetQuotaCompositeKey {

    private String morgCode;
    private String projectCode;
    private String customCode;
    private String accountSubjectCode;
    private Long periodId;
    private String budgetType;
}

