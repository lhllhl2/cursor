package com.jasolar.mis.module.system.controller.budget.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

/**
 * Description: 合同申请结果信息响应VO
 * Author : Auto Generated
 * Date : 2025-01-XX
 * Version : 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ContractApplyResultInfoRespVo {

    /**
     * 合同号
     * 参考值：HT001
     */
    private String contractNo;

    /**
     * 处理时间
     * 参考值：2025-01-01 12:00:00
     */
    private String processTime;

    /**
     * 合同明细列表（包含每个明细的校验结果）
     */
    private List<ContractDetailRespVo> contractDetails;
}

