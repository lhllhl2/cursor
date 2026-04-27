package com.jasolar.mis.module.system.controller.budget.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Description: 组织明细信息VO
 * Author : Auto Generated
 * Date : 2025-01-XX
 * Version : 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class OrgDetailDetailVo {

    /**
     * EHR组织编码
     */
    private String ehrOrgCode;

    /**
     * ERP部门编码
     */
    private String erpDeptCode;

    /**
     * 计划组织编码
     */
    private String planOrgCode;

    /**
     * 管理组织编码
     */
    private String morgCode;

    /**
     * 父管理组织编码
     */
    private String parentMorgCode;

    /**
     * 是否叶子节点
     */
    private Boolean isLeaf;
}

