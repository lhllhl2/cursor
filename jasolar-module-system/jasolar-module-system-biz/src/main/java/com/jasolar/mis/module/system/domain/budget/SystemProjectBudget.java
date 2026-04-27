package com.jasolar.mis.module.system.domain.budget;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jasolar.mis.framework.mybatis.core.dataobject.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

/**
 * 项目预算实体类
 *
 * @author Auto Generated
 * @date 2025-01-XX
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@TableName(value = "system_project_budget", autoResultMap = true)
public class SystemProjectBudget extends BaseDO {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 科目
     */
    private String account;

    /**
     * 年度
     */
    private String year;

    /**
     * 版本
     */
    private String version;

    /**
     * 场景
     */
    private String scenario;

    /**
     * 自定义字段1
     */
    private String custom1;

    /**
     * 自定义字段2
     */
    private String custom2;

    /**
     * 自定义字段3
     */
    private String custom3;

    /**
     * 项目
     */
    private String project;

    /**
     * 项目ID（不变值，用于关联）
     */
    private String projectId;

    /**
     * 是否内部项目
     */
    private String isInternal;

    /**
     * 币种
     */
    private String currency;

    /**
     * 一月金额
     */
    private BigDecimal jan;

    /**
     * 二月金额
     */
    private BigDecimal feb;

    /**
     * 三月金额
     */
    private BigDecimal mar;

    /**
     * 四月金额
     */
    private BigDecimal apr;

    /**
     * 五月金额
     */
    private BigDecimal may;

    /**
     * 六月金额
     */
    private BigDecimal jun;

    /**
     * 七月金额
     */
    private BigDecimal jul;

    /**
     * 八月金额
     */
    private BigDecimal aug;

    /**
     * 九月金额
     */
    private BigDecimal sep;

    /**
     * 十月金额
     */
    private BigDecimal oct;

    /**
     * 十一月金额
     */
    private BigDecimal nov;

    /**
     * 十二月金额
     */
    private BigDecimal dec;

    /**
     * 第一季度金额
     */
    private BigDecimal q1;

    /**
     * 第二季度金额
     */
    private BigDecimal q2;

    /**
     * 第三季度金额
     */
    private BigDecimal q3;

    /**
     * 第四季度金额
     */
    private BigDecimal q4;

    /**
     * 年度总额
     */
    @TableField("yeartotal")
    private BigDecimal yearTotal;
}

