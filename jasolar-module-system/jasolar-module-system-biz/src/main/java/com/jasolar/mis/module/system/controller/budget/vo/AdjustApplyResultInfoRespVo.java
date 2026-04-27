package com.jasolar.mis.module.system.controller.budget.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

/**
 * Description: 预算调整申请结果信息响应VO
 * Author : Auto Generated
 * Date : 2025-01-XX
 * Version : 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AdjustApplyResultInfoRespVo {

    /**
     * 预算调整单号
     * 参考值：AD001
     */
    private String adjustOrderNo;

    /**
     * 处理时间
     * 参考值：2025-01-01 12:00:00
     */
    private String processTime;

    /**
     * 是否内部项目（单据级）
     */
    private String isInternal;

    /**
     * 调整明细列表（包含每个明细的校验结果）
     */
    private List<AdjustDetailRespVo> adjustDetails;
}

