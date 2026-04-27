package com.jasolar.mis.framework.ids.sn;

import com.jasolar.mis.framework.common.pojo.CommonResult;
import com.jasolar.mis.framework.common.util.date.LocalDateTimeUtils;
import com.jasolar.mis.framework.ids.enums.ExpirationType;
import com.jasolar.mis.module.infra.api.sn.DocumentSnApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RedissonClient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Redis流水号生成器
 */
@Slf4j
@RequiredArgsConstructor
public class SerialNumberProvider {

    /**
     * 冒号分隔符
     */
    public static final String COLON = ":";
    /**
     * 缓存前缀, 缓存KEY的格式为: "前缀:bizType:dateValue"
     */
    public static final String REDIS_KEY_PREFIX = "SN" + COLON;

    private static final DateTimeFormatter DAILY_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter MONTHLY_FORMATTER = DateTimeFormatter.ofPattern("yyyyMM");

    private final RedissonClient redissonClient;

    private final DocumentSnApi documentSnApi;

    /**
     * 生成流水号
     *
     * @param bizType
     * @param expirationType
     * @return
     */
    public long generateSerialNumber(String bizType, ExpirationType expirationType) {
        String dateSuffix = getDateSuffix(expirationType);
        String redisKey = REDIS_KEY_PREFIX + bizType + COLON + dateSuffix;
        RAtomicLong atomicLong = redissonClient.getAtomicLong(redisKey);
        if (!atomicLong.isExists()) {
            // 没有缓存则读取数据库
            CommonResult<Long> r = documentSnApi.currentSerialNumber(bizType, dateSuffix);
            Long currentSN = r.getCheckedData();
            if (currentSN == null) {
                log.warn("不存在对应的流水号记录，重新开始");
                currentSN = 0L; // 设置默认值为 1
            }

            // 防止多线程并发写入
            atomicLong.compareAndSet(0, currentSN);
            setExpiry(atomicLong, expirationType);
        }
        return atomicLong.incrementAndGet();
    }

    /**
     * 生成负增长的序列号
     *
     * @param bizType 业务类型
     * @param originalValue 原始值
     * @return 生成的负增长序列号
     */
    public long generateNegativeSerialNumber(String bizType, String originalValue) {
        String dateSuffix = getDateSuffix(ExpirationType.NEVER);
        String redisKey = REDIS_KEY_PREFIX + bizType + COLON + dateSuffix;
        RAtomicLong atomicLong = redissonClient.getAtomicLong(redisKey);

        // 获取当前序列号
        long currentValue = atomicLong.get();

        // 如果当前序列号不存在，则使用 originalValue
        if (currentValue == 0) {
            if (originalValue != null) {
                try {
                    currentValue = Long.parseLong(originalValue);
                } catch (NumberFormatException e) {
                    log.warn("原始值格式不正确，使用默认值 0");
                    // 设置默认值为 0
                }
            } else {
                log.warn("不存在对应的流水号记录，重新开始");
                // 设置默认值为 0
            }

            // 防止多线程并发写入
            atomicLong.compareAndSet(0, currentValue);
            setExpiry(atomicLong, ExpirationType.NEVER);// 重新获取当前值
        }

        // 返回负增长后的序列号
        return atomicLong.decrementAndGet();
    }

    /**
     * 根据过期类型获取key的日期后缀
     *
     * @param expirationType
     * @return
     */
    private String getDateSuffix(ExpirationType expirationType) {
        return switch (expirationType) {
            case DAILY -> LocalDate.now().format(DAILY_FORMATTER);
            case MONTHLY -> LocalDate.now().format(MONTHLY_FORMATTER);
            case YEARLY -> String.valueOf(LocalDate.now().getYear());
            case NEVER -> "NEVER";
        };
    }

    /**
     * 设置过期时间
     *
     * @param atomicLong
     * @param redisKey
     * @param expirationType
     */
    private void setExpiry(RAtomicLong atomicLong, ExpirationType expirationType) {
        switch (expirationType) {
            case DAILY:
                atomicLong.expire(LocalDateTimeUtils.atEndOfDay(LocalDateTime.now()).atZone(ZoneId.systemDefault()).toInstant());
                break;
            case MONTHLY:
                atomicLong.expire(LocalDateTimeUtils.endOfMonth(LocalDateTime.now()).atZone(ZoneId.systemDefault()).toInstant());
                break;
            case YEARLY:
                atomicLong.expire(LocalDateTimeUtils.atEndOfYear(LocalDateTime.now()).atZone(ZoneId.systemDefault()).toInstant());
                break;
            case NEVER:
                log.debug("Key {} 将永不过期.", atomicLong.getName());
                return; // 永不过期
        }
    }
}
