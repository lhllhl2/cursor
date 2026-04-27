/*
 * Copyright (c) 2020, @deloitte.com.cn. All rights reserved.
 */
package com.jasolar.mis.framework.mybatis.core.id;

import java.time.Duration;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.dromara.hutool.core.text.StrPool;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.commons.util.InetUtils.HostInfo;
import org.springframework.scheduling.annotation.Scheduled;

import com.jasolar.mis.framework.common.util.date.DateUtils;
import com.jasolar.mis.framework.common.util.spring.SpringUtils;

import cn.hutool.core.date.DateUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * 使用Redis生成workId.并指定一个有效期，在有效期内，通过定时任务重新刷新有效期.
 * 这样当系统停止后，生成的ID会被自动删除，释放出来给下一次使用
 *
 * @author galuo
 * @date 2020-06-08 16:17
 */
@Slf4j
public class RedisWorker implements DisposableBean {

    /** 生成的WorkId的存活时间，单位毫秒, 生成后保留2小时。 */
    public static final long TTL = 2 * 60 * 60 * 1000L;

    /** 当前生成的workId */
    private Integer id;
    /** 生成workId的时间，格式为{@link DateHelper#DEFAULT_DATETIME_FORMAT} */
    @Getter
    private Date createTime;
    /** 服务主机信息 */
    @Getter
    private String host;

    /** Redis客户端 */
    private final RedissonClient redisson;
    /** 允许的最大ID */
    @Getter
    private final int maxId;

    /** 格式化redis key的字符串 */
    private final String keyFormat;

    /** 最终生成id的Redis Key,与{@link #id}对应 */
    @Getter
    private String key;
    /** 缓存的值 */
    @Getter
    private String value;

    /**
     * @param inetUtils 用于解析本机信息
     * @param redisson RedissonClient客户端
     * @param maxId 可生成的最大的workId
     */
    public RedisWorker(InetUtils inetUtils, RedissonClient redisson, int maxWorkId) {
        super();
        this.redisson = redisson;
        this.maxId = maxWorkId;
        this.keyFormat = "WORKS:%0" + Integer.toString(maxId).length() + "d";
        HostInfo info = inetUtils.findFirstNonLoopbackHostInfo();
        this.host = info.getHostname() + "(" + info.getIpAddress() + ")";
    }

    /**
     * 得到当前可使用的workId
     *
     * @return 当前可使用的workId
     */
    public synchronized int getId() {
        if (id == null) {
            this.initialize();
        }
        return id;
    }

    /** 读取可用的workId并赋值到{@link #id}字段 */
    protected void initialize() {
        Date now = new Date();
        String value = this.formatValue(now);
        for (int i = 0; i <= maxId; i++) {
            String key = this.formatKey(i);
            RBucket<String> bucket = bucket(key);
            if (bucket.setIfAbsent(value, Duration.ofMillis(TTL))) {
                this.id = i;
                this.createTime = now;
                this.key = key;
                this.value = value;
                log.info("使用work id: {}, 服务: {}", id, value);
                return;
            }

            log.info("work id: {} 已经被其他服务使用: {}", i, bucket.get());
        }
    }

    /**
     * 生成Redis使用的key
     *
     * @param n 序号
     * @return Redis使用的key
     */
    private String formatKey(int n) {
        return String.format(keyFormat, n);
    }

    /**
     * 生成写入Redis的值
     *
     * @param date 时间戳
     * @return 写入Redis的值
     */
    private String formatValue(Date date) {
        String value = StringUtils.joinWith(StrPool.SLASH, SpringUtils.getApplicationName(), this.host,
                DateUtil.format(date, DateUtils.FORMAT_DATETIME));
        return value;
    }

    /**
     * 得到Redisson缓存对象
     *
     * @param key Redis Key
     * @return key对应的Redisson缓存对象
     */
    private RBucket<String> bucket(String key) {
        return redisson.getBucket(key);
    }

    /**
     * 在外部定时器中调用定时刷新work id，防止被redis删除缓存,调度的时间必须小于{@link #TTL}.
     *
     * @see WorkerOnlySnowflakeIdGeneratorFactory#refresh()
     */
    @Scheduled(fixedDelay = TTL >> 2, initialDelay = TTL >> 4)
    public synchronized void refresh() {
        if (this.id != null) {
            if (this.value == null) {
                this.value = formatValue(createTime);
            }
            log.info("刷新worker缓存:{}={}", this.key, value);
            bucket(this.key).set(this.value, Duration.ofMillis(TTL));
        }
    }

    /** 强制删除work id缓存，释放出此work id */
    public synchronized void delete() {
        if (this.id != null) {
            log.info("删除占用的worker缓存:{}", this.key);
            bucket(this.key).delete();
        }
    }

    @Override
    public void destroy() throws Exception {
        this.delete();
    }
}
