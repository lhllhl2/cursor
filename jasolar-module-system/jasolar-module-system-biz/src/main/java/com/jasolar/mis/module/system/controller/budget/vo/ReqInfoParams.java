package com.jasolar.mis.module.system.controller.budget.vo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.List;

/**
 * Description: 请求信息参数
 * Author : Auto Generated
 * Date : 2025-01-XX
 * Version : 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ReqInfoParams {

    /**
     * 单据号
     */
    @NotBlank(message = "单据号不能为空")
    private String documentNo;

    /**
     * 单据名称
     */
    private String documentName;

    /**
     * 数据来源
     */
    private String dataSource;

    /**
     * 流程名称
     */
    private String processName;

    /**
     * 单据状态
     */
    @NotBlank(message = "单据状态不能为空")
    private String documentStatus;

    /**
     * 总预算金额
     */
    private BigDecimal totalBudgetAmount;

    /**
     * 操作人
     */
    @NotBlank(message = "操作人不能为空")
    private String operator;

    /**
     * 操作人工号
     */
    private String operatorNo;

    /**
     * 操作时间
     */
    @NotBlank(message = "操作时间不能为空")
    private String operateTime;

    /**
     * 明细列表
     */
    @Valid
    @NotNull(message = "明细列表不能为空")
    private List<DetailDetailVo> details;
}

