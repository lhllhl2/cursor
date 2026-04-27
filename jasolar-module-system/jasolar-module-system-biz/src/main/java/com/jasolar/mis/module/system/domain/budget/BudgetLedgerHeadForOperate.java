package com.jasolar.mis.module.system.domain.budget;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jasolar.mis.framework.mybatis.core.dataobject.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 预算流水头操作表实体
 */
@Data
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@TableName(value = "budget_ledger_head_for_operate", autoResultMap = true)
public class BudgetLedgerHeadForOperate extends BaseDO {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 业务类型
     */
    private String bizType;

    /**
     * 业务单号
     */
    private String bizCode;

    /**
     * 单据名称
     */
    private String documentName;

    /**
     * 数据来源
     */
    private String dataSource;

    /**
     * 版本号
     */
    private String version;

    /**
     * 状态
     */
    private String status;

    /**
     * 操作人（申请时传入的operator字段）
     */
    private String operator;

    /**
     * 操作人工号（申请时传入的operator字段，用于存储工号）
     */
    private String operatorNo;

    /**
     * 流程名称（业务流程，与 BUDGET_LEDGER_HEAD.PROCESS_NAME 一致，重跑时带入申请参数）
     */
    private String processName;
}











