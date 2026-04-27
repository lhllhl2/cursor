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
 * 预算额度历史实体
 *
 * @author Auto
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@TableName(value = "budget_quota_history", autoResultMap = true)
public class BudgetQuotaHistory extends BaseDO {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 关联预算额度ID
     */
    private Long quotaId;

    /**
     * 预算池ID
     */
    private Long poolId;

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

    private String budgetType;

    private String currency;

    private String version;

    private BigDecimal amountTotal;

    /**
     * 总预算金额变更值
     */
    private BigDecimal amountTotalVchanged;

    private BigDecimal amountAdj;

    /**
     * 付款额预算金额
     */
    private BigDecimal amountPay;

    /**
     * 付款额预算金额变更值
     */
    private BigDecimal amountPayVchanged;

    /**
     * 可用预算调整金额
     */
    private BigDecimal amountAvailableAdj;

    /**
     * 付款额调整金额
     */
    private BigDecimal amountPayAdj;
}

