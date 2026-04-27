package com.jasolar.mis.framework.redis.lock;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import jakarta.annotation.Nullable;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Redssion的执行对象，用于在分布式锁中执行方法。当方法同时只允许一个节点运行时，需要此类的处理。
 *
 * @author galuo
 * @date 2020-09-07 10:01
 */
@Slf4j
@Getter
@Setter
public class RedissonLockExecutor implements RedisLockExecutor {

    private RedissonClient redisson;

    /** 锁的存活时间,默认为10分钟 */
    private long lockLeaseTime = TimeUnit.MINUTES.toMillis(10);

    /** 获取锁的等待时间，默认为3秒 */
    private long lockWaitTime = TimeUnit.SECONDS.toMillis(3);

    /** 默认获取锁的key添加前缀 */
    private String keyPreix = "LOCKS:";

    /**
     * 指定RedissonClient构造对象
     *
     * @param redisson RedissonClient对象
     */
    public RedissonLockExecutor(RedissonClient redisson) {
        super();
        this.redisson = redisson;
    }

    /**
     * 获取缓存的KEY
     * 
     * @param redisKey
     * @return
     */
    protected String key(String redisKey) {
        return this.keyPreix + redisKey;
    }

    @Override
    public <V> V tryExecute(String lockKey, Callable<V> callable) {
        return this.tryExecute(lockKey, callable, lockWaitTime, lockLeaseTime);
    }

    @Override
    public RLock tryLock(String lockKey, long waitTime, long leaseTime) {
        RLock lock = redisson.getLock(key(lockKey));
        try {
            if (lock.tryLock(waitTime, leaseTime, TimeUnit.MILLISECONDS)) {
                return lock;
            }
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }
        return null;
    }

    /**
     * 获取分布式锁
     *
     * @param lockKey 锁定
     * @return 未能锁定则返回false
     */
    @Override
    public RLock tryLock(String lockKey) {
        return this.tryLock(lockKey, this.lockWaitTime, this.lockLeaseTime);
    }

    @Override
    public RLock lock(String lockKey) {
        RLock lock = redisson.getLock(key(lockKey));
        lock.lock(this.lockLeaseTime, TimeUnit.MILLISECONDS);
        return lock;
    }

    /**
     * 释放分布式锁
     *
     * @param lock 分布式锁,只能是RLock
     */
    @Override
    public void unlock(@Nullable Lock lock) {
        try {
            if (lock instanceof RLock r && r.isHeldByCurrentThread()) {
                lock.unlock();
            }
        } catch (IllegalMonitorStateException ignore) {
            // 有可能是在执行到这里时锁已经被自动释放了
            if (log.isWarnEnabled()) {
                log.warn("Redisson unlock exception,可能是锁已经到期自动释放.", ignore);
            }
        }
    }

    @Override
    public void unlock(Lock lock, long tid) {
        try {
            if (lock instanceof RLock r && r.isHeldByThread(tid)) {
                lock.unlock();
            }
        } catch (IllegalMonitorStateException ignore) {
            // 有可能是在执行到这里时锁已经被自动释放了
            if (log.isWarnEnabled()) {
                log.warn("Redisson unlock exception,可能是锁已经到期自动释放.", ignore);
            }
        }
    }

}
