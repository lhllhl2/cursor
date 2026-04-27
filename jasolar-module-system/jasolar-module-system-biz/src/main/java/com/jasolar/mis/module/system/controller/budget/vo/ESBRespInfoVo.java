package com.jasolar.mis.module.system.controller.budget.vo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Description: ESB响应信息VO
 * Author : Auto Generated
 * Date : 2025-01-XX
 * Version : 1.0
 */
@SuperBuilder
@NoArgsConstructor
@Data
@ToString
public class ESBRespInfoVo extends ESBInfoParams {

    /**
     * 接口返回状态码
     * 该字段是接口返回状态码，与returnMsg成对出现，由业务系统自行制定。
     * returnStatus是S时，returnCode要返回A0001-业务系统编码~A0999-业务系统编码中的一个状态码(如: A0001-SAP)
     * returnStatus是E时，returnCode要返回E0001-业务系统编码~E0999-业务系统编码中的一个状态码(如: E0001-SAP)
     * returnStatus是W时，returnCode要返回W0001-业务系统编码~W0999-业务系统编码中的一个状态码(如: W0001-SAP)
     * 提供方要根据接口执行情况，返回该字段。类型为String，长度为20+
     */
    @NotBlank(message = "返回状态码不能为空")
    private String returnCode;

    /**
     * 接口返回状态信息
     * 该字段是接口返回状态信息，与returnCode成对出现，由业务系统自行制定。
     * 提供方要根据接口执行情况，返回该字段。类型为String，长度为50+
     * 参考值：接收成功!
     */
    @NotBlank(message = "返回状态信息不能为空")
    private String returnMsg;

    /**
     * 接口返回状态
     * 该字段是接口返回状态，可能的值：S、W、E
     * S(成功)：表示接口调用成功，包含技术成功(没有错误)和业务成功(正常单据处理)
     * W(其他状态)：表示接口警告，包含技术警告(如处理时间超限)和业务警告(如查询接口未找到数据)
     * E(失败)：表示接口错误，包含技术错误(如校验失败、字段类型不匹配)和业务错误(如单据处理失败、单据号不匹配)
     * 提供方要根据接口执行情况，返回该字段。类型为String，长度为1+
     */
    @NotBlank(message = "返回状态不能为空")
    private String returnStatus;

    /**
     * 响应时间
     * 该字段为提供方响应结束时的系统时间。
     * 格式：yyyy-MM-dd HH24:mm:ss.SSS
     * responseTime与requestTime的差值即为提供方的系统处理时间。
     * 提供方要根据接口执行情况，返回该字段。类型为String，长度为30+
     * 参考值：2008-11-11 14:13:34.222
     */
    @NotBlank(message = "响应时间不能为空")
    private String responseTime;
}

