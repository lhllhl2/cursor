package com.jasolar.mis.framework.mybatis.core.id;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZoneOffset;

import lombok.Getter;

/**
 * 雪花算法生成ID基类.
 * 
 * 在各项目中需要注意配置dataCenterId, workerId防止重复. 一般的可使用redis进行缓存已使用的id防止重复
 * 
 * id的结构如下(每部分用-分开): 0 - 0000000000 0000000000 0000000000 0000000000 0 - 00000 - 00000 - 0000000000 00
 * 第一位未使用,(主要是防止生成负数id，如果使用可能生成负数id)， 接下来的41位为毫秒级时间， 然后是5位datacenterId和5位workerId(10位的长度最多支持部署1024个节点），
 * 最后12位是毫秒内的计数（12位的计数顺序号支持每个节点每毫秒产生4096个ID序号） 一共加起来刚好64位，为一个Long型。(转换成字符串长度为19)
 * 
 * snowflake生成的ID整体上按照时间自增排序，并且整个分布式系统内不会产生ID碰撞（由datacenter和workerId作区分），并且效率较高。据说：snowflake每秒能够产生26万个ID。.
 *
 * @author LuoGang
 */
@SuppressWarnings("serial")
public class SnowflakeIdGenerator implements IdGenerator, Serializable {
    /** dataCenterId 默认占5位 */
    public static final int DEFAULT_DATA_CENTER_ID_BITS = 5;
    /** workdId 默认占5位 */
    public static final int DEFAULT_WORK_ID_BITS = 5;
    /** 递增序列默认占12位 */
    public static final int DEFAULT_SEQUENCE_BITS = 12;

    /** 默认的开始时间,生成ID的毫秒时间戳需要减去此时间 */
    public static final long DEFAULT_START_TIME = LocalDate.of(2025, 6, 10).atStartOfDay(ZoneOffset.systemDefault()).toInstant()
            .toEpochMilli();

    /** 起始时间,一般配置为项目开始时间，一旦配置不允许变更 */
    @Getter
    private long startTimeMillis = DEFAULT_START_TIME;

    /** dataCenterId所占的位数 */
    @Getter
    private int dataCenterIdBits = DEFAULT_DATA_CENTER_ID_BITS;

    /** workId所占的位数 */
    @Getter
    private int workerIdBits = DEFAULT_WORK_ID_BITS;

    /** 每毫秒可生成的序列所占的位数 */
    @Getter
    private int sequenceBits = DEFAULT_SEQUENCE_BITS;

    /** workId的最大值,从0开始 */
    @Getter
    private long maxWorkerId = -1L ^ -1L << workerIdBits;

    /** dataCenterId的最大值,从0开始 */
    @Getter
    private long maxDatacenterId = -1L ^ -1L << workerIdBits;

    /** 1毫秒内可生成的序号的最大值,从0开始 */
    @Getter
    private long maxSequence = -1L ^ -1L << workerIdBits;

    /** workId在id中最终要左移位的位数 */
    private int workerIdShift = sequenceBits;

    /** dataCenterId在id中最终要左移位的位数 */
    private int dataCenterIdShift = sequenceBits + workerIdBits;

    /** 时间戳在id中最终要左移位的位数 */
    private int timeShift = sequenceBits + workerIdBits + dataCenterIdBits;

    @Getter
    private long workerId = 0L;

    @Getter
    private long dataCenterId = 0L;

    @Getter
    private long sequence = 0L;

    /** 最后一次生成序号的时间 */
    @Getter
    private long lastTimeMillis = -1L;

    /** 默认构造器，workerId和datacenterId为0 */
    public SnowflakeIdGenerator() {
    }

    /**
     * 通过指定参数构造
     *
     * @param dataCenterIdBits
     * @param workerIdBits
     * @param sequenceBits
     * @param dataCenterId
     * @param workerId
     */
    public SnowflakeIdGenerator(int dataCenterIdBits, int workerIdBits, int sequenceBits, long dataCenterId, long workerId) {
        super();
        this.initialize(dataCenterIdBits, workerIdBits, sequenceBits, dataCenterId, workerId);
    }

    /**
     * 通过指定参数构造
     *
     * @param dataCenterIdBits
     * @param workerIdBits
     * @param sequenceBits
     * @param dataCenterId
     * @param workerId
     * @param startTimeMillis 计数开始时间
     */
    public SnowflakeIdGenerator(int dataCenterIdBits, int workerIdBits, int sequenceBits, long dataCenterId, long workerId,
            long startTime) {
        this(dataCenterIdBits, workerIdBits, sequenceBits, dataCenterId, workerId);
        this.startTimeMillis = startTime;
    }

    /**
     * 通过指定参数构造
     *
     * @param dataCenterId
     * @param workerId
     */
    public SnowflakeIdGenerator(long dataCenterId, long workerId) {
        super();
        this.initialize(DEFAULT_DATA_CENTER_ID_BITS, DEFAULT_WORK_ID_BITS, DEFAULT_SEQUENCE_BITS, dataCenterId, workerId);
    }

    /**
     * 通过指定参数构造
     *
     * @param dataCenterId
     * @param workerId
     * @param startTimeMillis 计数开始时间
     */
    public SnowflakeIdGenerator(long dataCenterId, long workerId, long startTime) {
        this(dataCenterId, workerId);
        this.startTimeMillis = startTime;
    }

    /**
     * 初始数据
     *
     * @param dataCenterIdBits dataCenterId所占的位数,0~10
     * @param workerIdBits workerId所占的位数,0~10
     * @param sequenceBits 每毫秒可生成的序列所占的位数,1~20
     * @param dataCenterId 当前使用的dataCenterId
     * @param workerId 当前使用的workId
     */
    protected void initialize(int dataCenterIdBits, int workerIdBits, int sequenceBits, long dataCenterId, long workerId) {
        this.dataCenterIdBits = dataCenterIdBits;
        this.workerIdBits = workerIdBits;
        this.sequenceBits = sequenceBits;

        this.workerIdShift = sequenceBits;
        this.dataCenterIdShift = sequenceBits + workerIdBits;
        this.timeShift = sequenceBits + workerIdBits + dataCenterIdBits;

        this.maxWorkerId = -1L ^ -1L << workerIdBits;
        this.maxDatacenterId = -1L ^ -1L << dataCenterIdBits;
        this.maxSequence = -1L ^ -1L << sequenceBits;

        this.setDataCenterId(dataCenterId);
        this.setWorkerId(workerId);
    }

    @Override
    public synchronized long generate() {
        long mills = currentTimeMillis();
        if (mills < lastTimeMillis) {
            // 时钟回滚，抛出异常，防止生成重复ID
            throw new RuntimeException(
                    String.format("Clock moved backwards. Refusing to generate id for %d milliseconds", lastTimeMillis - mills));
        }

        if (lastTimeMillis == mills) {
            sequence = sequence + 1 & maxSequence;
            if (sequence == 0) {
                mills = nextTimeMillis();
            }
        } else {
            sequence = 0;
        }

        lastTimeMillis = mills;

        return mills - startTimeMillis << timeShift | dataCenterId << dataCenterIdShift | workerId << workerIdShift | sequence;
    }

    /**
     * 得到基于当前毫秒时间的时间戳
     *
     * @return {@link System#currentTimeMillis()}
     */
    protected long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    /**
     * 得到新的有效时间戳，生成的时间必须大于{@link lastTimeMillis}
     *
     * @return 下一个时间戳
     */
    protected long nextTimeMillis() {
        long mills = currentTimeMillis();
        while (mills <= lastTimeMillis) {
            mills = currentTimeMillis();
        }
        return mills;
    }

    /**
     * 指定当前使用的workId
     *
     * @param workerId 当前使用的workId
     */
    public void setWorkerId(long workerId) {
        if (workerId > maxWorkerId || workerId < 0) {
            throw new IllegalArgumentException(
                    String.format("worker id %d can't be greater than %d or less than 0", workerId, maxWorkerId));
        }

        this.workerId = workerId;
    }

    /**
     * 指定当前使用的dataCenterId
     *
     * @param dataCenterId 当前使用的dataCenterId
     */
    public void setDataCenterId(long dataCenterId) {
        if (dataCenterId > maxDatacenterId || dataCenterId < 0) {
            throw new IllegalArgumentException(
                    String.format("datacenter id %d can't be greater than %d or less than 0", dataCenterId, maxDatacenterId));
        }
        this.dataCenterId = dataCenterId;
    }
}
