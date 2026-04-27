package com.jasolar.mis.module.system.controller.budget.vo;

import com.jasolar.mis.module.system.enums.DocumentStatusEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * Description: 合同审批/撤回请求信息参数
 * Author : Auto Generated
 * Date : 2025-01-XX
 * Version : 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ContractRenewReqInfoParams {

    /**
     * 合同号
     * 参考值：HT001
     */
    @NotBlank(message = "合同号不能为空")
    private String contractNo;

    /**
     * 单据状态
     * 枚举值参考: {@link DocumentStatusEnum}
     * 可选值: SUBMITTED(已提交), PENDING(待审批), NOT_SUBMITTED(未提交),
     *         APPROVED(已审批), REJECTED(已拒绝), CANCELLED(已取消)
     */
    @NotBlank(message = "单据状态不能为空")
    private String documentStatus;

    /**
     * 合同年度发生额
     * 参考值：100.22
     * 注意：仅在审批通过（APPROVED）时可选使用，撤回（CANCELLED）时不需要
     */
    private BigDecimal contractAnnualAmount;

    /**
     * 审批人
     */
    @NotBlank(message = "审批人不能为空")
    private String approver;

    /**
     * 审批时间
     */
    @NotBlank(message = "审批时间不能为空")
    private String approveTime;

    /**
     * 审批意见
     * 注意：审批（APPROVED/REJECTED）时可选，撤回（CANCELLED）时不需要
     */
    private String approveComment;
}

