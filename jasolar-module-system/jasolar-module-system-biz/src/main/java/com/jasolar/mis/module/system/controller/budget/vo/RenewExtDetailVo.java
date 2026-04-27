package com.jasolar.mis.module.system.controller.budget.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Description: 续期扩展明细信息
 * Author : Auto Generated
 * Date : 2025-01-XX
 * Version : 1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RenewExtDetailVo extends RenewDetailDetailVo {

    /**
     * 需求单号
     */
    private String demandOrderNo;
}

