package com.jasolar.mis.module.system.controller.budget.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 预算资产类型映射分页结果 VO
 */
@Data
@ApiModel("预算资产类型映射分页结果VO")
public class BudgetAssetTypeMappingPageVo {

    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("预算资产类型编码")
    private String budgetAssetTypeCode;

    @ApiModelProperty("预算资产类型名称")
    private String budgetAssetTypeName;

    @ApiModelProperty("资产大类编码")
    private String assetMajorCategoryCode;

    @ApiModelProperty("资产大类名称")
    private String assetMajorCategoryName;

    @ApiModelProperty("资产类型编码/ERP资产类型")
    private String erpAssetType;

    @ApiModelProperty("资产类型名称")
    private String assetTypeName;

    @ApiModelProperty("年份")
    private String year;

    @ApiModelProperty("是否变更：UNCHANGED-不变，NEW-新增，MODIFY-修改")
    private String changeStatus;

    @ApiModelProperty("创建人")
    private String creator;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("更新人")
    private String updater;

    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;
}
