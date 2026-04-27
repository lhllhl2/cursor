package com.jasolar.mis.module.system.controller.budget.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotEmpty;
import org.apache.commons.lang3.StringUtils;
import java.util.List;

/**
 * Description: 查询请求信息参数，支持按需求单号、合同号、付款/报销单号筛选。
 * Author : Auto Generated
 * Date : 2025-01-XX
 * Version : 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@ApiModel("查询请求信息参数")
public class QueryReqInfoParams {

    /** 需求单号（预算申请/需求单），可为空按其它条件查询 */
    @ApiModelProperty(value = "需求单号（预算申请/需求单）", required = false, example = "BUD-REQ-20250101-0001")
    private String demandOrderNo;

    /** 合同号，可为空按其它条件查询 */
    @ApiModelProperty(value = "合同号", required = false, example = "CTR-20250101-0001")
    private String contractNo;

    /** 付款/报销单号，可为空按其它条件查询 */
    @ApiModelProperty(value = "付款/报销单号", required = false, example = "CLM-20250101-0001")
    private String claimOrderNo;

    /** 调整类型 */
    @ApiModelProperty(value = "调整类型", required = false, example = "0")
    private String adjustType;

    /** 数据来源 */
    @ApiModelProperty(value = "数据来源", required = false)
    private String dataSource;

    /** 流程名称（用于展示，可选） */
    @ApiModelProperty(value = "流程名称", required = false)
    private String processName;

    /** 明细列表 */
    @ApiModelProperty(value = "明细列表", required = true)
    @NotEmpty(message = "明细列表不能为空")
    @Valid
    private List<QueryDetailDetailVo> details;

    /**
     * 验证查询条件：(demandOrderNo, contractNo, claimOrderNo) 和 adjustType 必须二选一
     * 不能都不传，也不能都传
     */
    @AssertTrue(message = "(需求单号、合同号、付款/报销单号) 和 调整类型 必须二选一，不能都不传也不能都传")
    private boolean isQueryConditionValid() {
        // 判断是否有传入单号（需求单号、合同号、付款/报销单号至少传一个）
        boolean hasOrderNo = StringUtils.isNotBlank(demandOrderNo)
                || StringUtils.isNotBlank(contractNo)
                || StringUtils.isNotBlank(claimOrderNo);
        
        // 判断是否有传入调整类型
        boolean hasAdjustType = StringUtils.isNotBlank(adjustType);
        
        // 必须二选一：有单号且没有调整类型，或者没有单号且有调整类型
        return hasOrderNo != hasAdjustType;
    }
}
