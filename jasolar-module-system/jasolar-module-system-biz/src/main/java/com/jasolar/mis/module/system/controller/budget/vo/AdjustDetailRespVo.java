package com.jasolar.mis.module.system.controller.budget.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Description: 调整明细响应VO（用于申请接口返回）
 * Author : Auto Generated
 * Date : 2025-12-05
 * Version : 1.0
 */
@ApiModel(description = "调整明细响应VO（用于申请接口返回）")
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AdjustDetailRespVo extends AdjustDetailDetailVo {

    /**
     * 校验结果
     * 参考值：0 (表示通过)
     */
    @ApiModelProperty(value = "校验结果，0表示通过", example = "0", required = false)
    private String validationResult;

    /**
     * 校验消息
     * 参考值：通过
     */
    @ApiModelProperty(value = "校验消息", example = "通过", required = false)
    private String validationMessage;
}

