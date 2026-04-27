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
 * 预算资产类型映射表（BUDGET_ASSET_TYPE_MAPPING）
 * 维护预算资产类型与ERP资产类型的映射关系，预算资产类型来自预算系统视图，其余手工维护
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@TableName(value = "budget_asset_type_mapping", autoResultMap = true)
public class BudgetAssetTypeMapping extends BaseDO {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 预算资产类型编码（取预算系统的视图，映射后的编码）
     */
    private String budgetAssetTypeCode;

    /**
     * 预算资产类型名称（取预算系统的视图）
     */
    private String budgetAssetTypeName;

    /**
     * 资产大类编码（手工维护）
     */
    private String assetMajorCategoryCode;

    /**
     * 资产大类名称（手工维护）
     */
    private String assetMajorCategoryName;

    /**
     * 资产类型编码/ERP资产类型（手工维护）
     */
    private String erpAssetType;

    /**
     * 资产类型名称（手工维护）
     */
    private String assetTypeName;

    /**
     * 年份（手工维护）
     */
    private String year;

    /**
     * 是否变更：UNCHANGED-不变，NEW-新增，MODIFY-修改
     */
    private String changeStatus;
}
