package com.jasolar.mis.module.system.controller.budget.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.List;

/**
 * Description: 申请单结果信息响应VO
 * Author : Auto Generated
 * Date : 2025-01-XX
 * Version : 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ApplyResultInfoRespVo {

    /**
     * 需求单号
     * 参考值：XQ001
     */
    private String demandOrderNo;

    /**
     * 处理时间
     * 参考值：2025-01-01 12:00:00
     */
    private String processTime;

    /**
     * 需求明细列表（包含每个明细的校验结果）
     */
    private List<ApplyDetailRespVo> demandDetails;
}

