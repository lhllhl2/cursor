package com.jasolar.mis.module.system.controller.budget.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Description: 预算付款/报销响应VO
 * Author : Auto Generated
 * Date : 2025-01-XX
 * Version : 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class BudgetClaimRespVo {

    private ESBRespInfoVo esbInfo;

    private ClaimApplyResultInfoRespVo claimApplyResult;
}

