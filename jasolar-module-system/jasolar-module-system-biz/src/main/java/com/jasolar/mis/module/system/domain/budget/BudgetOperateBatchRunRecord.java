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
 * 操作表批量跑单执行记录表（BUDGET_OPERATE_BATCH_RUN_RECORD）
 * 记录每张单据跑单成功/失败及失败时的报错信息
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@TableName(value = "budget_operate_batch_run_record", autoResultMap = true)
public class BudgetOperateBatchRunRecord extends BaseDO {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 单据号（业务单号）
     */
    private String bizCode;

    /**
     * 单据类型：APPLY/CONTRACT/CLAIM/ADJUST
     */
    private String bizType;

    /**
     * 执行状态：SUCCESS-成功，FAILURE-失败
     */
    private String runStatus;

    /**
     * 失败时的报错信息
     */
    private String errorMsg;
}
