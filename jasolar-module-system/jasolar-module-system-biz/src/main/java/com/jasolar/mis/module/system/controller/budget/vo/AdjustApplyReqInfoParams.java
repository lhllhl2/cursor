package com.jasolar.mis.module.system.controller.budget.vo;

import com.jasolar.mis.module.system.enums.DocumentStatusEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

/**
 * Description: 预算调整申请请求信息参数
 * Author : Auto Generated
 * Date : 2025-01-XX
 * Version : 1.0
 */
@ApiModel(description = "预算调整申请请求信息参数")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AdjustApplyReqInfoParams {

    /**
     * 预算调整单号
     * 参考值：AD001
     */
    @ApiModelProperty(value = "预算调整单号", example = "AD001", required = true)
    @NotBlank(message = "预算调整单号不能为空")
    private String adjustOrderNo;

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
     * 可选值: NOT_SUBMITTED(未提交), PENDING(待审批), SUBMITTED(已提交),
     *         APPROVED(已审批), REJECTED(已拒绝), CANCELLED(已取消)
     */
    @ApiModelProperty(value = "单据状态", required = true, allowableValues = "NOT_SUBMITTED,PENDING,SUBMITTED,APPROVED,REJECTED,CANCELLED")
    @NotBlank(message = "单据状态不能为空")
    private String documentStatus;

    /**
     * 是否内部项目（单据级，所有明细一致）
     */
    @ApiModelProperty(value = "是否在集团内，0-否，1-是（单据级）", example = "1", required = true)
    @NotBlank(message = "是否在集团内不能为空")
    private String isInternal;

    /**
     * 调整明细列表
     */
    @ApiModelProperty(value = "调整明细列表", required = true)
    @Valid
    private List<AdjustDetailDetailVo> adjustDetails;

    /**
     * 操作人
     */
    @ApiModelProperty(value = "操作人", required = true)
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
    @ApiModelProperty(value = "操作时间", example = "2025-01-01 12:00:00", required = true)
    @NotBlank(message = "操作时间不能为空")
    private String operateTime;
}

