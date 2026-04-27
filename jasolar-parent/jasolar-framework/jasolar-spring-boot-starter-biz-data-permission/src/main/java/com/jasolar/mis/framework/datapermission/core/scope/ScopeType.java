package com.jasolar.mis.framework.datapermission.core.scope;

import java.util.Arrays;

import com.jasolar.mis.framework.common.core.ArrayValuable;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 数据权限维度。
 * 
 * @author galuo
 */
@Getter
@AllArgsConstructor
public enum ScopeType implements ArrayValuable<Integer> {

    /** 登录的供应商权限 */
    SUPPLIER_PORTAL(-1),

    /** 人员维度 */
    USER(0),

    /** 部门维度 */
    DEPT(1), // 部门维度权限

    /** 法人 */
    LEGAL(2), // 法人维度权限

    /** 事业群 */
    BUSINESS_GROUP(3),

    /** 事业处 */
    BUSINESS_UNIT(4),

    /** 工务窗口 */
    WORKING(50); // 工务窗口权限

    /** 维度类型 */
    private final int type;

    /**
     * 
     * 根据指定的整数获取枚举类型
     * 
     * @param type
     * @return
     * @throws IllegalArgumentException type的值不在枚举中
     */
    public static ScopeType of(Integer type) throws IllegalArgumentException {
        if (type != null) {
            for (ScopeType t : values()) {
                if (t.type == type) {
                    return t;
                }
            }
        }
        throw new IllegalArgumentException("The parameter value '" + type + "' with the name 'type' cannot be recognized ");
    }

    public static final Integer[] VALUES = Arrays.stream(values()).map(ScopeType::getType).toArray(Integer[]::new);

    @Override
    public Integer[] array() {
        return VALUES;
    }
}
