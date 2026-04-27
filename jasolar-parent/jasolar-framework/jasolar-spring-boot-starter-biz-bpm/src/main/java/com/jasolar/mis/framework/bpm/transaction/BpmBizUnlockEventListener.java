package com.jasolar.mis.framework.bpm.transaction;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 监听业务单据提交事件, 在业务单据提交操作的数据库事务提交后,启动审批流
 * 
 * @author galuo
 * @date 2025-05-20 15:15
 *
 * @param <T>
 */
public interface BpmBizUnlockEventListener {

    /**
     * 用于在事务提交后释放分布式锁
     * 
     * @param event 事件
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMPLETION, classes = BpmBizUnlockEvent.class)
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void unlock(BpmBizUnlockEvent event);

}
