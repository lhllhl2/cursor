package com.jasolar.mis.module.system.controller.budget.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Description: ESB信息参数
 * Author : Auto Generated
 * Date : 2025-01-XX
 * Version : 1.0
 */
@ApiModel(description = "ESB信息参数")
@SuperBuilder
@NoArgsConstructor
@Data
@ToString
public class ESBInfoParams {

    /**
     * 接口调用流水号
     * 该字段为接口调用的流水号，每次调用接口时都会有一个instId产生。
     * 提供方开发接口时要包含这个字段，类型为String，长度为50+。
     * 调用方调用接口时不需要填写该字段。
     * 参考值：N53e值：N53e3cfef.1b64609a.N4c.18e87ed2f0f.N7ff6
     */
    @ApiModelProperty(value = "接口调用流水号，该字段为接口调用的流水号，每次调用接口时都会有一个instId产生。提供方开发接口时要包含这个字段，类型为String，长度为50+。调用方调用接口时不需要填写该字段。", example = "N53e3cfef.1b64609a.N4c.18e87ed2f0f.N7ff6", required = false)
    private String instId;

    /**
     * 请求时间
     * 该字段为调用方调用接口时的系统时间。
     * 提供方开发接口时要包含这个字段。
     * 调用方调用接口时填写系统时间，格式：yyyy-MM-dd HH24:mm:ss.SSS
     * 参考值：2008-11-11 14:13:34.222
     */
    @ApiModelProperty(value = "请求时间，该字段为调用方调用接口时的系统时间。提供方开发接口时要包含这个字段。调用方调用接口时填写系统时间，格式：yyyy-MM-dd HH:mm:ss.SSS", example = "2008-11-11 14:13:34.222", required = true)
    @NotBlank(message = "请求时间不能为空")
    private String requestTime;

    /**
     * 弹性字段1
     * 该字段为总线规范中的弹性字段。
     * 提供方开发接口时要包含这个字段，不允许该字段传递业务数据。
     */
    @ApiModelProperty(value = "弹性字段1，该字段为总线规范中的弹性字段。提供方开发接口时要包含这个字段，不允许该字段传递业务数据。", required = false)
    private String attr1;

    /**
     * 弹性字段2
     * 该字段为总线规范中的弹性字段。
     * 提供方开发接口时要包含这个字段，不允许该字段传递业务数据。
     */
    @ApiModelProperty(value = "弹性字段2，该字段为总线规范中的弹性字段。提供方开发接口时要包含这个字段，不允许该字段传递业务数据。", required = false)
    private String attr2;

    /**
     * 弹性字段3
     * 该字段为总线规范中的弹性字段。
     * 提供方开发接口时要包含这个字段，不允许该字段传递业务数据。
     */
    @ApiModelProperty(value = "弹性字段3，该字段为总线规范中的弹性字段。提供方开发接口时要包含这个字段，不允许该字段传递业务数据。", required = false)
    private String attr3;
}

