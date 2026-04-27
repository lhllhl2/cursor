package com.jasolar.mis.module.system.domain.ehr;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * EHR组织控制层级视图
 * 功能：通过EHR_ORG_MANAGE_R表，对所有EHR_CD向上追溯对应的CONTROL_LEVEL=1的EHR_CD
 *      以及预算组织相关信息
 * 
 * @author Auto Generated
 * @date 2025-01-XX
 */
@TableName(value = "V_EHR_CONTROL_LEVEL", autoResultMap = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EhrControlLevelView {

    /**
     * 原始EHR组织代码
     */
    @TableField(value = "EHR_CD")
    private String ehrCd;

    /**
     * 向上追溯找到的CONTROL_LEVEL=1的EHR组织代码
     */
    @TableField(value = "CONTROL_EHR_CD")
    private String controlEhrCd;

    /**
     * 预算组织编码（从EHR_CD向上追溯找到的第一个非NULL的ORG_CD）
     */
    @TableField(value = "BUDGET_ORG_CD")
    private String budgetOrgCd;

    /**
     * 预算层级的EHR组织编码（找到BUDGET_ORG_CD时对应的EHR_CD）
     */
    @TableField(value = "BUDGET_EHR_CD")
    private String budgetEhrCd;

    /**
     * EHR组织名称（对应EHR_CD）
     */
    @TableField(value = "EHR_NM")
    private String ehrNm;

    /**
     * 控制层级EHR组织名称（对应CONTROL_EHR_CD）
     */
    @TableField(value = "CONTROL_EHR_NM")
    private String controlEhrNm;

    /**
     * 预算组织名称（对应BUDGET_ORG_CD）
     */
    @TableField(value = "BUDGET_ORG_NM")
    private String budgetOrgNm;

    /**
     * 预算层级EHR组织名称（对应BUDGET_EHR_CD）
     */
    @TableField(value = "BUDGET_EHR_NM")
    private String budgetEhrNm;

    /**
     * ERP部门编码（口径：起点 EHR_CD 对应的 ERP_DEPART）
     */
    @TableField(value = "ERP_DEPART")
    private String erpDepart;
}

