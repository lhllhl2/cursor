package com.jasolar.mis.framework.datapermission.core.scope;

import java.util.Arrays;

import com.jasolar.mis.framework.common.core.ArrayValuable;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 数据范围级别枚举类.
 * <ol>
 * <li>999 所有权限</li>
 * <li>1~99 用于统一定义所有权限维度可能的等级</li>
 * <li>100~109 用于控制人员权限</li>
 * <li>110~119 用于控制部门权限</li>
 * </ol>
 * 
 *
 * @author galuo
 */
@Getter
@AllArgsConstructor
public enum ScopeLevel implements ArrayValuable<Integer> {

    /** 全部数据权限 */
    ALL(999), // 全部数据权限

    /** 指定ID的数据权限 */
    ID(1), // 指定具体的数据权限

    /** 本人所属的数据权限 */
    SELF(100);
    //
    // /** 查看本部门数据权限 */
    // DEPT_SELF(110),
    //
    // /** 查看本部门及下属部门数据权限 */
    // DEPT_SELF_AND_CHILDREN(111);

    /** 权限范围等级 */
    private final int level;

    /**
     * 
     * 根据指定的整数获取枚举类型
     * 
     * @param level
     * @return
     * @throws IllegalArgumentException level的值不在枚举中
     */
    public static ScopeLevel of(Integer level) throws IllegalArgumentException {
        if (level != null) {
            for (ScopeLevel sc : ScopeLevel.values()) {
                if (sc.level == level) {
                    return sc;
                }
            }
        }
        throw new IllegalArgumentException("The parameter value '" + level + "' with the name 'type' cannot be recognized ");
    }

    public static final Integer[] VALUES = Arrays.stream(values()).map(ScopeLevel::getLevel).toArray(Integer[]::new);

    @Override
    public Integer[] array() {
        return VALUES;
    }
}
