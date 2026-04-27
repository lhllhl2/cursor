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
 * OA回调执行日志
 */
@Data
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@TableName(value = "budget_oa_callback_exec_log", autoResultMap = true)
public class BudgetOaCallbackExecLog extends BaseDO {

    @TableId(type = IdType.INPUT)
    private Long id;

    /**
     * 请求参数JSON
     */
    private String requestParams;

    /**
     * 响应结果JSON
     */
    private String responseResult;

    /**
     * 执行SQL列表，分号分隔
     */
    private String runSql;
}
