package com.jasolar.mis.framework.mybatis.core.dataobject;

import java.time.LocalDateTime;

/**
 * BPM业务申请单
 * 
 * @author galuo
 * @date 2025-03-11 16:39
 *
 */
public interface IBpmBizDO extends IBaseScopeDO {

    /** @return 业务数据主键ID */
    Long getId();

    /** @return 申请单号 */
    String getNo();

    /**
     * 设置申请单号
     * 
     * @param no 申请单号
     */
    void setNo(String no);

    /** @return 业务类型 */
    String getBizType();

    /**
     * 设置业务类型
     * 
     * @param bizType 业务类型, 用于生成id. 与{@link com.fiifoxconn.mis.framework.ids.IBizType}的实现对应
     */
    void setBizType(String bizType);

    /** @return 流程定义 */
    String getProcDefKey();

    /**
     * 设置流程定义
     * 
     * @param procDefKey 流程定义标识
     */
    void setProcDefKey(String procDefKey);

    /** @return 流程实例ID */
    String getProcInstId();

    /**
     * 设置流程实例ID
     * 
     * @param procInstId 流程实例ID
     */
    void setProcInstId(String procInstId);

    /** @return 申请日期. 如果是草稿,等于创建时间.提交审批后,则为提交审批的时间 */
    LocalDateTime getSubmitTime();

    /**
     * 设置申请日期
     * 
     * @param submitTime 申请日期
     */
    void setSubmitTime(LocalDateTime submitTime);

    /** @return 审批完成时间 */
    LocalDateTime getCompleteTime();

    /**
     * 设置审批完成时间
     * 
     * @param completeTime
     */
    void setCompleteTime(LocalDateTime completeTime);

    /**
     * 流程状态. 默认为字典bpm_proc_status. 一般的申请单只有4种状态: DRAFT.草稿, APPROVING.审批中, APPROVED.审批通过, REJECTED.审批拒绝.
     * 如果业务中有特殊状态,需要单独定义字典
     * 
     * @return
     */
    String getStatus();

    /**
     * 修改流程状态.
     * 默认为字典bpm_proc_status. 一般的申请单只有4种状态: DRAFT.草稿, APPROVING.审批中, APPROVED.审批通过, REJECTED.审批拒绝.
     * 如果业务中有特殊状态,需要单独定义字典
     * 
     * @param status 流程状态
     */
    void setStatus(String status);

    /**
     * 当前审批人名称,多个之间逗号隔开
     * 
     * @return 当前审批人名称
     */
    String getCurrentApproverName();

    /**
     * 设置当前审批人名称
     * 
     * @param currentApproverName 当前审批人名称
     */
    void setCurrentApproverName(String currentApproverName);
}
