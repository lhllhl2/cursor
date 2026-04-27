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
 * OA审批流推送任务表（BUDGET_OA_APPROVAL_PUSH_TASK）
 * 一个组织对应一个推送任务
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@TableName(value = "budget_oa_approval_push_task", autoResultMap = true)
public class BudgetOaApprovalPushTask extends BaseDO {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 任务ID
     */
    private String taskId;

    /**
     * 业务实体ID
     */
    private String entityId;

    /**
     * 管理组织编码
     */
    private String morgCode;

    /**
     * 上级管理组织编码
     */
    private String parentMorgCode;

    /**
     * 管理组织类型
     */
    private String morgType;

    /**
     * 管理组织名称
     */
    private String morgName;

    /**
     * 任务状态
     */
    private String status;

    /**
     * 版本ID
     */
    private String versionId;

    /**
     * 版本名称
     */
    private String versionName;

    /**
     * 推送次数
     */
    private Integer sendTimes;

    /**
     * 是否按审批层级推送（1:是,0:否）
     */
    private Integer isApprovalLevel;

    /**
     * 脚本类型（与 SYSTEM_MANAGE_ORG.SCRIPT_TYPE 一致）
     */
    private String scriptType;

    /**
     * 员工工号
     */
    private String employeeNo;

    /**
     * OA流程请求ID（首次新建流程返回）
     */
    private String requestId;
}
