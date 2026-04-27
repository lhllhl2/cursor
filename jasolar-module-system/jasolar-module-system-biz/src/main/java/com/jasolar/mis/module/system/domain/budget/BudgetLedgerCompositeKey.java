package com.jasolar.mis.module.system.domain.budget;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 预算流水复合条件
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BudgetLedgerCompositeKey {

    private String bizType;
    private String bizCode;
    private String bizItemCode;
}

