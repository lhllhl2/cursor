package com.jasolar.mis.module.system.controller.budget.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

/**
 * Description: 合同查询结果VO（用于查询接口）
 * Author : Auto Generated
 * Date : 2025-01-XX
 * Version : 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ContractQueryResultVo {

    /**
     * 合同号
     */
    private String contractNo;

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
     * 合同明细列表
     */
    private List<ContractQueryRespDetailVo> contractDetails;
}

