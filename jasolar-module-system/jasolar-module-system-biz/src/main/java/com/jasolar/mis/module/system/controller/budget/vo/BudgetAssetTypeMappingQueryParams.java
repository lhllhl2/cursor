package com.jasolar.mis.module.system.controller.budget.vo;

import com.jasolar.mis.framework.common.pojo.PageParam;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

/**
 * 预算资产类型映射查询参数
 * year、changeStatus 多选精确匹配；其余指定字段支持模糊搜索
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@ApiModel("预算资产类型映射查询参数")
public class BudgetAssetTypeMappingQueryParams extends PageParam {

    @ApiModelProperty(value = "年份（多选，精确匹配），请求体传 year: [\"2025\", \"2024\"]", required = false)
    private List<String> year;

    @ApiModelProperty(value = "是否变更（多选）：UNCHANGED-不变，NEW-新增，MODIFY-修改，请求体传 changeStatus: [\"NEW\", \"MODIFY\"]", required = false)
    private List<String> changeStatus;

    @ApiModelProperty(value = "预算资产类型编码（模糊搜索）", required = false)
    private String budgetAssetTypeCode;

    @ApiModelProperty(value = "预算资产类型名称（模糊搜索）", required = false)
    private String budgetAssetTypeName;

    @ApiModelProperty(value = "资产大类编码（模糊搜索）", required = false)
    private String assetMajorCategoryCode;

    @ApiModelProperty(value = "资产大类名称（模糊搜索）", required = false)
    private String assetMajorCategoryName;

    @ApiModelProperty(value = "资产类型编码/ERP资产类型（模糊搜索）", required = false)
    private String erpAssetType;

    @ApiModelProperty(value = "资产类型名称（模糊搜索）", required = false)
    private String assetTypeName;
}
