package com.jasolar.mis.module.system.domain.budget;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jasolar.mis.framework.mybatis.core.dataobject.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 预算接口请求报文记录实体
 */
@Data
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@TableName(value = "budget_api_request_log", autoResultMap = true)
public class BudgetApiRequestLog extends BaseDO {

    @TableId(type = IdType.INPUT)
    private Long id;

    /**
     * 请求URL
     */
    private String requestUrl;

    /**
     * 请求方法（GET/POST/PUT/DELETE等）
     */
    private String requestMethod;

    /**
     * Controller类名
     */
    private String controllerName;

    /**
     * 方法名
     */
    private String methodName;

    /**
     * 请求参数（JSON格式）
     */
    private String requestParams;

    /**
     * 响应结果（JSON格式）
     */
    private String responseResult;

    /**
     * 用户IP地址
     */
    private String userIp;

    /**
     * 用户代理信息
     */
    private String userAgent;

    /**
     * 执行时长（毫秒）
     */
    private Integer executeTime;

    /**
     * 状态（SUCCESS/ERROR）
     */
    private String status;

    /**
     * 错误信息
     */
    private String errorMsg;
}

