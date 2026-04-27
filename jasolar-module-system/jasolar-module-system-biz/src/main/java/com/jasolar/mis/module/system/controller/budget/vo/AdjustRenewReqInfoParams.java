package com.jasolar.mis.module.system.controller.budget.vo;

import com.jasolar.mis.module.system.enums.DocumentStatusEnum;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Description: 预算调整审批/撤回请求信息参数
 * Author : Auto Generated
 * Date : 2025-01-XX
 * Version : 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AdjustRenewReqInfoParams {

    /**
     * 预算调整单号
     * 参考值：AD001
     */
    @NotBlank(message = "预算调整单号不能为空")
    private String adjustOrderNo;

    /**
     * 单据状态
     * 枚举值参考: {@link DocumentStatusEnum}
     * 可选值: SUBMITTED(已提交), PENDING(待审批), APPROVED(已审批),
     *         REJECTED(已拒绝), CANCELLED(已取消)
     */
    @NotBlank(message = "单据状态不能为空")
    private String documentStatus;

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
     */
    private String approveComment;
}

