package com.jasolar.mis.framework.common.core;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Key Value 的键值对
 *
 * @author zhaohuang
 */
@SuppressWarnings("serial")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KeyValue<K, V> implements Serializable {

    private K key;

    private V value;

    /**
     * 构建数据
     * 
     * @param <K>
     * @param <V>
     * @param k
     * @param v
     * @return
     */
    public static <K, V> KeyValue<K, V> of(K k, V v) {
        return new KeyValue<>(k, v);
    }

}
