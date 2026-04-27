package com.jasolar.mis.framework.mybatis.core.id;

/**
 * 不需要dataCenterId，仅保留workerId的雪花算法.
 *
 * @author galuo
 * @date 2020-06-08 17:22
 */
@SuppressWarnings("serial")
public class WorkerOnlySnowflakeIdGenerator extends SnowflakeIdGenerator {

    /** work id在ID中所占的位数 */
    public static final int WORK_ID_BITS = DEFAULT_WORK_ID_BITS + DEFAULT_DATA_CENTER_ID_BITS;
    /** 最大允许的workId */
    public static final int MAX_WORK_ID = (int) (-1L ^ -1L << WORK_ID_BITS);

    /**
     * 指定workId生成对象
     *
     * @param workerId 当前使用的workId
     */
    public WorkerOnlySnowflakeIdGenerator(long workerId) {
        super(0, WORK_ID_BITS, DEFAULT_SEQUENCE_BITS, 0, workerId);
    }

}
