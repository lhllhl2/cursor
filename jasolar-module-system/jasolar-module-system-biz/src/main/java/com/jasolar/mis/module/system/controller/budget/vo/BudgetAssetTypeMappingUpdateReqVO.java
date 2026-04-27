package com.jasolar.mis.module.system.controller.budget.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

/**
 * 预算资产类型映射 - 手工维护字段保存请求 VO
 * budgetAssetTypeCode、budgetAssetTypeName、year 由同步拉取，其余字段在页面填写后由此接口保存
 */
@Data
@ApiModel("预算资产类型映射-手工维护保存请求")
public class BudgetAssetTypeMappingUpdateReqVO {

    @NotNull(message = "主键不能为空")
    @ApiModelProperty(value = "主键", required = true)
    private Long id;

    @ApiModelProperty("资产大类编码（手工维护）")
    private String assetMajorCategoryCode;

    @ApiModelProperty("资产大类名称（手工维护）")
    private String assetMajorCategoryName;

    @ApiModelProperty("资产类型编码/ERP资产类型（手工维护）")
    private String erpAssetType;

    @ApiModelProperty("资产类型名称（手工维护）")
    private String assetTypeName;

    @ApiModelProperty("年份（手工维护）")
    private String year;

    @ApiModelProperty("是否变更：UNCHANGED-不变，NEW-新增，MODIFY-修改")
    private String changeStatus;
}
