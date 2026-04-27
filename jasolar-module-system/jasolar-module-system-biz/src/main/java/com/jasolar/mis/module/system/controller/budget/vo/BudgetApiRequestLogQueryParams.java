package com.jasolar.mis.module.system.controller.budget.vo;

import com.jasolar.mis.framework.common.pojo.PageParam;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

/**
 * 预算接口请求报文记录查询参数
 * 
 * @author Auto Generated
 */
@Data
@ToString
@ApiModel("预算接口请求报文记录查询参数")
public class BudgetApiRequestLogQueryParams extends PageParam {

    /**
     * 单据号（支持模糊搜索）
     */
    @ApiModelProperty(value = "单据号（支持模糊搜索）", required = false)
    private String docNo;

    /**
     * 请求参数（支持模糊搜索，CLOB字段）
     */
    @ApiModelProperty(value = "请求参数（支持模糊搜索）", required = false)
    private String requestParams;

    /**
     * 响应结果（支持模糊搜索，CLOB字段）
     */
    @ApiModelProperty(value = "响应结果（支持模糊搜索）", required = false)
    private String responseResult;

    /**
     * 接口名称（支持模糊搜索）
     */
    @ApiModelProperty(value = "接口名称（支持模糊搜索）", required = false)
    private String interfaceName;

    /**
     * 用户IP地址（支持模糊搜索）
     */
    @ApiModelProperty(value = "用户IP地址（支持模糊搜索）", required = false)
    private String userIp;

    /**
     * Controller类名
     */
    @ApiModelProperty(value = "Controller类名", required = false)
    private String controllerName;

    /**
     * 方法名
     */
    @ApiModelProperty(value = "方法名", required = false)
    private String methodName;

    /**
     * 状态（SUCCESS/ERROR，精确搜索）
     */
    @ApiModelProperty(value = "状态（SUCCESS/ERROR，精确搜索）", required = false)
    private String status;
}

