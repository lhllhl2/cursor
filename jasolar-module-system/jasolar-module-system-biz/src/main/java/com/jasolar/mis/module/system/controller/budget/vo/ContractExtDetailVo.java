package com.jasolar.mis.module.system.controller.budget.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Description: 扩展的合同明细信息（用于查询）
 * Author : Auto Generated
 * Date : 2025-01-XX
 * Version : 1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ContractExtDetailVo extends ContractDetailDetailVo {

    /**
     * 合同号
     */
    private String contractNo;
}

