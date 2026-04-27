package com.jasolar.mis.module.system.controller.budget.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Description: 预算查询响应VO
 * Author : Auto Generated
 * Date : 2025-01-XX
 * Version : 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class BudgetQueryRespVo {

    private ESBRespInfoVo esbInfo;

    /**
     * 查询结果（动态类型）
     * 根据 documentType 可以是：
     * - ApplyQueryResultVo（预算申请）
     * - ContractQueryResultVo（合同）
     * - ClaimQueryResultVo（付款/报销）
     * - AdjustQueryResultVo（调整单）
     */
    private Object queryResult;
}

