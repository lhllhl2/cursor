package com.jasolar.mis.framework.lock4j.core;

/**
 * Lock4j Redis Key 枚举类
 *
 * @author zhaohuang
 */
public interface Lock4jRedisKeyConstants {
    default String getKey(String key) {
        return String.format(LOCK4J, key);
    }

    /**
     * 分布式锁
     * <p>
     * KEY 格式：lock4j:%s // 参数来自 DefaultLockKeyBuilder 类
     * VALUE 数据格式：HASH // RLock.class：Redisson 的 Lock 锁，使用 Hash 数据结构
     * 过期时间：不固定
     */
    String LOCK4J = "lock4j:%s";

}
