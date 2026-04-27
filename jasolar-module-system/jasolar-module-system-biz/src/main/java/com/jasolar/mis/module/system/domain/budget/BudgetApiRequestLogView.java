package com.jasolar.mis.module.system.domain.budget;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 预算接口请求报文记录视图实体
 * 对应视图：V_BUDGET_API_REQUEST_LOG
 * 说明：基于BUDGET_API_REQUEST_LOG表创建视图，添加了接口名称（INTERFACE_NAME）和单据号（DOC_NO）字段
 * 
 * @author Auto Generated
 */
@TableName(value = "V_BUDGET_API_REQUEST_LOG", autoResultMap = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetApiRequestLogView {

    /**
     * 主键ID
     */
    @TableField(value = "ID")
    private Long id;

    /**
     * 请求URL
     */
    @TableField(value = "REQUEST_URL")
    private String requestUrl;

    /**
     * 请求方法（GET/POST/PUT/DELETE等）
     */
    @TableField(value = "REQUEST_METHOD")
    private String requestMethod;

    /**
     * Controller类名
     */
    @TableField(value = "CONTROLLER_NAME")
    private String controllerName;

    /**
     * 方法名
     */
    @TableField(value = "METHOD_NAME")
    private String methodName;

    /**
     * 请求参数（JSON格式）
     */
    @TableField(value = "REQUEST_PARAMS")
    private String requestParams;

    /**
     * 响应结果（JSON格式）
     */
    @TableField(value = "RESPONSE_RESULT")
    private String responseResult;

    /**
     * 用户IP地址
     */
    @TableField(value = "USER_IP")
    private String userIp;

    /**
     * 用户代理信息
     */
    @TableField(value = "USER_AGENT")
    private String userAgent;

    /**
     * 执行时长（毫秒）
     */
    @TableField(value = "EXECUTE_TIME")
    private Integer executeTime;

    /**
     * 状态（SUCCESS/ERROR）
     */
    @TableField(value = "STATUS")
    private String status;

    /**
     * 错误信息
     */
    @TableField(value = "ERROR_MSG")
    private String errorMsg;

    /**
     * 创建人
     */
    @TableField(value = "CREATOR")
    private String creator;

    /**
     * 创建时间
     */
    @TableField(value = "CREATE_TIME")
    private LocalDateTime createTime;

    /**
     * 更新人
     */
    @TableField(value = "UPDATER")
    private String updater;

    /**
     * 更新时间
     */
    @TableField(value = "UPDATE_TIME")
    private LocalDateTime updateTime;

    /**
     * 是否删除(1:已删除,0:未删除)
     */
    @TableField(value = "DELETED")
    private Integer deleted;

    /**
     * 接口名称（视图计算字段）
     * 根据CONTROLLER_NAME和METHOD_NAME推导：
     * - BudgetApplicationController + apply = 预算申请接口
     * - BudgetApplicationController + authOrCancel = 预算申请审批接口
     * - BudgetContractController + apply = 合同申请接口
     * - BudgetContractController + authOrCancel = 合同审批接口
     * - BudgetClaimController + apply = 付款申请接口
     * - BudgetClaimController + authOrCancel = 付款审批接口
     * - BudgetAdjustController + apply = 预算调整申请接口
     * - BudgetAdjustController + authOrCancel = 预算调整审批接口
     * - BudgetQueryController + query = 预算查询接口
     */
    @TableField(value = "INTERFACE_NAME")
    private String interfaceName;

    /**
     * 单据号（视图计算字段）
     * 从REQUEST_PARAMS JSON中提取：
     * - BudgetApplicationController: 提取 "demandOrderNo"
     * - BudgetContractController: 提取 "contractNo"
     * - BudgetClaimController: 提取 "claimOrderNo"
     * - BudgetAdjustController: 提取 "adjustOrderNo"
     */
    @TableField(value = "DOC_NO")
    private String docNo;
}

