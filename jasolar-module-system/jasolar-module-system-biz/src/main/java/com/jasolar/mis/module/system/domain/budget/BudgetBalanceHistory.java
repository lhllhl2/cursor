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
 * 预算余额历史实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@TableName(value = "budget_balance_history", autoResultMap = true)
public class BudgetBalanceHistory extends BaseDO {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 关联预算余额ID
     */
    private Long balanceId;

    /**
     * 预算池ID
     */
    private Long poolId;

    /**
     * 预算额度ID
     */
    private Long quotaId;

    private String morgCode;

    private String projectCode;

    private String customCode;

    private String accountSubjectCode;

    /**
     * ERP资产类型编码
     */
    private String erpAssetType;

    /**
     * 是否内部项目
     */
    private String isInternal;

    /**
     * 预算年度
     */
    private String year;

    /**
     * 预算季度
     */
    private String quarter;

    private String currency;

    private String version;

    private BigDecimal amountFrozen;

    /**
     * 冻结金额变更值
     */
    private BigDecimal amountFrozenVchanged;

    private BigDecimal amountOccupied;

    /**
     * 占用金额变更值
     */
    private BigDecimal amountOccupiedVchanged;

    private BigDecimal amountActual;

    /**
     * 实际金额变更值
     */
    private BigDecimal amountActualVchanged;

    private BigDecimal amountAvailable;

    /**
     * 可用金额变更值
     */
    private BigDecimal amountAvailableVchanged;

    /**
     * 可支付金额
     */
    private BigDecimal amountPayAvailable;

    /**
     * 可支付金额变更值
     */
    private BigDecimal amountPayAvailableVchanged;
}

