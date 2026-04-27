package com.jasolar.mis.module.system.controller.budget.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

/**
 * Description: 付款/报销查询结果VO（用于查询接口）
 * Author : Auto Generated
 * Date : 2025-01-XX
 * Version : 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ClaimQueryResultVo {

    /**
     * 报销/付款单号
     */
    private String claimOrderNo;

    /**
     * 单据名称
     */
    private String documentName;

    /**
     * 数据来源
     */
    private String dataSource;

    /**
     * 单据状态
     */
    private String documentStatus;

    /**
     * 报销明细列表
     */
    private List<ClaimQueryRespDetailVo> claimDetails;
}

