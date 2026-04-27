package com.jasolar.mis.framework.idempotent.core.redis;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;

import com.jasolar.mis.framework.idempotent.core.IdempotentKeyOps;

import lombok.AllArgsConstructor;

/**
 * 幂等 Redis DAO
 *
 * @author zhaohuang
 */
@AllArgsConstructor
public class IdempotentRedisDAO implements IdempotentKeyOps {

    private static final String PREFIX = "IDEMPOTENT:";

    private static String formatKey(String key) {
        return PREFIX + key;
    }

    private final RedissonClient redisson;

    @Override
    public void delete(String key) {
        String redisKey = formatKey(key);
        redisson.getKeys().delete(redisKey);
    }

    @Override
    public boolean put(String key, long timeout, TimeUnit timeUnit) {
        String redisKey = formatKey(key);
        RBucket<Integer> bucket = redisson.getBucket(redisKey);
        return bucket.setIfAbsent(1, Duration.ofMillis(timeUnit.toMillis(timeout)));
    }

}
