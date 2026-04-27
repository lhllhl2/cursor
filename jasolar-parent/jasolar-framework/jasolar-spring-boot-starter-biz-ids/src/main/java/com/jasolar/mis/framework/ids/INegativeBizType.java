package com.jasolar.mis.framework.ids;

/**
 * 负增长业务类型接口
 */
public interface INegativeBizType {

    /**
     * 获取原始字符
     */
    String getOriginalValue();

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
}