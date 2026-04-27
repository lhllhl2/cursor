package com.jasolar.mis.module.system.controller.budget.vo;

import com.jasolar.mis.module.system.enums.DocumentStatusEnum;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

/**
 * Description: 付款/报销申请请求信息参数
 * Author : Auto Generated
 * Date : 2025-01-XX
 * Version : 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ClaimApplyReqInfoParams {

    /**
     * 报销/付款单号
     * 参考值：BX001
     */
    @NotBlank(message = "报销/付款单号不能为空")
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
     * 流程名称
     */
    private String processName;

    /**
     * 单据状态
     * 枚举值参考: {@link DocumentStatusEnum}
     * 可选值: SUBMITTED(已提交), PENDING(待审批), NOT_SUBMITTED(未提交),
     *         APPROVED(已审批), REJECTED(已拒绝), CANCELLED(已取消)
     */
    @NotBlank(message = "单据状态不能为空")
    private String documentStatus;

    /**
     * 报销明细列表
     */
    @Valid
    private List<ClaimDetailDetailVo> claimDetails;

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
}

