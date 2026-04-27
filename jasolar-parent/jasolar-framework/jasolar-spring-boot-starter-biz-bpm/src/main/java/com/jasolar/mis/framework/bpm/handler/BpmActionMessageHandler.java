package com.jasolar.mis.framework.bpm.handler;

import com.jasolar.mis.module.bpm.api.message.dto.BaseBpmMessageDTO;
import com.jasolar.mis.module.bpm.api.message.dto.BpmProcessMessageDTO;
import com.jasolar.mis.module.bpm.api.message.dto.BpmTaskMessageDTO;

/**
 * 为每个action分别定义一个回调方法
 * 
 * @author galuo
 * @date 2025-04-02 15:01
 *
 */
public interface BpmActionMessageHandler extends BpmMessageHandler {

    /**
     * 处理BPM消息(自动ACK)
     * 
     * @param message BPM消息
     */
    default void handleMessage(BaseBpmMessageDTO message) {
        switch (message.getAction()) {
        case TASK_ASSIGN:
            this.doTaskAssign((BpmTaskMessageDTO) message);
            break;
        case TASK_APPROVE:
            this.doTaskApprove((BpmTaskMessageDTO) message);
            break;
        case TASK_REJECT:
            this.doTaskReject((BpmTaskMessageDTO) message);
            break;
        case TASK_RETURN:
            this.doTaskReturn((BpmTaskMessageDTO) message);
            break;
        case TASK_CANCEL:
            this.doTaskCancel((BpmTaskMessageDTO) message);
            break;
        case PROCESS_START:
            this.doProcessStart((BpmProcessMessageDTO) message);
            break;
        case PROCESS_APPROVED:
            this.doProcessApproved((BpmProcessMessageDTO) message);
            break;
        case PROCESS_REJECT:
            this.doProcessReject((BpmProcessMessageDTO) message);
            break;
        case PROCESS_RETURN:
            this.doProcessReturn((BpmProcessMessageDTO) message);
            break;
        case PROCESS_CANCEL:
            this.doProcessCancel((BpmProcessMessageDTO) message);
            break;
        }
    }

    /**
     * 创建任务
     * 
     * @param message MQ消息
     */
    default void doTaskAssign(BpmTaskMessageDTO message) {
    }

    /**
     * 任务审批通过
     * 
     * @param message MQ消息
     */
    default void doTaskApprove(BpmTaskMessageDTO message) {
    }

    /**
     * 任务审批拒绝
     * 
     * @param message MQ消息
     */
    default void doTaskReject(BpmTaskMessageDTO message) {
    }

    /**
     * 任务退回
     * 
     * @param message MQ消息
     */
    default void doTaskReturn(BpmTaskMessageDTO message) {
    }

    /**
     * 任务取消, 适用于或签其中一人处理后, 取消其他人的任务的情况
     * 
     * @param message MQ消息
     */
    default void doTaskCancel(BpmTaskMessageDTO message) {
    }

    /**
     * 流程发起
     * 
     * @param message MQ消息
     */
    default void doProcessStart(BpmProcessMessageDTO message) {
        // 流程启动一般由业务端调用,所以一般可以不用写回调
    }

    /**
     * 流程审批通过
     * 
     * @param message MQ消息
     */
    void doProcessApproved(BpmProcessMessageDTO message);

    /**
     * 流程审批拒绝
     * 
     * @param message MQ消息
     */
    void doProcessReject(BpmProcessMessageDTO message);

    /**
     * 流程审批退回申请人
     * 
     * @param message MQ消息
     */
    void doProcessReturn(BpmProcessMessageDTO message);

    /**
     * 流程取消, 用于申请人撤回审批流
     * 
     * @param message MQ消息
     */
    void doProcessCancel(BpmProcessMessageDTO message);
}
