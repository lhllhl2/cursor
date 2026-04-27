package com.jasolar.mis.module.system.controller.budget.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

/**
 * Description: 付款/报销申请结果信息响应VO
 * Author : Auto Generated
 * Date : 2025-01-XX
 * Version : 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString

public class ClaimApplyResultInfoRespVo {

    /**
     * 报销/付款单号
     * 参考值：BX001
     */
    private String claimOrderNo;

    /**
     * 处理时间
     * 参考值：2025-01-01 12:00:00
     */
    private String processTime;

    /**
     * 报销明细列表（包含每个明细的校验结果）
     */
    private List<ClaimDetailRespVo> claimDetails;
}

