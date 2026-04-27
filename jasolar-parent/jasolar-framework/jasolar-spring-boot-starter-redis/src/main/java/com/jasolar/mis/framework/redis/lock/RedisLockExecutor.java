package com.jasolar.mis.framework.redis.lock;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import jakarta.annotation.Nullable;

/**
 * Redis分布式锁
 *
 * @author galuo@deloitte.com.cn
 * @date 2021-03-05 10:46
 *
 */
public interface RedisLockExecutor {

    /**
     * 在分布式锁中执行代码，并返回数据。此方法总是会执行callable并返回数据, 如果有被其他线程锁住,则会一直等待直到获取到锁继续执行
     *
     * @param <V> 返回数据的类型
     * @param lockKey 分布式锁的Redis Key
     * @param callable 要执行的方法
     * @return callable返回的值
     */
    default <V> V execute(String lockKey, Callable<V> callable) {
        Lock lock = lock(lockKey);
        try {
            return callable.call();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        } finally {
            unlock(lock);
        }
    }

    /**
     * 在分布式锁中执行代码，并返回数据。如果未能获得锁则不会执行方法，直接返回null
     *
     * @param <V> 返回数据的类型
     * @param lockKey 分布式锁的Redis Key
     * @param callable 要执行的方法
     * @param waitTime 获取锁的等待时间,超出时间未能获取锁则返回null, 单位毫秒
     * @param leaseTime 获取到锁后，锁的存活时间, 单位毫秒
     * @return callable返回的值
     */
    default <V> V tryExecute(String lockKey, Callable<V> callable, long waitTime, long leaseTime) {
        Lock lock = this.tryLock(lockKey, waitTime, leaseTime);
        try {
            return callable.call();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        } finally {
            unlock(lock);
        }
    }

    /**
     * 在分布式锁中执行代码，并返回数据。如果未能获得锁则不会执行方法，直接返回null.
     * 默认获取锁1秒超时, 锁时间1分钟.
     *
     * @param <V> 返回数据的类型
     * @param lockKey 分布式锁的Redis Key
     * @param callable 要执行的方法
     * @return callable返回的值
     */
    default <V> V tryExecute(String lockKey, Callable<V> callable) {
        return tryExecute(lockKey, callable, TimeUnit.SECONDS.toMillis(1), TimeUnit.MINUTES.toMillis(1));
    }

    /**
     * 获取分布式锁.
     * 获取锁的等待时间以及锁的存活时间由具体的实现类中指定默认值
     *
     * @param lockKey 分布式锁的Redis Key
     * @return 未能获取锁则返回null
     */
    default Lock tryLock(String lockKey) {
        return tryLock(lockKey, TimeUnit.SECONDS.toMillis(1), TimeUnit.MINUTES.toMillis(1));
    }

    /**
     * 获取分布式锁
     *
     * @param lockKey 分布式锁的Redis Key
     * @param waitTime 获取锁的等待时间,超出时间未能获取锁则返回null, 单位毫秒
     * @param leaseTime 获取到锁后，锁的存活时间, 单位毫秒
     * @return 未能获取锁则返回null
     */
    Lock tryLock(String lockKey, long waitTime, long leaseTime);

    /**
     * 获取分布式锁,如果有被其他线程锁住,则会一直等待直到获取到锁
     * 
     * @param lockKey 分布式锁的Redis Key
     * @return 分布式锁
     */
    Lock lock(String lockKey);

    /**
     * 释放分布式锁
     *
     * @param lock 分布式锁
     */
    default void unlock(@Nullable Lock lock) {
        lock.unlock();
    }

    /**
     * 释放分布式锁
     *
     * @param lock 分布式锁
     * @param tid 生成锁时的线程ID
     */
    void unlock(@Nullable Lock lock, long tid);
}
