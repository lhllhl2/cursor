package com.jasolar.mis.module.system.controller.budget.vo;

import com.jasolar.mis.module.system.enums.DocumentStatusEnum;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

/**
 * Description: 事前申请审批/撤回请求信息参数
 * Author : Auto Generated
 * Date : 2025-01-XX
 * Version : 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class RenewApplyReqInfoParams {

    /**
     * 需求单号
     * 参考值：XQ001
     */
    @NotBlank(message = "需求单号不能为空")
    private String demandOrderNo;

    /**
     * 单据状态
     * 枚举值参考: {@link DocumentStatusEnum}
     * 可选值: NOT_SUBMITTED(未提交), UPDATED(已更新), PENDING(待审批), 
     *         APPROVED(已审批), REJECTED(已拒绝), CANCELLED(已取消)
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

    /**
     * 需求明细列表（审批/撤回时非必填，仅在场景一需要传明细金额）
     */
    @Valid
    private List<RenewDetailDetailVo> demandDetails;
}

