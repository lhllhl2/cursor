package com.jasolar.mis.module.system.controller.budget.vo;

import com.jasolar.mis.module.system.enums.DocumentStatusEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
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
 * Description: 申请单请求信息参数
 * Author : Auto Generated
 * Date : 2025-01-XX
 * Version : 1.0
 */
@ApiModel(description = "申请单请求信息参数")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ApplyReqInfoParams {

    /**
     * 需求单号
     * 参考值：XQ001
     */
    @ApiModelProperty(value = "需求单号", example = "XQ001", required = true)
    @NotBlank(message = "需求单号不能为空")
    private String demandOrderNo;

    /**
     * 单据名称
     */
    @ApiModelProperty(value = "单据名称", required = false)
    private String documentName;

    /**
     * 数据来源
     */
    @ApiModelProperty(value = "数据来源", required = false)
    private String dataSource;

    /**
     * 流程名称
     */
    @ApiModelProperty(value = "流程名称", required = false)
    private String processName;

    /**
     * 单据状态
     * 枚举值参考: {@link DocumentStatusEnum}
     * 可选值: NOT_SUBMITTED(未提交), INITIAL_SUBMITTED(初始提交), PENDING(待审批), 
     *         APPROVED(已审批), REJECTED(已拒绝), CANCELLED(已取消)
     */
    @ApiModelProperty(value = "单据状态，可选值: NOT_SUBMITTED(未提交), INITIAL_SUBMITTED(初始提交), PENDING(待审批), APPROVED(已审批), REJECTED(已拒绝), CANCELLED(已取消)", example = "INITIAL_SUBMITTED", required = true, allowableValues = "NOT_SUBMITTED,INITIAL_SUBMITTED,PENDING,APPROVED,REJECTED,CANCELLED")
    @NotBlank(message = "单据状态不能为空")
    private String documentStatus;

    /**
     * 总投资额
     * 参考值：1100.22
     */
    @ApiModelProperty(value = "总投资额", example = "1100.22", required = false)
    private BigDecimal totalInvestmentAmount;

    /**
     * 需求明细列表
     */
    @ApiModelProperty(value = "需求明细列表", required = false)
    @Valid
    private List<ApplyDetailDetalVo> demandDetails;

    /**
     * 操作人
     */
    @ApiModelProperty(value = "操作人", example = "admin", required = true)
    @NotBlank(message = "操作人不能为空")
    private String operator;

    /**
     * 操作人工号
     */
    @ApiModelProperty(value = "操作人工号", required = false)
    private String operatorNo;

    /**
     * 操作时间
     */
    @ApiModelProperty(value = "操作时间，格式：yyyy-MM-dd HH:mm:ss", example = "2025-01-15 14:13:34", required = true)
    @NotBlank(message = "操作时间不能为空")
    private String operateTime;
}

