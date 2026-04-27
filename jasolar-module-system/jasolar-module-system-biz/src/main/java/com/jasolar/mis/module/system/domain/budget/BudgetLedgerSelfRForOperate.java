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
 * 预算流水操作表自引用关系（BUDGET_LEDGER_SELF_R_FOR_OPERATE）
 * id 对应 BUDGET_LEDGER_FOR_OPERATE.id，relatedId 对应关联的 BUDGET_LEDGER_FOR_OPERATE.id
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@TableName(value = "budget_ledger_self_r_for_operate", autoResultMap = true)
public class BudgetLedgerSelfRForOperate extends BaseDO {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 业务类型：APPLY-关联申请单，CONTRACT-关联合同单
     */
    private String bizType;

    /**
     * 关联的 BUDGET_LEDGER_FOR_OPERATE 的 id
     */
    private Long relatedId;
}
