package com.jasolar.mis.module.system.controller.budget.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 扩展的调整明细信息（用于查询）
 * 
 * @author jasolar
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AdjustExtDetailVo extends AdjustDetailDetailVo {

    /**
     * 调整单号
     */
    private String adjustOrderNo;
    
    /**
     * 元数据（JSON字符串格式，用于存储到 BudgetLedger）
     * 存储格式：{"text1": "123", "text2": "234", ...}
     */
    private String metadataJson;
}

