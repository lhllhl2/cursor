package com.jasolar.mis.module.system.controller.budget.vo;

import com.jasolar.mis.framework.common.pojo.PageParam;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * Description: 付款情况查询参数
 * Author : Auto Generated
 * Date : 2025-01-XX
 * Version : 1.0
 */
@Data
@ToString
@ApiModel("付款情况查询参数")
public class PaymentStatusQueryParams extends PageParam {

    /**
     * 预算年度
     */
    @ApiModelProperty(value = "预算年度", required = true)
    @NotBlank(message = "预算年度不能为空")
    private String year;

    /**
     * EHR组织编码
     */
    @ApiModelProperty(value = "EHR组织编码", required = false)
    private String ehrCode;

    /**
     * EHR组织名称
     */
    @ApiModelProperty(value = "EHR组织名称", required = false)
    private String ehrName;

    /**
     * 项目编码
     */
    @ApiModelProperty(value = "项目编码", required = false)
    private String projectCode;

    /**
     * 项目名称
     */
    @ApiModelProperty(value = "项目名称", required = false)
    private String projectName;

    /**
     * ERP科目编码
     */
    @ApiModelProperty(value = "ERP科目编码", required = false)
    private String erpAcctCd;

    /**
     * ERP科目名称
     */
    @ApiModelProperty(value = "ERP科目名称", required = false)
    private String erpAcctNm;

    /**
     * ERP资产类型编码
     */
    @ApiModelProperty(value = "ERP资产类型编码", required = false)
    private String erpAssetType;

    /**
     * 是否内部项目
     */
    @ApiModelProperty(value = "是否内部项目", required = false)
    private String isInternal;

    /**
     * EHR组织编码列表批次（用于权限过滤，IN 查询，每个子列表最多1000个元素，避免Oracle IN子句超过1000个限制）
     */
    @ApiModelProperty(value = "EHR组织编码列表批次（用于权限过滤）", required = false)
    private List<List<String>> ehrCdListBatches;

    /**
     * 项目编码列表批次（用于权限过滤，IN 查询，每个子列表最多1000个元素，避免Oracle IN子句超过1000个限制）
     */
    @ApiModelProperty(value = "项目编码列表批次（用于权限过滤）", required = false)
    private List<List<String>> prjCdListBatches;
}

