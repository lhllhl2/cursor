package com.jasolar.mis.framework.redis.config;

import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.spring.starter.RedissonAutoConfigurationCustomizer;
import org.redisson.spring.starter.RedissonAutoConfigurationV2;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jasolar.mis.framework.common.util.json.JsonUtils;
import com.jasolar.mis.framework.redis.lock.RedisLockExecutor;
import com.jasolar.mis.framework.redis.lock.RedissonLockExecutor;

/**
 * Redis 配置类
 */
@EnableConfigurationProperties({ RedissonClientProperties.class })
@AutoConfiguration(before = RedissonAutoConfigurationV2.class) // 目的：使用自己定义的 RedisTemplate Bean
public class JasolarRedisAutoConfiguration {

    /**
     * 创建 RedisTemplate Bean，使用 JSON 序列化方式
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        // 创建 RedisTemplate 对象
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        // 设置 RedisConnection 工厂。😈 它就是实现多种 Java Redis 客户端接入的秘密工厂。感兴趣的胖友，可以自己去撸下。
        template.setConnectionFactory(factory);
        // 使用 String 序列化方式，序列化 KEY 。
        template.setKeySerializer(RedisSerializer.string());
        template.setHashKeySerializer(RedisSerializer.string());
        // 使用 JSON 序列化方式（库是 Jackson ），序列化 VALUE 。
        RedisSerializer<Object> serializer = buildRedisSerializer();
        template.setValueSerializer(serializer);
        template.setHashValueSerializer(serializer);
        return template;
    }

    /**
     * 构建Redis序列化数据的对象
     * 
     * @return
     */
    public static RedisSerializer<Object> buildRedisSerializer() {
        return new GenericJackson2JsonRedisSerializer(objectMapper());
    }

    /**
     * 使redisson使用JSON格式序列化
     * 
     * @return
     */
    @Bean
    public RedissonAutoConfigurationCustomizer jacksonCodecCustomizer() {
        return cfg -> cfg.setCodec(new JsonJacksonCodec(objectMapper(), false));
    }

    /**
     * 生成ObjectMapper对象
     * 
     * @return
     */
    private static ObjectMapper objectMapper() {
        ObjectMapper objectMapper = JsonJacksonCodec.INSTANCE.getObjectMapper();
        JsonUtils.compact(objectMapper);
        return objectMapper.copy();
    }

    @Bean
    @ConditionalOnMissingBean(RedisLockExecutor.class)
    public RedisLockExecutor redisLockExecutor(RedissonClient redisson, RedissonClientProperties redissonClientProperties) {
        RedissonLockExecutor e = new RedissonLockExecutor(redisson);
        if (redissonClientProperties.getLockWaitTime() > 0) {
            e.setLockWaitTime(redissonClientProperties.getLockWaitTime());
        }
        if (redissonClientProperties.getLockLeaseTime() > 0) {
            e.setLockLeaseTime(redissonClientProperties.getLockLeaseTime());
        }
        return e;
    }
}
