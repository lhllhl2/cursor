package com.jasolar.mis.framework.mybatis.core.dataobject;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * BPM处理人，需要在每个使用BPM的微服务中创建xxx_bpm_user表，用于存放业务流程的BPM处理人数据。并且DO继承此类
 * 
 * @author galuo
 * @date 2025-03-11 13:47
 *
 */
@SuppressWarnings("serial")
@Data
@SuperBuilder
@NoArgsConstructor
public abstract class BaseBpmTaskDO extends BaseIdentityDO {

    /** 业务类型; 即IBizType的实现对象, 用于流程的相关配置,如获取ID,获取流程定义key */
    private String bizType;

    // /** 业务数据的ID */
    // @Deprecated
    // private Long bizId;

    /** 业务数据的单号 */
    private String bizNo;

    /** 流程实例ID */
    private String procInstId;

    /** 流程定义key */
    private String procDefKey;
    /** 流程名称 */
    private String procDefName;

    /** 任务定义key */
    private String taskDefKey;
    /** 任务名称 */
    private String taskDefName;

    /** 任务实例ID */
    private String taskId;

    /**
     * 任务处理状态。字典bpm_task_status
     * 
     * @see com.fiifoxconn.mis.framework.bpm.util.BpmBizTaskStatusEnum
     */
    private String taskStatus;

    /** 处理人工号 */
    private String userNo;

    /** 处理人名称 */
    private String userName;

    /** 被委托人工号 */
    private String delegateeNo;

    /** 被委托人名称 */
    private String delegateeName;

    /** 审批备注 */
    private String remark;

}
