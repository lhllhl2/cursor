package com.jasolar.mis.framework.data;

import com.jasolar.mis.framework.data.util.DeptUtils;
import com.jasolar.mis.framework.data.util.DictUtils;
import com.jasolar.mis.framework.data.util.PermissionUtils;
import com.jasolar.mis.framework.data.util.UserUtils;
import com.jasolar.mis.framework.redis.lock.RedisLockExecutor;
import lombok.SneakyThrows;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;

import java.time.Duration;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 无具体作用, 用于初始化RedissonClient
 * 
 * @author galuo
 * @date 2025-03-28 14:13
 *
 */
public class RedissonUtils implements InitializingBean {

    /** redisson客户端 */
    public static RedissonClient REDISSON;

    /** 分布式锁执行 */
    private static RedisLockExecutor LOCK_EXECUTOR;

    /** 在内存中的缓存时间,默认为1分钟 */
    public static Duration MEMORY_DURATION = Duration.ofMinutes(1);

    /** REDIS中的缓存时间,默认12小时 */
    public static Duration REDIS_DURATION = Duration.ofHours(12);

    /** 星号, 可以用于清空缓存 */
    public static final String STAR = "*";

    /** 缓存前缀 */
    public static final String DATA_KEY_PREFIX = "DATA:";

    /** 分布式锁 */
    public static final String LOCK_KEY = "LOCK:DATA";

    /** 内存缓存时间,单位分钟 */
    @Value("${jasolar.cache.data.duration.memory:1}")
    private int memoryDuration;

    /** redis缓存时间,单位分钟 */
    @Value("${jasolar.cache.data.duration.redis:600}")
    private int redisDuration;

    /** 无参构造函数 */
    protected RedissonUtils() {
        super();
    }

    /**
     * 使用redisson客户端初始化
     * 
     * @param redisson RedissonClient
     * @param lockExecutor RedisLockExecutor
     */
    public RedissonUtils(RedissonClient redisson, RedisLockExecutor lockExecutor) {
        super();
        REDISSON = redisson;
        LOCK_EXECUTOR = lockExecutor;
    }

    /**
     * 
     * 在分布式锁中执行方法
     * 
     * @param <V>
     * @param lockKey 锁的key
     * @param callable 要执行的方法
     * @return
     */
    @SneakyThrows
    public static <V> V execute(String lockKey, Callable<V> callable) {
        Lock lock = LOCK_EXECUTOR.lock(lockKey);
        try {
            return callable.call();
        } finally {
            LOCK_EXECUTOR.unlock(lock);
        }
    }

    /**
     * 根据前缀清空所有缓存
     * 
     * @param prefix 缓存key的前缀
     */
    public static void clear(String prefix) {
        REDISSON.getKeys().deleteByPattern(prefix + STAR);
    }

    /**
     * 删除指定的缓存
     * 
     * @param keys 要删除缓存key
     */
    public static void delete(String... keys) {
        REDISSON.getKeys().delete(keys);
    }

    /**
     * 如果obj等于defaultValue则返回null,否则返回obj. 主要方便缓存数据
     * 
     * @param <T> 数据类型
     * @param obj 要比较的值
     * @param defaultValue 默认值
     * @return 如果obj等于defaultValue则返回null,否则返回obj
     */
    public static <T> T nullIfDefault(T obj, T defaultValue) {
        return defaultValue.equals(obj) ? null : obj;
    }

    /**
     * 
     * 从map中删除符合条件的数据, 剩下的数据在一个新的map中返回,不直接修改map是因为外部传来的map可能无法修改
     * 
     * @param <K>
     * @param <V>
     * @param map 原始数据
     * @param predicate 要删除的数据条件
     * @return 删除符合条件的数据后, 剩下的数据在一个新的map中返回
     */
    public static <K, V> Map<K, V> removeIf(Map<K, V> map, Predicate<Entry<K, V>> predicate) {
        return map.entrySet().parallelStream().filter(predicate.negate()).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (memoryDuration > 0) {
            MEMORY_DURATION = Duration.ofMinutes(memoryDuration);
        }
        if (redisDuration > 0) {
            REDIS_DURATION = Duration.ofMinutes(redisDuration);
        }

        // 同时启动只执行一次
        LOCK_EXECUTOR.tryExecute(LOCK_KEY, () -> {
            // 启动时清除所有缓存
            clearAll();

            // 工具类全部初始化
            DeptUtils.init();
            // LegalUtils.init();
            UserUtils.init();
            DictUtils.init();
            // SupplierUtils.init();
            // MaterialCatergoryUtils.init();
            // MaterialUtils.init();
            //ConsignCenterUtils.init();
//            PermissionUtils.init();

            return null;
        });
    }

    /** 清除全部缓存 */
    public static void clearAll() {
        DeptUtils.clear();
        // LegalUtils.clear();
        UserUtils.clear();
        DictUtils.clear();
        // SupplierUtils.clear();
        // MaterialCatergoryUtils.clear();
        // MaterialUtils.clear();
        // ConsignCenterUtils.clear();
        PermissionUtils.clear();
    }

}
