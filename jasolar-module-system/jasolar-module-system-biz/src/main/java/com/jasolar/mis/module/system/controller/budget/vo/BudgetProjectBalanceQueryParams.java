package com.jasolar.mis.module.system.controller.budget.vo;

import com.jasolar.mis.framework.common.pojo.PageParam;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 预算项目余额查询参数
 *
 * @author Auto
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel("预算项目余额查询参数")
public class BudgetProjectBalanceQueryParams extends PageParam {

    @ApiModelProperty(value = "预算类型", required = true)
    @NotBlank(message = "预算类型不能为空")
    private String budgetType;

    @ApiModelProperty(value = "年度", example = "2024")
    private String year;

    @ApiModelProperty(value = "EHR组织编码", example = "012-021-003")
    private String ehrCode;

    @ApiModelProperty(value = "EHR组织名称（模糊查询）", example = "财务部")
    private String ehrName;

    @ApiModelProperty(value = "项目编码", example = "PRJ001")
    private String prjCode;

    @ApiModelProperty(value = "项目名称（模糊查询）", example = "项目A")
    private String prjName;

    /**
     * 项目编码列表（用于权限过滤，IN 查询）
     */
    @ApiModelProperty(value = "项目编码列表（用于权限过滤）", required = false)
    private List<String> prjCdList;
}
