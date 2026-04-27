package com.jasolar.mis.module.system.controller.budget.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Description: 扩展的明细信息（用于查询）
 * Author : Auto Generated
 * Date : 2025-01-XX
 * Version : 1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ExtDetailVo extends DetailDetailVo {

    /**
     * 单据号
     */
    private String documentNo;
}

