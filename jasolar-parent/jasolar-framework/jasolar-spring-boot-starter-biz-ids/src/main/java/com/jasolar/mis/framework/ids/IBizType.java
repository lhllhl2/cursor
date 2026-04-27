package com.jasolar.mis.framework.ids;

import com.jasolar.mis.framework.ids.enums.ExpirationType;

/**
 * @author zhahuang
 */
public interface IBizType {

    /**
     * 获取业务类型
     */
    String getBizType();

    /**
     * 获取序列长度
     */
    Integer getSequenceLength();

    /**
     * 获取前缀
     */
    String getPrefix();

    /**
     * 获取后缀
     */
    String getSuffix();

    /**
     * 流水号过期类型
     */
    default ExpirationType getExpirationType() {
        return ExpirationType.DAILY;
    }
}
