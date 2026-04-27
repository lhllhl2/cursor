package com.jasolar.mis.framework.bpm.transaction;

import java.util.concurrent.locks.Lock;

import org.springframework.context.ApplicationEvent;

import lombok.Getter;

/**
 * 提交业务单据的事件,用于解除分布式锁
 * 
 * @author galuo
 * @date 2025-05-20 15:15
 *
 * @param <T>
 */
@SuppressWarnings("serial")
public class BpmBizUnlockEvent extends ApplicationEvent {
    // Thread.currentThread().getId()
    /** 线程ID */
    @Getter
    private final long tid;
    @Getter
    private final String bizType;
    @Getter
    private final String bizNo;

    /**
     * 构建事件
     * 
     * @param lock 分布式锁对象
     * @param tid 线程ID
     */
    public BpmBizUnlockEvent(Lock lock, String bizType, String bizNo) {
        super(lock);
        this.tid = Thread.currentThread().getId();
        this.bizType = bizType;
        this.bizNo = bizNo;
    }

    @Override
    public Lock getSource() {
        return (Lock) super.getSource();
    }

}
