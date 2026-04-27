package com.jasolar.mis.module.system.domain.budget;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 预算流水扩展查询结果（包含关联表的名称字段）
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BudgetLedgerWithNames extends BudgetLedger {
    
    /**
     * 管理组织名称（来自 EHR_ORG_MANAGE_R.EHR_NM）
     */
    private String morgName;
    
    /**
     * 预算科目名称（来自 SUBJECT_INFO.ERP_ACCT_NM）
     */
    private String budgetSubjectName;
    
    /**
     * 主数据项目名称（来自 PROJECT_CONTROL_R.PRJ_NM）
     */
    private String masterProjectName;
    
    /**
     * ERP资产类型名称（来自 DATAINTEGRATION.VIEW_BUDGET_MEMBER_NAME_CODE.MEMBER_NM）
     */
    private String erpAssetTypeName;
    
    /**
     * 数据来源（来自 BUDGET_LEDGER_HEAD.DATA_SOURCE）
     */
    private String dataSource;

    /**
     * 流程名称（来自 BUDGET_LEDGER_HEAD.PROCESS_NAME）
     */
    private String processName;
}

