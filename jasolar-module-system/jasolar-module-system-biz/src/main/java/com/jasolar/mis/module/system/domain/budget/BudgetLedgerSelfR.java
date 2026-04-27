package com.jasolar.mis.module.system.domain.budget;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jasolar.mis.framework.mybatis.core.dataobject.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

/**
 * 预算流水自引用关系
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@TableName(value = "budget_ledger_self_r", autoResultMap = true)
public class BudgetLedgerSelfR extends BaseDO {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 业务类型
     */
    private String bizType;

    /**
     * 关联ID
     */
    private Long relatedId;

    /**
     * 第一季度扣减金额
     */
    private BigDecimal amountConsumedQOne;

    /**
     * 第二季度扣减金额
     */
    private BigDecimal amountConsumedQTwo;

    /**
     * 第三季度扣减金额
     */
    private BigDecimal amountConsumedQThree;

    /**
     * 第四季度扣减金额
     */
    private BigDecimal amountConsumedQFour;
}


