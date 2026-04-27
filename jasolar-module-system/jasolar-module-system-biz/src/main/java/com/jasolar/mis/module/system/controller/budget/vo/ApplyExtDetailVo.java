package com.jasolar.mis.module.system.controller.budget.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 扩展的需求明细信息（用于查询）
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ApplyExtDetailVo extends ApplyDetailDetalVo {

    /**
     * 需求单号
     */
    private String demandOrderNo;
    
    /**
     * 元数据（JSON字符串格式，用于存储到 BudgetLedger）
     * 存储格式：{"text1": "123", "text2": "234", ...}
     */
    private String metadataJson;
}

