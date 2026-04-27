package com.jasolar.mis.module.system.controller.budget.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 预算接口请求报文记录视图VO
 * 
 * @author Auto Generated
 */
@Data
@ApiModel("预算接口请求报文记录视图VO")
public class BudgetApiRequestLogViewVo {

    @ApiModelProperty("主键ID")
    private Long id;

    @ApiModelProperty("请求URL")
    private String requestUrl;

    @ApiModelProperty("请求方法")
    private String requestMethod;

    @ApiModelProperty("Controller类名")
    private String controllerName;

    @ApiModelProperty("方法名")
    private String methodName;

    @ApiModelProperty("请求参数（JSON格式）")
    private String requestParams;

    @ApiModelProperty("响应结果（JSON格式）")
    private String responseResult;

    @ApiModelProperty("用户IP地址")
    private String userIp;

    @ApiModelProperty("用户代理信息")
    private String userAgent;

    @ApiModelProperty("执行时长（毫秒）")
    private Integer executeTime;

    @ApiModelProperty("状态（SUCCESS/ERROR）")
    private String status;

    @ApiModelProperty("错误信息")
    private String errorMsg;

    @ApiModelProperty("创建人")
    private String creator;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("更新人")
    private String updater;

    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;

    @ApiModelProperty("是否删除")
    private Integer deleted;

    @ApiModelProperty("接口名称")
    private String interfaceName;

    @ApiModelProperty("单据号")
    private String docNo;
}

