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
 * 预算池维度关系实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@TableName(value = "budget_pool_dem_r", autoResultMap = true)
public class BudgetPoolDemR extends BaseDO {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 预算年份
     */
    private String year;

    /**
     * 预算季度
     */
    private String quarter;

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
     * 项目ID（不变值，用于关联）
     */
    private String projectId;

    /**
     * ERP资产类型编码
     */
    private String erpAssetType;

    /**
     * 是否内部项目
     */
    private String isInternal;
}


