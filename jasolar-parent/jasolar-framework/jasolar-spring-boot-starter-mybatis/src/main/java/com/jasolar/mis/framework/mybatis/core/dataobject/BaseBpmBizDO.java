package com.jasolar.mis.framework.mybatis.core.dataobject;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.jasolar.mis.framework.mybatis.core.enums.BpmStatusEnum;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuppressWarnings("serial")
@Data
@SuperBuilder
@NoArgsConstructor
public abstract class BaseBpmBizDO extends BaseScopeDO implements IBpmBizDO {

    /** 流程业务类型, 即IBizType的实现对象, 用于流程的相关配置,如获取NO,获取流程定义key */
    @Schema(description = "业务类型")
    private String bizType;

    /** 申请单号 */
    @Schema(description = "业务单号")
    private String no;

    /** 流程实例ID */
    @Schema(description = "流程实例ID")
    private String procInstId;
    /** 流程标识;画流程图时定义的流程标识 */
    @Schema(description = "流程标识")
    private String procDefKey;
    /** 申请日期;如果是草稿,等于创建时间.提交审批后,则为提交审批的时间 */
    @Schema(description = "申请日期;如果是草稿,等于创建时间.提交审批后,则为提交审批的时间")
    private LocalDateTime submitTime;
    /** 审批完成时间 */
    @Schema(description = "审批完成时间")
    private LocalDateTime completeTime;

    /**
     * 审批状态;字典bpm_proc_status. 默认的状态参见枚举: {@link BpmStatusEnum}.
     * 如果业务中有特殊状态,需要单独定义字典和枚举
     * 
     * @see BpmStatusEnum
     */
    @Schema(description = "审批状态;字典bpm_proc_status. 默认的状态参见枚举: {@link BpmStatusEnum}. 如果业务中有特殊状态,需要单独定义字典和枚举")
    private String status;

    /** 当前流程的审批人 */
    @Schema(description = "当前流程的审批人")
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String currentApproverName;
}
