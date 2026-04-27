package com.jasolar.mis.framework.biztask;

import com.jasolar.mis.framework.biztask.mq.BizTaskMessage;

/**
 * 业务待办消费者的服务
 * 
 * @author galuo
 * @date 2025-04-14 18:41
 *
 */
public interface BizTaskConsumerService {

    /**
     * 创建待办
     * 
     * @param bizTask 待办数据
     */
    boolean assignTask(BizTaskMessage bizTask);

    /**
     * 完成待办
     * 
     * @param bizTask 待办数据
     */
    boolean completeTask(BizTaskMessage bizTask);

    /**
     * 删除待办
     * 
     * @param bizTask 待办数据
     */
    boolean deleteTask(BizTaskMessage bizTask);
}
