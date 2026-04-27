package com.jasolar.mis.module.system.domain.budget;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jasolar.mis.framework.mybatis.core.dataobject.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * 预算期间实体类
 *
 * @author Auto Generated
 * @date 2025-01-XX
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@TableName(value = "budget_period", autoResultMap = true)
public class BudgetPeriod extends BaseDO {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 年度
     */
    private String year;

    /**
     * 月份
     */
    private String month;

    /**
     * 期间类型(月度MONTHLY, 季度QUARTERLY, 半年度HALFYEARLY, 年度YEARLY)
     */
    private String periodType;

    /**
     * 开始日期
     */
    private LocalDateTime startDate;

    /**
     * 结束日期
     */
    private LocalDateTime endDate;
}

