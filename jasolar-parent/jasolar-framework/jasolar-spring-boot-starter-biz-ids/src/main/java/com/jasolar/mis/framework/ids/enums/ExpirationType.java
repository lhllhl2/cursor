package com.jasolar.mis.framework.ids.enums;

/**
 * 过期类型, 也即生成序列号的间隔
 * 
 * @author galuo
 * @date 2025-04-15 15:24
 *
 */
public enum ExpirationType {
    /** 每天重新生成序号 */
    DAILY, // 每天
    /** 每月重新生成序号 */
    MONTHLY, // 每月
    /** 每年重新生成序号 */
    YEARLY, // 每年

    /** 永不过期,序号在整个系统中一直递增 */
    NEVER // 永不过期
}