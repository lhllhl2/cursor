package com.jasolar.mis.framework.datapermission.core.scope;

import java.util.Arrays;

import com.jasolar.mis.framework.common.core.ArrayValuable;

import lombok.AllArgsConstructor;

/**
 * 用于控制读写分离的权限
 * 
 * @author galuo
 * @date 2025-03-03 09:39
 *
 */
@AllArgsConstructor
public enum ReadWrite implements ArrayValuable<Integer> {
    /** 读写不分离 */
    ALL(0), // 全部权限
    /** 仅适用于读操作的权限 */
    READ(1), // 读权限
    /** 仅适用于写操作的权限 */
    WRITE(2); // 写权限

    /** 类型 */
    private final int value;

    /** @return 值value */
    public int value() {
        return this.value;
    }

    /**
     * 
     * 根据指定的整数获取枚举类型
     * 
     * @param value
     * @return
     * @throws IllegalArgumentException type的值不在枚举中
     */
    public static ReadWrite of(Integer value) throws IllegalArgumentException {
        if (value != null) {
            for (ReadWrite rw : values()) {
                if (rw.value == value) {
                    return rw;
                }
            }
        }
        throw new IllegalArgumentException("The parameter value '" + value + "' with the name 'value' cannot be recognized ");
    }

    public static final Integer[] VALUES = Arrays.stream(values()).map(ReadWrite::value).toArray(Integer[]::new);

    @Override
    public Integer[] array() {
        return VALUES;
    }

}
