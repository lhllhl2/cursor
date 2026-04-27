package com.jasolar.mis.framework.idempotent.core;

import java.util.concurrent.TimeUnit;

/**
 * 防重复KEY的操作
 * 
 * @author galuo
 * @date 2025-06-10 10:20
 *
 */
public interface IdempotentKeyOps {

    /**
     * 增加一个key, 如果已存在则返回false
     * 
     * @param key 重复的KEY
     * @param timeout 超时时间
     * @param timeUnit 时间单位
     * @return 如果key已存在则返回false, 否则添加key并返回true
     */
    boolean put(String key, long timeout, TimeUnit timeUnit);

    /**
     * 删除一个KEY
     * 
     * @param key 要删除的缓存KEY
     */
    void delete(String key);
}
