/*
 * Copyright (c) 2020, @deloitte.com.cn. All rights reserved.
 */
package com.jasolar.mis.framework.redis.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * Redisson Cache的相关配置
 * 
 * @author galuo
 * @date 2020-06-12 18:26
 */
@Data
@ConfigurationProperties(prefix = "spring.redis.redisson")
public class RedissonClientProperties {
    /** 锁的超时时间, 默认10分钟 */
    protected long lockLeaseTime = 600000L;
    /** 获取锁的等待时间, 默认2秒 */
    protected long lockWaitTime = 2000L;

    // /** 自定义的相关的配置 */
    // protected CustomizerPoperties customizer = new CustomizerPoperties();
    //
    // /**
    // * Redis Connection的相关配置
    // *
    // * @author galuo
    // * @date 2022/04/07
    // */
    // static class CustomizerPoperties {
    //
    // /** 最小空闲链接数量 */
    // protected int connectionMinimumIdleSize = 3;
    //
    // /** 连接池数量 */
    // protected int connectionPoolSize = 64;
    // /**
    // * If pooled connection not used for a <code>timeout</code> time and current connections amount bigger than
    // * minimum idle connections pool size, then it will closed and removed from pool. Value in milliseconds.
    // *
    // */
    // protected int idleConnectionTimeout = 10000;
    //
    // /**
    // * Timeout during connecting to any Redis server. Value in milliseconds.
    // *
    // */
    // protected int connectTimeout = 3000;
    //
    // /**
    // * Redis server response timeout. Starts to countdown when Redis command was succesfully sent. Value in
    // * milliseconds.
    // *
    // */
    // protected int timeout = 3000;
    //
    // protected int retryAttempts = 3;
    //
    // protected int retryInterval = 1500;
    //
    // /** Enables sentinels list check during Redisson startup. 配置为true则哨兵模式要至少3个节点 */
    // protected boolean checkSentinelsList = false;
    // }

}
