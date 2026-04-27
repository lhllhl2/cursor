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
 * 预算流水操作表实体
 */
@Data
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@TableName(value = "budget_ledger_for_operate", autoResultMap = true)
public class BudgetLedgerForOperate extends BaseDO {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String bizType;

    private String bizCode;

    private String bizItemCode;

    private String effectType;

    /**
     * 预算年度
     */
    private String year;

    /**
     * 预算月份
     */
    private String month;

    /**
     * 实际年度
     */
    private String actualYear;

    /**
     * 实际月份
     */
    private String actualMonth;

    /**
     * 管理组织编码
     */
    private String morgCode;

    /**
     * 预算科目编码
     */
    private String budgetSubjectCode;

    /**
     * 主数据项目编码
     */
    private String masterProjectCode;

    /**
     * ERP资产类型编码
     */
    private String erpAssetType;

    /**
     * 是否内部项目
     */
    private String isInternal;

    private String currency;

    /**
     * 预算总金额
     */
    private BigDecimal amount;

    /**
     * 第一季度消耗金额
     */
    private BigDecimal amountConsumedQOne;

    /**
     * 第二季度消耗金额
     */
    private BigDecimal amountConsumedQTwo;

    /**
     * 第三季度消耗金额
     */
    private BigDecimal amountConsumedQThree;

    /**
     * 第四季度消耗金额
     */
    private BigDecimal amountConsumedQFour;

    /**
     * 可用金额
     */
    private BigDecimal amountAvailable;

    private String version;

    /**
     * 上一版本号
     */
    private String versionPre;

    /**
     * 元数据（扩展信息，JSON 字符串格式）
     */
    private String metadata;

    /**
     * 操作人（申请时传入的operator字段）
     */
    private String operator;

    /**
     * 操作人工号（申请时传入的operator字段，用于存储工号）
     */
    private String operatorNo;
}


